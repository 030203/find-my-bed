import json

import requests

from runtime_config import Config


class DeepSeekClient:
    def __init__(self):
        self.provider = ""

    def _resolve_provider(self):
        preferred = (Config.AI_PROVIDER or "").strip().lower()

        providers = []
        if preferred == "qwen":
            providers.extend(
                [
                    ("qwen", Config.QWEN_API_KEY, Config.QWEN_MODEL, Config.QWEN_BASE_URL),
                    ("deepseek", Config.DEEPSEEK_API_KEY, Config.DEEPSEEK_MODEL, Config.DEEPSEEK_BASE_URL),
                ]
            )
        else:
            providers.extend(
                [
                    ("deepseek", Config.DEEPSEEK_API_KEY, Config.DEEPSEEK_MODEL, Config.DEEPSEEK_BASE_URL),
                    ("qwen", Config.QWEN_API_KEY, Config.QWEN_MODEL, Config.QWEN_BASE_URL),
                ]
            )

        for provider, api_key, model, base_url in providers:
            if api_key:
                return provider, api_key, model, base_url

        raise RuntimeError("No LLM API key configured. Set DEEPSEEK_API_KEY or QWEN_API_KEY.")

    def _chat_url(self, base_url):
        normalized_base_url = base_url.rstrip("/")
        if normalized_base_url.endswith("/chat/completions"):
            return normalized_base_url
        return f"{normalized_base_url}/chat/completions"

    def _post(self, payload, api_key, base_url):
        response = requests.post(
            self._chat_url(base_url),
            headers={
                "Authorization": f"Bearer {api_key}",
                "Content-Type": "application/json",
            },
            json=payload,
            timeout=Config.LLM_HTTP_TIMEOUT,
        )
        response.raise_for_status()
        data = response.json()
        return data["choices"][0]["message"]

    def complete(self, messages, tools=None, response_format=None, temperature=0.2):
        self.provider, api_key, model, base_url = self._resolve_provider()
        payload = {
            "model": model,
            "messages": messages,
            "temperature": temperature,
        }
        if tools:
            payload["tools"] = tools
            payload["tool_choice"] = "auto"
        if response_format:
            payload["response_format"] = response_format
        return self._post(payload, api_key, base_url)

    def chat(self, system_prompt, user_prompt, response_format=None, temperature=0.2):
        message = self.complete(
            [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            response_format=response_format,
            temperature=temperature,
        )
        return self.extract_text(message)

    @staticmethod
    def extract_text(message):
        content = message.get("content", "")
        if isinstance(content, str):
            return content
        if isinstance(content, list):
            parts = []
            for item in content:
                if isinstance(item, dict) and item.get("type") == "text":
                    parts.append(item.get("text", ""))
                elif isinstance(item, str):
                    parts.append(item)
            return "\n".join(part for part in parts if part).strip()
        if content is None:
            return ""
        return json.dumps(content, ensure_ascii=False)
