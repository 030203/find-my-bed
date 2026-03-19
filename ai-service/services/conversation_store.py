import json

import redis

from runtime_config import Config


class InMemoryConversationStore:
    def __init__(self):
        self._store = {}
        self.backend_name = "memory"

    def get_history(self, conversation_id):
        return list(self._store.get(conversation_id, []))

    def save_history(self, conversation_id, history):
        self._store[conversation_id] = list(history)


class RedisConversationStore:
    def __init__(self):
        self.backend_name = "redis"
        self.client = redis.Redis(
            host=Config.REDIS_HOST,
            port=Config.REDIS_PORT,
            db=Config.REDIS_DB,
            password=Config.REDIS_PASSWORD or None,
            decode_responses=True,
            socket_timeout=Config.REDIS_SOCKET_TIMEOUT,
            socket_connect_timeout=Config.REDIS_SOCKET_TIMEOUT,
        )
        self.client.ping()

    def get_history(self, conversation_id):
        payload = self.client.get(self._key(conversation_id))
        if not payload:
            return []

        history = json.loads(payload)
        if isinstance(history, list):
            return history
        return []

    def save_history(self, conversation_id, history):
        self.client.setex(
            self._key(conversation_id),
            Config.REDIS_SESSION_TTL_SECONDS,
            json.dumps(history, ensure_ascii=False),
        )

    def _key(self, conversation_id):
        return f"{Config.REDIS_SESSION_PREFIX}{conversation_id}"


def build_conversation_store():
    try:
        return RedisConversationStore(), None
    except Exception as exc:
        return InMemoryConversationStore(), exc

