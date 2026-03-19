import json
import re

import requests

from runtime_config import Config


class ToolService:
    WEATHER_CODE_MAP = {
        0: "晴",
        1: "大部晴朗",
        2: "多云",
        3: "阴",
        45: "雾",
        48: "雾凇",
        51: "小毛毛雨",
        53: "毛毛雨",
        55: "强毛毛雨",
        61: "小雨",
        63: "中雨",
        65: "大雨",
        71: "小雪",
        73: "中雪",
        75: "大雪",
        80: "阵雨",
        81: "强阵雨",
        82: "暴雨阵雨",
        95: "雷暴",
    }

    def __init__(self, db_service, llm_client):
        self.db_service = db_service
        self.llm_client = llm_client

    def get_tool_definitions(self):
        return [
            {
                "type": "function",
                "function": {
                    "name": "get_weather",
                    "description": "查询指定城市或地址未来几天的天气。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "location": {"type": "string", "description": "城市名或地址，例如 深圳龙华区。"},
                            "days": {"type": "integer", "description": "返回天数，默认 3。"},
                        },
                        "required": ["location"],
                    },
                },
            },
            {
                "type": "function",
                "function": {
                    "name": "geocode_address",
                    "description": "把地址或地名解析为经纬度和标准化地址。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "query": {"type": "string", "description": "地址、地标或城市名。"},
                            "limit": {"type": "integer", "description": "返回条数，默认 5。"},
                        },
                        "required": ["query"],
                    },
                },
            },
            {
                "type": "function",
                "function": {
                    "name": "generate_sql",
                    "description": "把业务问题转换成只读 MySQL 查询语句。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "question": {"type": "string", "description": "业务问题，例如 最近 7 天订单金额最高的民宿。"},
                        },
                        "required": ["question"],
                    },
                },
            },
            {
                "type": "function",
                "function": {
                    "name": "query_sql",
                    "description": "执行只读 SQL 并返回结果。只允许 SELECT/WITH/SHOW/DESCRIBE/EXPLAIN。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "sql": {"type": "string", "description": "待读取的 SQL。"},
                        },
                        "required": ["sql"],
                    },
                },
            },
            {
                "type": "function",
                "function": {
                    "name": "find_available_properties",
                    "description": "按城市、入住日期和人数查询估算可订房源。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "city": {"type": "string", "description": "城市名，可选。"},
                            "check_in_date": {"type": "string", "description": "入住日期，格式 YYYY-MM-DD。"},
                            "check_out_date": {"type": "string", "description": "离店日期，格式 YYYY-MM-DD。"},
                            "guests": {"type": "integer", "description": "入住人数，默认 2。"},
                            "limit": {"type": "integer", "description": "返回条数，默认 5。"},
                        },
                        "required": [],
                    },
                },
            },
            {
                "type": "function",
                "function": {
                    "name": "get_business_overview",
                    "description": "获取民宿业务经营概览，如订单数、营收、评分和热门房源。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "date_from": {"type": "string", "description": "开始日期，格式 YYYY-MM-DD。"},
                            "date_to": {"type": "string", "description": "结束日期，格式 YYYY-MM-DD。"},
                            "merchant_id": {"type": "integer", "description": "商家 ID，可选。"},
                            "property_id": {"type": "integer", "description": "民宿 ID，可选。"},
                        },
                        "required": [],
                    },
                },
            },
        ]

    def handle_tool_call(self, tool_call):
        function_call = tool_call.get("function", {})
        name = function_call.get("name")
        arguments = function_call.get("arguments") or "{}"

        try:
            parsed_arguments = json.loads(arguments)
        except json.JSONDecodeError:
            parsed_arguments = {}

        try:
            result = self._dispatch(name, parsed_arguments)
            return {
                "ok": True,
                "tool": name,
                "result": result,
            }
        except Exception as exc:
            return {
                "ok": False,
                "tool": name,
                "error": str(exc),
            }

    def _dispatch(self, name, arguments):
        if name == "get_weather":
            return self.get_weather(**arguments)
        if name == "geocode_address":
            return self.geocode_address(**arguments)
        if name == "generate_sql":
            return self.generate_sql(**arguments)
        if name == "query_sql":
            return self.query_sql(**arguments)
        if name == "find_available_properties":
            return self.find_available_properties(**arguments)
        if name == "get_business_overview":
            return self.get_business_overview(**arguments)
        raise ValueError(f"未知工具：{name}")

    def get_weather(self, location, days=3):
        places = self.geocode_address(location, limit=1)["matches"]
        if not places:
            raise ValueError("没有找到对应地点，无法查询天气。")

        place = places[0]
        params = {
            "latitude": place["latitude"],
            "longitude": place["longitude"],
            "current": "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m",
            "daily": "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max",
            "forecast_days": max(1, min(int(days or 3), 7)),
            "timezone": "auto",
        }
        response = requests.get(
            Config.OPEN_METEO_FORECAST_URL,
            params=params,
            headers={"User-Agent": Config.HTTP_USER_AGENT},
            timeout=Config.HTTP_TIMEOUT,
        )
        response.raise_for_status()
        payload = response.json()

        current = payload.get("current", {})
        daily = payload.get("daily", {})
        forecast = []
        for index, date_value in enumerate(daily.get("time", [])):
            code = daily.get("weather_code", [None])[index]
            forecast.append(
                {
                    "date": date_value,
                    "weather": self.WEATHER_CODE_MAP.get(code, f"天气代码 {code}"),
                    "temp_max": daily.get("temperature_2m_max", [None])[index],
                    "temp_min": daily.get("temperature_2m_min", [None])[index],
                    "precipitation_probability_max": daily.get("precipitation_probability_max", [None])[index],
                }
            )

        return {
            "location": place,
            "current": {
                "temperature": current.get("temperature_2m"),
                "apparent_temperature": current.get("apparent_temperature"),
                "humidity": current.get("relative_humidity_2m"),
                "wind_speed": current.get("wind_speed_10m"),
                "weather": self.WEATHER_CODE_MAP.get(current.get("weather_code"), current.get("weather_code")),
            },
            "forecast": forecast,
            "source": "open-meteo",
        }

    def geocode_address(self, query, limit=5):
        normalized_limit = max(1, min(int(limit or 5), 10))

        response = requests.get(
            Config.NOMINATIM_SEARCH_URL,
            params={
                "q": query,
                "format": "jsonv2",
                "addressdetails": 1,
                "limit": normalized_limit,
            },
            headers={"User-Agent": Config.HTTP_USER_AGENT},
            timeout=Config.HTTP_TIMEOUT,
        )
        response.raise_for_status()
        payload = response.json()

        matches = []
        for item in payload:
            matches.append(
                {
                    "display_name": item.get("display_name"),
                    "latitude": float(item["lat"]),
                    "longitude": float(item["lon"]),
                    "type": item.get("type"),
                }
            )
        return {"query": query, "matches": matches, "source": "nominatim"}

    def generate_sql(self, question):
        schema = self.db_service.get_schema_overview()
        system_prompt = (
            "你是 MySQL 只读 SQL 专家。"
            " 只输出 JSON，对象格式必须是 {\"sql\": string, \"explanation\": string, \"tables\": string[]}。"
            " 只能生成 SELECT/WITH/SHOW/DESCRIBE/EXPLAIN 语句。"
            " 优先使用 booking_system 库中的真实表。"
        )
        user_prompt = (
            f"数据库概要如下：\n{schema}\n\n"
            f"业务问题：{question}\n\n"
            "请生成一条安全、可读、适合直接执行的 MySQL 查询。"
        )
        raw = self.llm_client.chat(
            system_prompt,
            user_prompt,
            response_format={"type": "json_object"},
            temperature=0,
        )
        parsed = self._parse_json_object(raw)
        sql = (parsed.get("sql") or "").strip()
        if not sql:
            raise ValueError("模型没有生成 SQL。")
        self.db_service.execute_readonly(sql)
        return {
            "sql": sql,
            "explanation": parsed.get("explanation", ""),
            "tables": parsed.get("tables", []),
        }

    def query_sql(self, sql):
        return self.db_service.execute_readonly(sql)

    def find_available_properties(
        self,
        city=None,
        check_in_date=None,
        check_out_date=None,
        guests=2,
        limit=5,
    ):
        return self.db_service.find_available_properties(
            city=city,
            check_in_date=check_in_date,
            check_out_date=check_out_date,
            guests=int(guests or 2),
            limit=int(limit or 5),
        )

    def get_business_overview(self, date_from=None, date_to=None, merchant_id=None, property_id=None):
        return self.db_service.get_business_overview(
            date_from=date_from,
            date_to=date_to,
            merchant_id=merchant_id,
            property_id=property_id,
        )

    def _parse_json_object(self, text):
        cleaned = text.strip()
        try:
            return json.loads(cleaned)
        except json.JSONDecodeError:
            match = re.search(r"\{.*\}", cleaned, re.S)
            if match:
                return json.loads(match.group(0))
        raise ValueError("无法解析模型返回的 JSON。")

