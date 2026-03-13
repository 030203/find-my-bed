# 民宿预定管理系统

## 项目简介
这是一个前后端分离的民宿预定管理系统，用于管理民宿的房间预定。

## 项目架构

### 前端技术栈
- Vue 3
- Vite (构建工具)
- Vue Router (路由管理)
- Pinia (状态管理)
- Element Plus (UI组件库)
- Axios (HTTP客户端)

### 后端技术栈
- Spring Boot 3.1.5
- Spring Data JPA (数据访问)
- MySQL 8.0 (数据库)
- Maven (项目管理)

### 数据库
- MySQL 8.0+
- 通过 Navicat 或其他数据库工具连接

## 项目结构

```
民宿预定管理系统/
├── frontend/                 # 前端项目
│   ├── src/
│   │   ├── components/      # Vue组件
│   │   ├── views/           # 页面视图
│   │   ├── api/             # API调用
│   │   ├── store/           # Pinia状态管理
│   │   ├── router/          # 路由配置
│   │   ├── utils/           # 工具函数
│   │   ├── assets/          # 静态资源
│   │   ├── App.vue          # 根组件
│   │   └── main.js          # 入口文件
│   ├── public/              # 公共文件
│   ├── package.json         # 前端依赖
│   ├── vite.config.js       # Vite配置
│   └── index.html           # HTML入口
│
├── backend/                  # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/booking/system/
│   │   │   │   ├── controller/     # 控制器
│   │   │   │   ├── service/        # 服务层
│   │   │   │   ├── entity/         # 数据实体
│   │   │   │   ├── mapper/         # 数据访问
│   │   │   │   ├── config/         # 配置类
│   │   │   │   ├── util/           # 工具类
│   │   │   │   └── BookingSystemApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── init.sql
│   │   └── test/
│   └── pom.xml              # Maven配置
│
├── docs/                     # 文档目录
├── .vscode/                  # VS Code配置
└── README.md                 # 项目说明
```

## 数据库设置

### 1. 创建数据库
使用 Navicat 或命令行执行以下SQL：

```sql
CREATE DATABASE IF NOT EXISTS booking_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 初始化表结构
执行 `backend/src/main/resources/init.sql` 文件中的SQL语句

### 3. Navicat 连接配置
- Host: localhost
- Port: 3306
- Username: root
- Password: root
- Database: booking_system

## 前端部署

### 安装依赖
```bash
cd frontend
npm install
```

### 开发模式
```bash
npm run dev
```
访问: http://localhost:5173

### 构建生产版本
```bash
npm run build
```

## 后端部署

### 前置条件
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 编译项目
```bash
cd backend
mvn clean package
```

### 运行应用
```bash
mvn spring-boot:run
```

或直接运行JAR文件：
```bash
java -jar target/booking-system-1.0.0.jar
```

应用将运行在: http://localhost:8080

## API 接口

### 预定管理接口

#### 获取所有预定
```
GET /api/bookings
```

#### 获取单个预定
```
GET /api/bookings/{id}
```

#### 创建预定
```
POST /api/bookings
Content-Type: application/json

{
  "guestName": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "checkInDate": "2024-12-15",
  "checkOutDate": "2024-12-17",
  "numberOfGuests": 2,
  "roomType": "标准间",
  "price": 299.00,
  "status": "PENDING",
  "remarks": ""
}
```

#### 更新预定
```
PUT /api/bookings/{id}
Content-Type: application/json
```

#### 删除预定
```
DELETE /api/bookings/{id}
```

#### 按状态查询
```
GET /api/bookings/status/{status}
```

#### 搜索预定
```
GET /api/bookings/search/{guestName}
```

## 前后端通信

### 代理配置
前端已配置代理将 `/api` 请求转发到后端：
- 前端地址: http://localhost:5173
- 后端地址: http://localhost:8080
- 代理配置在: `frontend/vite.config.js`

### CORS 配置
后端已配置CORS，允许来自前端的跨域请求。

## 主要功能

1. **预定管理**
   - 查看所有预定
   - 创建新预定
   - 修改预定信息
   - 删除预定
   - 查询预定状态

2. **数据管理**
   - MySQL数据库存储
   - Navicat可视化管理

3. **前后端分离**
   - RESTful API接口
   - Vue 3单页应用
   - 响应式设计

## 常见问题

### 1. 数据库连接失败
检查 `backend/src/main/resources/application.properties` 中的数据库配置是否正确

### 2. 前端无法访问后端
确保后端已启动（http://localhost:8080），并检查CORS配置

### 3. 前端依赖安装失败
```bash
npm cache clean --force
npm install
```

## 许可证
MIT

## 作者
潘小墨