import json
import uuid

from runtime_config import Config
from services.db_service import DBService
from services.deepseek_client import DeepSeekClient
from services.tool_service import ToolService


class AIService:
    def __init__(self):
        self.db_service = DBService()
        self.client = None
        self.tool_service = None
        self._init_error = None
        self._conversations = {}
        self._bootstrap()

    def _bootstrap(self):
        if self.client and self.tool_service:
            return
        try:
            self.client = DeepSeekClient()
            self.tool_service = ToolService(self.db_service, self.client)
            self._init_error = None
        except Exception as exc:
            self.client = None
            self.tool_service = None
            self._init_error = exc

    def default_suggestions(self):
        return [
            "最近 7 天订单和成交金额怎么样？",
            "帮我找一下深圳可订的民宿，2 个人住，明天入住后天离店。",
            "把“最近付款成功率”生成 SQL，再执行给我看。",
            "上海这周天气怎么样，适合出行吗？",
            "评分最高的 5 家民宿是哪几家？",
            "给我一个最近 30 天的经营概览。",
        ]

    def chat(self, message, conversation_id=None, user_context=None):
        conversation_id = conversation_id or uuid.uuid4().hex
        history = list(self._conversations.get(conversation_id, []))

        self._bootstrap()
        if self._init_error:
            return {
                "conversationId": conversation_id,
                "answer": f"AI 服务已启动，但模型配置仍有问题：{self._init_error}",
                "source": "service-error",
            }

        system_prompt = self._build_system_prompt(user_context)
        messages = [{"role": "system", "content": system_prompt}, *history]
        user_message = {"role": "user", "content": message}
        messages.append(user_message)

        try:
            result = self._run_tool_loop(messages)
        except Exception as exc:
            result = {
                "answer": f"这次请求没有成功完成，原因是：{exc}",
                "source": "service-error",
            }

        history.extend(
            [
                user_message,
                {"role": "assistant", "content": result["answer"]},
            ]
        )
        self._conversations[conversation_id] = history[-(Config.AI_HISTORY_LIMIT * 2) :]

        return {
            "conversationId": conversation_id,
            "answer": result["answer"],
            "source": result["source"],
        }

    def _build_system_prompt(self, user_context):
        user_summary = "当前用户未登录。"
        if user_context:
            user_summary = (
                f"当前用户：id={user_context.get('id')}, "
                f"username={user_context.get('username')}, "
                f"role={user_context.get('role')}。"
            )

        schema = self.db_service.get_schema_overview()
        return (
            "你是 FindMyBed 的民宿业务数据助手，请始终使用中文回复。\n"
            f"{user_summary}\n"
            "当问题涉及订单、房源、支付、评分、天气、地址或 SQL 时，优先调用工具后再回答。\n"
            "如果用户明确要求“生成 SQL”，先调用 generate_sql；如果还要求“查询”或“执行”，再调用 query_sql。\n"
            "find_available_properties 适合回答可订民宿、房态和入住建议。\n"
            "get_business_overview 适合回答经营概览、营收、订单趋势、热门房源。\n"
            "如果工具返回错误，要向用户解释原因并给出下一步建议。\n"
            "回答尽量简洁，但要有结论；当引用工具结果时，请说明依据来自数据库或外部接口。\n"
            f"数据库概要：\n{schema}"
        )

    def _run_tool_loop(self, messages):
        tools = self.tool_service.get_tool_definitions()
        tool_used = False

        for _ in range(Config.TOOL_CALL_MAX_STEPS):
            assistant_message = self.client.complete(messages, tools=tools)
            messages.append(self._normalize_assistant_message(assistant_message))

            tool_calls = assistant_message.get("tool_calls") or []
            if not tool_calls:
                answer = self.client.extract_text(assistant_message).strip()
                if not answer:
                    answer = "服务已响应，但没有生成可展示的内容。"
                source = f"{self.client.provider}{'+tools' if tool_used else ''}"
                return {"answer": answer, "source": source}

            tool_used = True
            for tool_call in tool_calls:
                result = self.tool_service.handle_tool_call(tool_call)
                messages.append(
                    {
                        "role": "tool",
                        "tool_call_id": tool_call["id"],
                        "content": json.dumps(result, ensure_ascii=False),
                    }
                )

        final_message = self.client.complete(messages)
        answer = self.client.extract_text(final_message).strip() or "工具调用次数已达上限，但暂时没有得到最终结论。"
        return {"answer": answer, "source": f"{self.client.provider}+tools"}

    def _normalize_assistant_message(self, assistant_message):
        normalized = {
            "role": "assistant",
            "content": assistant_message.get("content") or "",
        }
        if assistant_message.get("tool_calls"):
            normalized["tool_calls"] = assistant_message["tool_calls"]
        return normalized

