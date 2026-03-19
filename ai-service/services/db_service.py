import re

import pymysql
from pymysql.cursors import DictCursor

from runtime_config import Config


class DBService:
    CORE_TABLES = {
        "users": [
            "id",
            "username",
            "role",
            "email",
            "phone",
            "status",
            "created_at",
        ],
        "merchants": [
            "id",
            "user_id",
            "merchant_name",
            "contact_name",
            "contact_phone",
            "city",
            "status",
            "rating",
        ],
        "properties": [
            "id",
            "merchant_id",
            "property_name",
            "property_type",
            "province",
            "city",
            "district",
            "address",
            "rating",
            "total_reviews",
            "price_range_min",
            "price_range_max",
            "status",
            "is_featured",
        ],
        "room_types": [
            "id",
            "property_id",
            "type_name",
            "max_occupancy",
            "bed_type",
            "room_size",
            "base_price",
            "status",
        ],
        "rooms": [
            "id",
            "property_id",
            "room_type_id",
            "room_number",
            "floor_number",
            "status",
        ],
        "bookings": [
            "id",
            "booking_number",
            "user_id",
            "property_id",
            "room_type_id",
            "room_id",
            "check_in_date",
            "check_out_date",
            "nights",
            "number_of_guests",
            "total_amount",
            "paid_amount",
            "status",
            "payment_status",
            "created_at",
        ],
        "payments": [
            "id",
            "payment_number",
            "booking_id",
            "payment_method",
            "payment_type",
            "amount",
            "status",
            "created_at",
        ],
        "reviews": [
            "id",
            "booking_id",
            "user_id",
            "property_id",
            "overall_rating",
            "cleanliness_rating",
            "service_rating",
            "location_rating",
            "value_rating",
            "comment",
            "status",
            "created_at",
        ],
    }

    def __init__(self):
        self._schema_cache = None
        self._schema_cache_database = None

    def _connect(self):
        return pymysql.connect(
            host=Config.MYSQL_HOST,
            port=Config.MYSQL_PORT,
            user=Config.MYSQL_USER,
            password=Config.MYSQL_PASSWORD,
            database=Config.MYSQL_DATABASE,
            charset="utf8mb4",
            cursorclass=DictCursor,
            autocommit=True,
        )

    def ping(self):
        with self._connect() as connection:
            with connection.cursor() as cursor:
                cursor.execute("SELECT 1 AS ok")
                return cursor.fetchone()

    def query(self, sql, params=None, max_rows=None):
        limit = max_rows or Config.SQL_MAX_ROWS
        with self._connect() as connection:
            with connection.cursor() as cursor:
                cursor.execute(sql, params or ())
                rows = cursor.fetchmany(limit + 1)
                has_more = len(rows) > limit
                trimmed = rows[:limit]
                return {
                    "rows": self._serialize_rows(trimmed),
                    "row_count": len(trimmed),
                    "truncated": has_more,
                }

    def execute_readonly(self, sql, params=None):
        sanitized = self._sanitize_sql(sql)
        self._ensure_readonly(sanitized)
        result = self.query(sanitized, params=params, max_rows=Config.SQL_MAX_ROWS)
        result["sql"] = sanitized
        return result

    def get_schema_overview(self):
        current_database = Config.MYSQL_DATABASE
        if self._schema_cache and self._schema_cache_database == current_database:
            return self._schema_cache

        try:
            table_names = tuple(self.CORE_TABLES.keys())
            placeholders = ",".join(["%s"] * len(table_names))
            sql = f"""
                SELECT table_name, column_name
                FROM information_schema.columns
                WHERE table_schema = %s
                  AND table_name IN ({placeholders})
                ORDER BY table_name, ordinal_position
            """
            params = (current_database, *table_names)
            result = self.query(sql, params=params, max_rows=500)
            grouped = {}
            for row in result["rows"]:
                grouped.setdefault(row["table_name"], []).append(row["column_name"])
            if grouped:
                lines = []
                for table_name in table_names:
                    columns = grouped.get(table_name)
                    if columns:
                        lines.append(f"{table_name}: {', '.join(columns)}")
                self._schema_cache = "\n".join(lines)
                self._schema_cache_database = current_database
                return self._schema_cache
        except Exception:
            pass

        fallback = []
        for table_name, columns in self.CORE_TABLES.items():
            fallback.append(f"{table_name}: {', '.join(columns)}")
        self._schema_cache = "\n".join(fallback)
        self._schema_cache_database = current_database
        return self._schema_cache
    def find_available_properties(self, city=None, check_in_date=None, check_out_date=None, guests=2, limit=5):
        where_clauses = ["p.status = 'APPROVED'", "rt.status = 'ACTIVE'"]
        params = []

        if city:
            where_clauses.append("p.city = %s")
            params.append(city)

        if check_in_date and check_out_date:
            overlap_join = """
                LEFT JOIN bookings b
                    ON b.room_type_id = rt.id
                   AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
                   AND b.check_in_date < %s
                   AND b.check_out_date > %s
            """
            params.extend([check_out_date, check_in_date])
        else:
            overlap_join = "LEFT JOIN bookings b ON 1 = 0"

        sql = f"""
            SELECT
                p.id,
                p.property_name,
                p.city,
                p.district,
                p.address,
                p.rating,
                p.total_reviews,
                MIN(rt.base_price) AS lowest_price,
                MAX(rt.max_occupancy) AS max_guests,
                COUNT(DISTINCT r.id) AS total_rooms,
                GREATEST(COUNT(DISTINCT r.id) - COUNT(DISTINCT b.id), 0) AS estimated_available_rooms
            FROM properties p
            JOIN room_types rt
              ON rt.property_id = p.id
            LEFT JOIN rooms r
              ON r.room_type_id = rt.id
             AND r.status = 'AVAILABLE'
            {overlap_join}
            WHERE {' AND '.join(where_clauses)}
            GROUP BY p.id, p.property_name, p.city, p.district, p.address, p.rating, p.total_reviews
            HAVING MAX(rt.max_occupancy) >= %s
               AND GREATEST(COUNT(DISTINCT r.id) - COUNT(DISTINCT b.id), 0) > 0
            ORDER BY p.rating DESC, lowest_price ASC
            LIMIT %s
        """
        params.extend([guests, limit])
        result = self.query(sql, params=params, max_rows=limit)
        result["filters"] = {
            "city": city,
            "check_in_date": check_in_date,
            "check_out_date": check_out_date,
            "guests": guests,
        }
        return result

    def get_business_overview(self, date_from=None, date_to=None, merchant_id=None, property_id=None):
        where_clauses = ["1 = 1"]
        params = []

        if merchant_id:
            where_clauses.append("p.merchant_id = %s")
            params.append(merchant_id)
        if property_id:
            where_clauses.append("b.property_id = %s")
            params.append(property_id)
        if date_from:
            where_clauses.append("DATE(b.created_at) >= %s")
            params.append(date_from)
        if date_to:
            where_clauses.append("DATE(b.created_at) <= %s")
            params.append(date_to)

        summary_sql = f"""
            SELECT
                COUNT(*) AS booking_count,
                SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END) AS confirmed_count,
                SUM(CASE WHEN b.status = 'CHECKED_IN' THEN 1 ELSE 0 END) AS checked_in_count,
                SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_count,
                ROUND(COALESCE(SUM(b.total_amount), 0), 2) AS total_amount,
                ROUND(COALESCE(SUM(b.paid_amount), 0), 2) AS total_paid_amount,
                ROUND(COALESCE(AVG(b.total_amount), 0), 2) AS avg_order_value
            FROM bookings b
            JOIN properties p ON p.id = b.property_id
            WHERE {' AND '.join(where_clauses)}
        """
        top_sql = f"""
            SELECT
                p.id,
                p.property_name,
                COUNT(b.id) AS booking_count,
                ROUND(COALESCE(SUM(b.total_amount), 0), 2) AS total_amount
            FROM bookings b
            JOIN properties p ON p.id = b.property_id
            WHERE {' AND '.join(where_clauses)}
            GROUP BY p.id, p.property_name
            ORDER BY total_amount DESC, booking_count DESC
            LIMIT 5
        """

        review_clauses = ["1 = 1"]
        review_params = []
        if merchant_id:
            review_clauses.append("p.merchant_id = %s")
            review_params.append(merchant_id)
        if property_id:
            review_clauses.append("r.property_id = %s")
            review_params.append(property_id)
        if date_from:
            review_clauses.append("DATE(r.created_at) >= %s")
            review_params.append(date_from)
        if date_to:
            review_clauses.append("DATE(r.created_at) <= %s")
            review_params.append(date_to)

        review_sql = f"""
            SELECT
                ROUND(COALESCE(AVG(r.overall_rating), 0), 2) AS avg_rating,
                COUNT(*) AS review_count
            FROM reviews r
            JOIN properties p ON p.id = r.property_id
            WHERE {' AND '.join(review_clauses)}
              AND r.status = 'APPROVED'
        """

        property_sql = """
            SELECT
                COUNT(*) AS active_properties
            FROM properties p
            WHERE p.status = 'APPROVED'
        """
        property_params = []
        if merchant_id:
            property_sql += " AND p.merchant_id = %s"
            property_params.append(merchant_id)
        if property_id:
            property_sql += " AND p.id = %s"
            property_params.append(property_id)

        summary = self.query(summary_sql, params=params, max_rows=1)["rows"]
        top_properties = self.query(top_sql, params=params, max_rows=5)["rows"]
        review_summary = self.query(review_sql, params=review_params, max_rows=1)["rows"]
        property_summary = self.query(property_sql, params=property_params, max_rows=1)["rows"]

        return {
            "date_range": {"date_from": date_from, "date_to": date_to},
            "summary": (summary[0] if summary else {}),
            "reviews": (review_summary[0] if review_summary else {}),
            "properties": (property_summary[0] if property_summary else {}),
            "top_properties": top_properties,
            "metric_basis": "订单口径基于 bookings.created_at，评价口径基于 reviews.created_at。",
        }

    def _sanitize_sql(self, sql):
        if not sql or not sql.strip():
            raise ValueError("SQL 不能为空。")
        return sql.strip().rstrip(";")

    def _ensure_readonly(self, sql):
        lowered = re.sub(r"\s+", " ", sql).strip().lower()
        allowed_prefixes = ("select ", "with ", "show ", "describe ", "desc ", "explain ")
        if not lowered.startswith(allowed_prefixes):
            raise ValueError("只允许执行只读 SQL（SELECT / WITH / SHOW / DESCRIBE / EXPLAIN）。")

        forbidden = [
            " insert ",
            " update ",
            " delete ",
            " drop ",
            " alter ",
            " truncate ",
            " create ",
            " replace ",
            " grant ",
            " revoke ",
            " call ",
        ]
        padded = f" {lowered} "
        for keyword in forbidden:
            if keyword in padded:
                raise ValueError("检测到可能修改数据的 SQL，已拒绝执行。")

    def _serialize_rows(self, rows):
        serialized = []
        for row in rows:
            serialized_row = {}
            for key, value in row.items():
                if hasattr(value, "isoformat"):
                    serialized_row[key] = value.isoformat()
                else:
                    serialized_row[key] = value
            serialized.append(serialized_row)
        return serialized





