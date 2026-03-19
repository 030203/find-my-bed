import json
import os
import threading
from pathlib import Path


def _load_local_env():
    env_path = Path(__file__).resolve().parent / ".env"
    if not env_path.exists():
        return

    for raw_line in env_path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue

        key, value = line.split("=", 1)
        os.environ.setdefault(key.strip(), value.strip().strip("\"'"))


def _to_bool(value):
    if isinstance(value, bool):
        return value
    return str(value).strip().lower() in {"1", "true", "yes", "y", "on"}


def _coerce_value(raw_value, default):
    if raw_value is None:
        return default

    if isinstance(raw_value, str):
        normalized = raw_value.strip().strip("\"'")
    else:
        normalized = raw_value

    try:
        if isinstance(default, bool):
            return _to_bool(normalized)
        if isinstance(default, int) and not isinstance(default, bool):
            return int(str(normalized).strip())
        if isinstance(default, float):
            return float(str(normalized).strip())
        if isinstance(default, dict):
            if isinstance(normalized, dict):
                return normalized
            if isinstance(normalized, str) and normalized:
                parsed = json.loads(normalized)
                if isinstance(parsed, dict):
                    return parsed
            return dict(default)
    except (TypeError, ValueError, json.JSONDecodeError):
        return default

    return normalized


def parse_key_value_text(text):
    stripped = (text or "").strip()
    if not stripped:
        return {}

    if stripped.startswith("{"):
        try:
            parsed = json.loads(stripped)
        except json.JSONDecodeError:
            parsed = None
        if isinstance(parsed, dict):
            return {str(key).strip(): value for key, value in parsed.items()}

    mapping = {}
    for raw_line in text.splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue

        separator = "=" if "=" in line else ":" if ":" in line else None
        if not separator:
            continue

        key, value = line.split(separator, 1)
        normalized_key = key.strip()
        if not normalized_key:
            continue
        mapping[normalized_key] = value.strip().strip("\"'")
    return mapping


_load_local_env()


class ConfigMeta(type):
    def __getattr__(cls, name):
        try:
            return cls.get(name)
        except KeyError as exc:
            raise AttributeError(name) from exc


