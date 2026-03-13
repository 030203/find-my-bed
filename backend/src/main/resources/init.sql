-- ============================================
-- 民宿预定管理系统 - 完整数据库设计
-- 角色：用户(USER)、管理员(ADMIN)、商户(MERCHANT)
-- ============================================

CREATE DATABASE IF NOT EXISTS booking_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE booking_system;

-- ============================================
-- 1. 用户体系
-- ============================================

-- 用户表（统一管理所有角色）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    role ENUM('USER', 'ADMIN', 'MERCHANT') NOT NULL DEFAULT 'USER' COMMENT '角色：用户/管理员/商户',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：激活/未激活/封禁',
    last_login_time TIMESTAMP NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户资料表（扩展用户信息）
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    real_name VARCHAR(50) COMMENT '真实姓名',
    id_card VARCHAR(18) COMMENT '身份证号',
    gender ENUM('MALE', 'FEMALE', 'OTHER') COMMENT '性别',
    birthday DATE COMMENT '生日',
    address VARCHAR(255) COMMENT '地址',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) COMMENT '紧急联系电话',
    preferences JSON COMMENT '用户偏好设置（JSON格式）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资料表';

-- ============================================
-- 2. 商户体系
-- ============================================

-- 商户表
CREATE TABLE IF NOT EXISTS merchants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '关联用户ID',
    merchant_name VARCHAR(100) NOT NULL COMMENT '商户名称',
    business_license VARCHAR(50) COMMENT '营业执照号',
    legal_person VARCHAR(50) COMMENT '法人代表',
    contact_name VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    address VARCHAR(255) COMMENT '商户地址',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    district VARCHAR(50) COMMENT '区县',
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED') NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：待审核/已通过/已拒绝/已暂停',
    audit_remark TEXT COMMENT '审核备注',
    audit_time TIMESTAMP NULL COMMENT '审核时间',
    audit_by BIGINT COMMENT '审核人ID',
    commission_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '平台佣金率（%）',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
    rating DECIMAL(3,2) DEFAULT 0.00 COMMENT '商户评分（0-5）',
    total_reviews INT DEFAULT 0 COMMENT '总评价数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户表';

