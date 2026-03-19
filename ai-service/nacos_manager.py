import atexit
import hashlib
import json
import logging
import socket
import threading
import time
from urllib.parse import urlparse

import requests

from runtime_config import Config


LOGGER = logging.getLogger(__name__)


def _normalize_server_addr(server_addr):
    primary = str(server_addr or "").split(",", 1)[0].strip()
    if not primary:
        primary = "127.0.0.1:8848"
    if primary.startswith("http://") or primary.startswith("https://"):
        return primary.rstrip("/")
    return f"http://{primary}".rstrip("/")


def _extract_host(server_addr):
    parsed = urlparse(_normalize_server_addr(server_addr))
    return parsed.hostname or "127.0.0.1"


class NacosManager:
    def __init__(self):
        self.enabled = bool(Config.NACOS_ENABLED)
        self.discovery_enabled = bool(Config.NACOS_DISCOVERY_ENABLED or self.enabled)
        self.config_enabled = bool(Config.NACOS_CONFIG_ENABLED or self.enabled)
        self.base_url = _normalize_server_addr(Config.NACOS_SERVER_ADDR)
        self.namespace = Config.NACOS_NAMESPACE
        self.group = Config.NACOS_GROUP
        self.username = Config.NACOS_USERNAME
        self.password = Config.NACOS_PASSWORD
        self.config_data_id = Config.NACOS_CONFIG_DATA_ID
        self.service_name = Config.NACOS_SERVICE_NAME
        self.cluster_name = Config.NACOS_CLUSTER_NAME
        self.ephemeral = bool(Config.NACOS_EPHEMERAL)
        self.poll_timeout_ms = max(int(Config.NACOS_CONFIG_POLL_TIMEOUT_MS), 1000)
        self.poll_interval_seconds = max(int(Config.NACOS_CONFIG_POLL_INTERVAL_SECONDS), 1)
        self.heartbeat_interval_seconds = max(int(Config.NACOS_DISCOVERY_HEARTBEAT_INTERVAL_SECONDS), 1)
        self._access_token = ""
        self._token_expire_at = 0
        self._token_lock = threading.Lock()
        self._config_md5 = ""
        self._instance_ip = ""
        self._instance_port = 0
        self._instance_metadata = {}
        self._registered = False
        self._started = False
        self._stop_event = threading.Event()
        self._config_thread = None
        self._heartbeat_thread = None

    def bootstrap_config(self):
        if not self.config_enabled:
            return
        try:
            self._refresh_config()
        except Exception as exc:
            LOGGER.warning("Failed to load config from Nacos: %s", exc)

    def start(self):
        if self._started or not (self.discovery_enabled or self.config_enabled):
            return

        self._started = True
        atexit.register(self.stop)

        if self.discovery_enabled:
            try:
                self._register_instance()
            except Exception as exc:
                LOGGER.warning("Failed to register ai-service into Nacos: %s", exc)
            else:
                self._heartbeat_thread = threading.Thread(
                    target=self._heartbeat_loop,
                    name="nacos-heartbeat",
                    daemon=True,
                )
                self._heartbeat_thread.start()

        if self.config_enabled:
            self._config_thread = threading.Thread(
                target=self._config_listener_loop,
                name="nacos-config-listener",
                daemon=True,
            )
            self._config_thread.start()

    def stop(self):
        if not self._started:
            return

        self._started = False
        self._stop_event.set()

        if self._config_thread and self._config_thread.is_alive():
            self._config_thread.join(timeout=2)
        if self._heartbeat_thread and self._heartbeat_thread.is_alive():
            self._heartbeat_thread.join(timeout=2)

        if self._registered:
            try:
                self._deregister_instance()
            except Exception as exc:
                LOGGER.warning("Failed to deregister ai-service from Nacos: %s", exc)
            finally:
                self._registered = False

    def _request(self, method, path, params=None, data=None, headers=None, timeout=10, retry=True):
        request_params = dict(params or {})
        token = self._ensure_token()
        if token:
            request_params["accessToken"] = token

        response = requests.request(
            method=method,
            url=f"{self.base_url}{path}",
            params=request_params,
            data=data,
            headers=headers,
            timeout=timeout,
        )
        if response.status_code == 403 and retry and (self.username or self.password):
            with self._token_lock:
                self._access_token = ""
                self._token_expire_at = 0
            return self._request(
                method=method,
                path=path,
                params=params,
                data=data,
                headers=headers,
                timeout=timeout,
                retry=False,
            )
        response.raise_for_status()
        return response

    def _ensure_token(self):
        if not (self.username or self.password):
            return ""

        with self._token_lock:
            if self._access_token and time.time() < self._token_expire_at - 60:
                return self._access_token

            response = requests.post(
                f"{self.base_url}/nacos/v1/auth/login",
                data={
                    "username": self.username,
                    "password": self.password,
                },
                timeout=10,
            )
            response.raise_for_status()
            payload = response.json()
            self._access_token = payload.get("accessToken", "")
            token_ttl = int(payload.get("tokenTtl") or 18000)
            self._token_expire_at = time.time() + max(token_ttl, 60)
            return self._access_token

    def _ensure_nacos_success(self, response):
        if "application/json" not in response.headers.get("Content-Type", ""):
            return response.text

        payload = response.json()
        code = payload.get("code")
        if code not in (0, "0", 10200, "10200", None):
            raise RuntimeError(payload.get("message") or json.dumps(payload, ensure_ascii=False))
        return payload.get("data")

    def _get_config_content(self):
        try:
            response = self._request(
                "GET",
                "/nacos/v1/cs/configs",
                params={
                    "dataId": self.config_data_id,
                    "group": self.group,
                    "tenant": self.namespace,
                },
                timeout=10,
            )
        except requests.HTTPError as exc:
            if exc.response is not None and exc.response.status_code == 404:
                return None
            raise
        return response.text

    def _refresh_config(self):
        content = self._get_config_content()
        if content is None:
            return

        self._config_md5 = self._md5(content)
        changed_keys = Config.apply_text(content, allowed_keys=Config.remote_managed_keys())
        if changed_keys:
            LOGGER.warning("Applied Nacos config refresh for keys: %s", ", ".join(sorted(changed_keys)))

    def _config_listener_loop(self):
        while not self._stop_event.is_set():
            try:
                response = self._request(
                    "POST",
                    "/nacos/v1/cs/configs/listener",
                    data={"Listening-Configs": self._build_listening_configs()},
                    headers={"Long-Pulling-Timeout": str(self.poll_timeout_ms)},
                    timeout=(self.poll_timeout_ms / 1000) + 10,
                )
                if response.text.strip():
                    self._refresh_config()
            except Exception as exc:
                LOGGER.warning("Nacos config listener error: %s", exc)
                if self._stop_event.wait(self.poll_interval_seconds):
                    return

    def _build_listening_configs(self):
        return f"{self.config_data_id}\x02{self.group}\x02{self._config_md5}\x02{self.namespace}\x01"

    def _register_instance(self):
        self._instance_ip = Config.NACOS_SERVICE_IP or self._resolve_service_ip()
        configured_port = int(Config.NACOS_SERVICE_PORT or 0)
        self._instance_port = configured_port or int(Config.AI_SERVICE_PORT)
        self._instance_metadata = dict(Config.NACOS_SERVICE_METADATA or {})
        self._instance_metadata.setdefault("healthPath", "/health")
        self._instance_metadata.setdefault("routePrefix", "/ai-api")

        response = self._request(
            "POST",
            "/nacos/v2/ns/instance",
            params={
                "namespaceId": self.namespace,
                "groupName": self.group,
                "serviceName": self.service_name,
                "ip": self._instance_ip,
                "port": str(self._instance_port),
                "clusterName": self.cluster_name,
                "weight": str(Config.NACOS_SERVICE_WEIGHT),
                "ephemeral": str(self.ephemeral).lower(),
                "enabled": "true",
                "healthy": "true",
                "metadata": json.dumps(self._instance_metadata, ensure_ascii=False),
            },
            timeout=10,
        )
        self._ensure_nacos_success(response)
        self._registered = True

    def _heartbeat_loop(self):
        while not self._stop_event.wait(self.heartbeat_interval_seconds):
            if not self._registered:
                continue
            try:
                self._send_heartbeat()
            except Exception as exc:
                LOGGER.warning("Nacos heartbeat error: %s", exc)

    def _send_heartbeat(self):
        beat = {
            "serviceName": self.service_name,
            "ip": self._instance_ip,
            "port": self._instance_port,
            "cluster": self.cluster_name,
            "weight": float(Config.NACOS_SERVICE_WEIGHT),
            "ephemeral": self.ephemeral,
            "metadata": self._instance_metadata,
        }
        response = self._request(
            "PUT",
            "/nacos/v1/ns/instance/beat",
            params={
                "namespaceId": self.namespace,
                "groupName": self.group,
                "serviceName": self.service_name,
                "ip": self._instance_ip,
                "port": str(self._instance_port),
                "clusterName": self.cluster_name,
                "ephemeral": str(self.ephemeral).lower(),
                "beat": json.dumps(beat, ensure_ascii=False),
            },
            timeout=10,
        )
        self._ensure_nacos_success(response)

    def _deregister_instance(self):
        response = self._request(
            "DELETE",
            "/nacos/v2/ns/instance",
            params={
                "namespaceId": self.namespace,
                "groupName": self.group,
                "serviceName": self.service_name,
                "ip": self._instance_ip,
                "port": str(self._instance_port),
                "clusterName": self.cluster_name,
                "ephemeral": str(self.ephemeral).lower(),
            },
            timeout=10,
        )
        self._ensure_nacos_success(response)

    def _resolve_service_ip(self):
        server_host = _extract_host(Config.NACOS_SERVER_ADDR)
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
                sock.connect((server_host, 80))
                return sock.getsockname()[0]
        except OSError:
            try:
                return socket.gethostbyname(socket.gethostname())
            except OSError:
                return "127.0.0.1"

    @staticmethod
    def _md5(content):
        return hashlib.md5((content or "").encode("utf-8")).hexdigest()