class Config(metaclass=ConfigMeta):
    DEFAULTS = {
        "AI_SERVICE_HOST": "0.0.0.0",
        "AI_SERVICE_PORT": 5001,
        "AI_SERVICE_DEBUG": False,
        "MYSQL_HOST": "127.0.0.1",
        "MYSQL_PORT": 3306,
        "MYSQL_USER": "root",
        "MYSQL_PASSWORD": "",
        "MYSQL_DATABASE": "booking_system",
        "AI_PROVIDER": "",
        "DEEPSEEK_API_KEY": "",
        "DEEPSEEK_MODEL": "deepseek-chat",
        "DEEPSEEK_BASE_URL": "https://api.deepseek.com",
        "QWEN_API_KEY": "",
        "QWEN_MODEL": "qwen-plus",
        "QWEN_BASE_URL": "https://dashscope.aliyuncs.com/compatible-mode/v1",
        "REDIS_HOST": "127.0.0.1",
        "REDIS_PORT": 6379,
        "REDIS_DB": 0,
        "REDIS_PASSWORD": "",
        "REDIS_SESSION_PREFIX": "roomos:ai:conversation:",
        "REDIS_SESSION_TTL_SECONDS": 86400,
        "REDIS_SOCKET_TIMEOUT": 5,
        "OPEN_METEO_GEOCODING_URL": "https://geocoding-api.open-meteo.com/v1/search",
        "OPEN_METEO_FORECAST_URL": "https://api.open-meteo.com/v1/forecast",
        "NOMINATIM_SEARCH_URL": "https://nominatim.openstreetmap.org/search",
        "TOOL_CALL_MAX_STEPS": 6,
        "AI_HISTORY_LIMIT": 12,
        "SQL_MAX_ROWS": 50,
        "HTTP_TIMEOUT": 20,
        "HTTP_USER_AGENT": "RoomOS-AI-Service/1.0",
        "LLM_HTTP_TIMEOUT": 45,
        "NACOS_ENABLED": False,
        "NACOS_DISCOVERY_ENABLED": False,
        "NACOS_CONFIG_ENABLED": False,
        "NACOS_SERVER_ADDR": "127.0.0.1:8848",
        "NACOS_NAMESPACE": "public",
        "NACOS_GROUP": "DEFAULT_GROUP",
        "NACOS_USERNAME": "",
        "NACOS_PASSWORD": "",
        "NACOS_CONFIG_DATA_ID": "roomos-ai-service.properties",
        "NACOS_CONFIG_POLL_TIMEOUT_MS": 30000,
        "NACOS_CONFIG_POLL_INTERVAL_SECONDS": 1,
        "NACOS_SERVICE_NAME": "roomos-ai-service",
        "NACOS_SERVICE_IP": "",
        "NACOS_SERVICE_PORT": 0,
        "NACOS_CLUSTER_NAME": "DEFAULT",
        "NACOS_SERVICE_WEIGHT": 1.0,
        "NACOS_EPHEMERAL": True,
        "NACOS_DISCOVERY_HEARTBEAT_INTERVAL_SECONDS": 5,
        "NACOS_SERVICE_METADATA": {},
    }
    STARTUP_ONLY_KEYS = {
        "AI_SERVICE_HOST",
        "AI_SERVICE_PORT",
        "AI_SERVICE_DEBUG",
        "NACOS_ENABLED",
        "NACOS_DISCOVERY_ENABLED",
        "NACOS_CONFIG_ENABLED",
        "NACOS_SERVER_ADDR",
        "NACOS_NAMESPACE",
        "NACOS_GROUP",
        "NACOS_USERNAME",
        "NACOS_PASSWORD",
        "NACOS_CONFIG_DATA_ID",
        "NACOS_CONFIG_POLL_TIMEOUT_MS",
        "NACOS_CONFIG_POLL_INTERVAL_SECONDS",
        "NACOS_SERVICE_NAME",
        "NACOS_SERVICE_IP",
        "NACOS_SERVICE_PORT",
        "NACOS_CLUSTER_NAME",
        "NACOS_SERVICE_WEIGHT",
        "NACOS_EPHEMERAL",
        "NACOS_DISCOVERY_HEARTBEAT_INTERVAL_SECONDS",
        "NACOS_SERVICE_METADATA",
    }
    ENV_ONLY_KEYS = {
        "MYSQL_PASSWORD",
        "REDIS_PASSWORD",
        "DEEPSEEK_API_KEY",
        "QWEN_API_KEY",
    }
    RUNTIME_KEYS = set(DEFAULTS) - STARTUP_ONLY_KEYS
    REMOTE_MANAGED_KEYS = RUNTIME_KEYS - ENV_ONLY_KEYS
    _lock = threading.RLock()
    _values = {}
    _version = 0

    @classmethod
    def reload_from_env(cls):
        values = {}
        for key, default in cls.DEFAULTS.items():
            raw_value = os.getenv(key)
            values[key] = _coerce_value(raw_value, default) if raw_value is not None else default

        with cls._lock:
            cls._values = values
            cls._version += 1
        return dict(values)

    @classmethod
    def apply_mapping(cls, mapping, allowed_keys=None):
        if not mapping:
            return []

        changed_keys = []
        with cls._lock:
            updated = dict(cls._values)
            for key, raw_value in mapping.items():
                normalized_key = str(key).strip()
                if not normalized_key:
                    continue
                if allowed_keys is not None and normalized_key not in allowed_keys:
                    continue

                default = cls.DEFAULTS.get(normalized_key)
                value = _coerce_value(raw_value, default) if default is not None else raw_value
                if updated.get(normalized_key) != value:
                    updated[normalized_key] = value
                    changed_keys.append(normalized_key)

            if changed_keys:
                cls._values = updated
                cls._version += 1
        return changed_keys

    @classmethod
    def apply_text(cls, text, allowed_keys=None):
        return cls.apply_mapping(parse_key_value_text(text), allowed_keys=allowed_keys)

    @classmethod
    def get(cls, name, default=None):
        with cls._lock:
            if name in cls._values:
                return cls._values[name]

        if name in cls.DEFAULTS:
            return cls.DEFAULTS[name]
        if default is not None:
            return default
        raise KeyError(name)

    @classmethod
    def version(cls):
        with cls._lock:
            return cls._version

    @classmethod
    def runtime_keys(cls):
        return set(cls.RUNTIME_KEYS)

    @classmethod
    def remote_managed_keys(cls):
        return set(cls.REMOTE_MANAGED_KEYS)


Config.reload_from_env()