-- 商户认证资料表
CREATE TABLE IF NOT EXISTS merchant_certifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    cert_type ENUM('BUSINESS_LICENSE', 'ID_CARD', 'OTHER') NOT NULL COMMENT '证件类型',
    cert_number VARCHAR(50) COMMENT '证件号码',
    cert_image VARCHAR(255) COMMENT '证件图片URL',
    cert_front_image VARCHAR(255) COMMENT '证件正面图片',
    cert_back_image VARCHAR(255) COMMENT '证件背面图片',
    expiry_date DATE COMMENT '有效期',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '审核状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE,
    INDEX idx_merchant_id (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户认证资料表';

-- ============================================
-- 3. 民宿/酒店体系
-- ============================================

-- 民宿/酒店表
CREATE TABLE IF NOT EXISTS properties (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    property_name VARCHAR(100) NOT NULL COMMENT '民宿名称',
    property_type ENUM('HOMESTAY', 'HOTEL', 'APARTMENT', 'VILLA', 'OTHER') NOT NULL DEFAULT 'HOMESTAY' COMMENT '类型：民宿/酒店/公寓/别墅/其他',
    description TEXT COMMENT '描述',
    address VARCHAR(255) NOT NULL COMMENT '详细地址',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) COMMENT '区县',
    longitude DECIMAL(10,7) COMMENT '经度',
    latitude DECIMAL(10,7) COMMENT '纬度',
    check_in_time TIME DEFAULT '14:00:00' COMMENT '入住时间',
    check_out_time TIME DEFAULT '12:00:00' COMMENT '退房时间',
    total_rooms INT DEFAULT 0 COMMENT '总房间数',
    available_rooms INT DEFAULT 0 COMMENT '可用房间数',
    rating DECIMAL(3,2) DEFAULT 0.00 COMMENT '评分（0-5）',
    total_reviews INT DEFAULT 0 COMMENT '总评价数',
    price_range_min DECIMAL(10,2) COMMENT '最低价格',
    price_range_max DECIMAL(10,2) COMMENT '最高价格',
    facilities JSON COMMENT '设施列表（JSON：WiFi、停车场、早餐等）',
    images JSON COMMENT '图片列表（JSON：封面图、详情图等）',
    policies JSON COMMENT '政策说明（JSON：取消政策、宠物政策等）',
    status ENUM('DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态：草稿/待审核/已通过/已拒绝/已暂停',
    is_featured BOOLEAN DEFAULT FALSE COMMENT '是否推荐',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE,
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_city (city),
    INDEX idx_status (status),
    INDEX idx_rating (rating),
    INDEX idx_location (longitude, latitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='民宿/酒店表';

-- 房间类型表
CREATE TABLE IF NOT EXISTS room_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    property_id BIGINT NOT NULL COMMENT '民宿ID',
    type_name VARCHAR(50) NOT NULL COMMENT '房型名称（如：标准间、豪华套房）',
    description TEXT COMMENT '房型描述',
    max_occupancy INT NOT NULL DEFAULT 2 COMMENT '最大入住人数',
    bed_type VARCHAR(50) COMMENT '床型（如：大床、双床、单人床）',
    room_size DECIMAL(6,2) COMMENT '房间面积（平方米）',
    amenities JSON COMMENT '房间设施（JSON：空调、电视、WiFi等）',
    images JSON COMMENT '房型图片（JSON）',
    base_price DECIMAL(10,2) NOT NULL COMMENT '基础价格',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    INDEX idx_property_id (property_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间类型表';

-- 房间表
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    property_id BIGINT NOT NULL COMMENT '民宿ID',
    room_type_id BIGINT NOT NULL COMMENT '房型ID',
    room_number VARCHAR(20) NOT NULL COMMENT '房间号',
    floor_number INT COMMENT '楼层',
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'OUT_OF_ORDER') NOT NULL DEFAULT 'AVAILABLE' COMMENT '房间状态：可用/已入住/维护中/停用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE CASCADE,
    UNIQUE KEY uk_property_room (property_id, room_number),
    INDEX idx_property_id (property_id),
    INDEX idx_room_type_id (room_type_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房间表';

-- ============================================
-- 4. 价格体系
-- ============================================

-- 价格规则表（支持动态定价）
CREATE TABLE IF NOT EXISTS pricing_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_type_id BIGINT NOT NULL COMMENT '房型ID',
    rule_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'SEASONAL', 'SPECIAL') NOT NULL COMMENT '规则类型：日/周/月/季节/特殊',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    day_of_week TINYINT COMMENT '星期几（1-7，NULL表示所有）',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    discount_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '折扣率（%）',
    min_nights INT DEFAULT 1 COMMENT '最少入住天数',
    max_nights INT COMMENT '最多入住天数',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE CASCADE,
    INDEX idx_room_type_id (room_type_id),
    INDEX idx_dates (start_date, end_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格规则表';

-- 优惠券表
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id BIGINT COMMENT '商户ID（NULL表示平台优惠券）',
    coupon_name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    coupon_type ENUM('FIXED', 'PERCENTAGE') NOT NULL COMMENT '类型：固定金额/百分比',
    discount_value DECIMAL(10,2) NOT NULL COMMENT '优惠值',
    min_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低消费金额',
    max_discount DECIMAL(10,2) COMMENT '最大优惠金额（百分比类型）',
    total_quantity INT NOT NULL COMMENT '总数量',
    used_quantity INT DEFAULT 0 COMMENT '已使用数量',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    applicable_properties JSON COMMENT '适用民宿ID列表（NULL表示全部）',
    status ENUM('ACTIVE', 'INACTIVE', 'EXPIRED') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE SET NULL,
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status),
    INDEX idx_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS user_coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT NOT NULL COMMENT '优惠券ID',
    status ENUM('UNUSED', 'USED', 'EXPIRED') DEFAULT 'UNUSED' COMMENT '状态：未使用/已使用/已过期',
    used_at TIMESTAMP NULL COMMENT '使用时间',
    used_booking_id BIGINT COMMENT '使用的预定ID',
    obtained_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    expired_at DATETIME COMMENT '过期时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_coupon_id (coupon_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- ============================================
-- 5. 预定体系
-- ============================================

-- 预定表
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_number VARCHAR(32) NOT NULL UNIQUE COMMENT '预定单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    property_id BIGINT NOT NULL COMMENT '民宿ID',
    room_type_id BIGINT NOT NULL COMMENT '房型ID',
    room_id BIGINT COMMENT '房间ID（分配后）',
    check_in_date DATE NOT NULL COMMENT '入住日期',
    check_out_date DATE NOT NULL COMMENT '退房日期',
    nights INT NOT NULL COMMENT '入住天数',
    number_of_guests INT NOT NULL DEFAULT 1 COMMENT '入住人数',
    adult_count INT DEFAULT 1 COMMENT '成人数',
    child_count INT DEFAULT 0 COMMENT '儿童数',
    base_price DECIMAL(10,2) NOT NULL COMMENT '基础价格',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '折扣金额',
    coupon_id BIGINT COMMENT '使用的优惠券ID',
    coupon_discount DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠券折扣',
    service_fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '服务费',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    paid_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '已支付金额',
    refund_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '退款金额',
    status ENUM('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'REFUNDED') NOT NULL DEFAULT 'PENDING' COMMENT '状态：待确认/已确认/已入住/已退房/已取消/已退款',
    payment_status ENUM('UNPAID', 'PARTIAL', 'PAID', 'REFUNDED') DEFAULT 'UNPAID' COMMENT '支付状态：未支付/部分支付/已支付/已退款',
    cancellation_reason TEXT COMMENT '取消原因',
    cancellation_time TIMESTAMP NULL COMMENT '取消时间',
    special_requests TEXT COMMENT '特殊要求',
    contact_name VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    check_in_time TIMESTAMP NULL COMMENT '实际入住时间',
    check_out_time TIMESTAMP NULL COMMENT '实际退房时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE SET NULL,
    INDEX idx_booking_number (booking_number),
    INDEX idx_user_id (user_id),
    INDEX idx_property_id (property_id),
    INDEX idx_status (status),
    INDEX idx_dates (check_in_date, check_out_date),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预定表';

-- 预定客人表（多人入住）
CREATE TABLE IF NOT EXISTS booking_guests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL COMMENT '预定ID',
    guest_name VARCHAR(50) NOT NULL COMMENT '客人姓名',
    id_card VARCHAR(18) COMMENT '身份证号',
    phone VARCHAR(20) COMMENT '联系电话',
    is_main_guest BOOLEAN DEFAULT FALSE COMMENT '是否主客人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    INDEX idx_booking_id (booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预定客人表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_number VARCHAR(32) NOT NULL UNIQUE COMMENT '支付单号',
    booking_id BIGINT NOT NULL COMMENT '预定ID',
    payment_method ENUM('ALIPAY', 'WECHAT', 'BANK_CARD', 'CASH', 'OTHER') NOT NULL COMMENT '支付方式',
    payment_type ENUM('DEPOSIT', 'FULL', 'REFUND') NOT NULL COMMENT '支付类型：定金/全款/退款',
    amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '支付状态',
    transaction_id VARCHAR(100) COMMENT '第三方交易号',
    paid_at TIMESTAMP NULL COMMENT '支付时间',
    remark TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    INDEX idx_payment_number (payment_number),
    INDEX idx_booking_id (booking_id),
    INDEX idx_status (status),
    INDEX idx_paid_at (paid_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- ============================================
-- 6. 评价体系
-- ============================================

-- 评价表
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL UNIQUE COMMENT '预定ID（一个预定只能评价一次）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    property_id BIGINT NOT NULL COMMENT '民宿ID',
    overall_rating TINYINT NOT NULL COMMENT '总体评分（1-5）',
    cleanliness_rating TINYINT COMMENT '清洁度评分（1-5）',
    service_rating TINYINT COMMENT '服务评分（1-5）',
    location_rating TINYINT COMMENT '位置评分（1-5）',
    value_rating TINYINT COMMENT '性价比评分（1-5）',
    comment TEXT COMMENT '评价内容',
    images JSON COMMENT '评价图片（JSON）',
    is_anonymous BOOLEAN DEFAULT FALSE COMMENT '是否匿名',
    is_verified BOOLEAN DEFAULT FALSE COMMENT '是否已验证入住',
    helpful_count INT DEFAULT 0 COMMENT '有用数',
    reply_content TEXT COMMENT '商家回复',
    reply_time TIMESTAMP NULL COMMENT '回复时间',
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'HIDDEN') DEFAULT 'PENDING' COMMENT '状态：待审核/已通过/已拒绝/已隐藏',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    INDEX idx_booking_id (booking_id),
    INDEX idx_user_id (user_id),
    INDEX idx_property_id (property_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- ============================================
-- 7. 系统功能
-- ============================================

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID（NULL表示系统通知）',
    notification_type ENUM('SYSTEM', 'BOOKING', 'PAYMENT', 'REVIEW', 'PROMOTION') NOT NULL COMMENT '通知类型',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    related_id BIGINT COMMENT '关联ID（如预定ID）',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    read_at TIMESTAMP NULL COMMENT '阅读时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作用户ID',
    user_role ENUM('USER', 'ADMIN', 'MERCHANT') COMMENT '用户角色',
    action_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    module VARCHAR(50) NOT NULL COMMENT '模块',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    description TEXT COMMENT '操作描述',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(255) COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 系统设置表
CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) NOT NULL UNIQUE COMMENT '设置键',
    setting_value TEXT COMMENT '设置值',
    setting_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING' COMMENT '值类型',
    description VARCHAR(255) COMMENT '描述',
    category VARCHAR(50) COMMENT '分类',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置表';

-- ============================================
-- 8. 初始化数据
-- ============================================

-- 插入管理员用户（密码：admin123，实际应使用BCrypt加密）
INSERT INTO users (username, password, role, email, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK7pXvO', 'ADMIN', 'admin@example.com', 'ACTIVE'),
('merchant1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK7pXvO', 'MERCHANT', 'merchant1@example.com', 'ACTIVE'),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK7pXvO', 'USER', 'user1@example.com', 'ACTIVE');

-- 插入系统设置
INSERT INTO system_settings (setting_key, setting_value, setting_type, description, category) VALUES
('platform_commission_rate', '10.00', 'NUMBER', '平台默认佣金率', 'FINANCE'),
('booking_cancellation_hours', '24', 'NUMBER', '可免费取消的小时数', 'BOOKING'),
('max_booking_days', '90', 'NUMBER', '最多可提前预定的天数', 'BOOKING'),
('min_booking_days', '1', 'NUMBER', '最少提前预定的天数', 'BOOKING');