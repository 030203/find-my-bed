import os

from flask import Flask, jsonify, request
from flask_cors import CORS

from nacos_manager import NacosManager
from runtime_config import Config
from services.ai_service import AIService


nacos_manager = NacosManager()
nacos_manager.bootstrap_config()

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

ai_service = AIService()


@app.get("/health")
def health():
    return jsonify(
        {
            "status": "ok",
            "service": Config.NACOS_SERVICE_NAME,
            "configVersion": Config.version(),
        }
    )


@app.get("/suggestions")
def suggestions():
    return jsonify(ai_service.default_suggestions())


@app.post("/chat")
def chat():
    payload = request.get_json(silent=True) or {}
    message = (payload.get("message") or "").strip()
    conversation_id = payload.get("conversationId")
    user_context = payload.get("userContext") or {}

    if not message:
        return jsonify({"error": "message is required"}), 400

    return jsonify(ai_service.chat(message, conversation_id, user_context))


def _should_start_background_tasks():
    return not Config.AI_SERVICE_DEBUG or os.getenv("WERKZEUG_RUN_MAIN") == "true"


if __name__ == "__main__":
    if _should_start_background_tasks():
        nacos_manager.start()

    app.run(
        host=Config.AI_SERVICE_HOST,
        port=int(Config.AI_SERVICE_PORT),
        debug=bool(Config.AI_SERVICE_DEBUG),
    )
