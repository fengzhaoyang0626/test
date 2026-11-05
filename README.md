# newproiect 简易管理系统

这是第一阶段版本，包含一个可运行的 Spring Boot 后端和用户管理接口。当前没有 Docker、登录认证、Redis、MQ 和多线程功能。

## 已实现功能

- Spring Boot 3 + Java 17
- MySQL + MyBatis-Plus
- Flyway 自动创建和升级数据库表
- 用户新增、详情、分页查询、修改和逻辑删除
- BCrypt 密码加密，接口不会返回密码
- 参数校验和统一错误响应
- Swagger UI 在线接口文档
- Actuator 健康检查
- 控制器与响应模型的基础测试

## 本地环境

需要安装：

1. JDK 17 或更高版本
2. MySQL 8

Maven 不需要单独安装，项目已经包含 Maven Wrapper。

## 1. 创建数据库

先登录 MySQL：

```bash
mysql -u root -p
```

执行：

```sql
CREATE DATABASE IF NOT EXISTS newproiect
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

不需要手动创建 `sys_user` 表，应用启动时 Flyway 会自动执行迁移脚本。

## 2. 配置数据库连接

默认连接配置是：

```text
地址：jdbc:mysql://localhost:3306/newproiect
用户名：root
密码：12345678
```

如果与你的 MySQL 不一致，可以通过环境变量覆盖：

```bash
export DB_URL='jdbc:mysql://localhost:3306/newproiect?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true'
export DB_USERNAME='root'
export DB_PASSWORD='你的数据库密码'
```

## 3. 启动项目

在项目目录执行：

```bash
./mvnw spring-boot:run
```

看到类似下面的日志表示启动成功：

```text
Tomcat started on port 8080
Started NewproiectApplication
```

健康检查地址：

```text
http://localhost:8080/actuator/health
```

## 4. 使用 Swagger 调试接口

Swagger 可以理解成“自动生成的接口说明书 + 在线调用工具”。启动项目后打开：

```text
http://localhost:8080/swagger-ui.html
```

页面中会看到“用户管理”分组，点开后能看到所有用户接口。

以“新增用户”为例：

1. 展开 `POST /api/users`。
2. 点击右上角 `Try it out`。
3. 修改 `Request body` 中的 JSON。
4. 点击 `Execute`。
5. 在下方 `Server response` 查看状态码和返回结果。

可以直接使用下面的请求体：

```json
{
  "username": "demo_user",
  "password": "password123",
  "displayName": "演示用户",
  "email": "demo@example.com",
  "phone": "13800000000",
  "status": 1
}
```

创建成功会返回 HTTP `201`，响应示例：

```json
{
  "success": true,
  "code": "OK",
  "message": "请求成功",
  "data": {
    "id": 1,
    "username": "demo_user",
    "displayName": "演示用户",
    "email": "demo@example.com",
    "phone": "13800000000",
    "status": 1,
    "createdAt": "2026-11-05 12:00:00",
    "updatedAt": "2026-11-05 12:00:00"
  },
  "timestamp": "2026-11-05 12:00:00"
}
```

拿到返回的 `id` 后，可以继续调试：

```text
GET    /api/users/{id}
GET    /api/users?page=1&size=10&keyword=demo&status=1
PUT    /api/users/{id}
DELETE /api/users/{id}
```

Swagger UI 底部的 `Schemas` 区域可以查看每个请求体和响应字段的说明与示例。

原始 OpenAPI JSON 地址：

```text
http://localhost:8080/v3/api-docs
```

## 5. 响应格式

成功响应：

```json
{
  "success": true,
  "code": "OK",
  "message": "请求成功",
  "data": {},
  "timestamp": "2026-11-05 12:00:00"
}
```

失败响应：

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "参数校验失败",
  "data": {
    "password": "密码长度必须在 8 到 72 个字符之间"
  },
  "timestamp": "2026-11-05 12:00:00"
}
```

## 6. 运行测试

```bash
./mvnw test
```

当前测试不依赖 MySQL，因此不需要先启动数据库。

## 7. 项目结构

```text
src/main/java/com/example/newproiect
├── common
│   ├── api          # 统一响应和分页
│   ├── config       # MyBatis-Plus、审计字段、Swagger 配置
│   └── exception    # 错误码和全局异常处理
└── system
    └── user
        ├── controller
        ├── dto
        ├── entity
        ├── mapper
        └── service
```

## 常见问题

### 启动时报数据库连接失败

检查 MySQL 是否启动、`newproiect` 数据库是否已创建，以及 `DB_USERNAME`、`DB_PASSWORD` 是否正确。

### Swagger 页面打不开

确认使用的是 `http://localhost:8080/swagger-ui.html`，并检查应用日志中是否出现 `Started NewproiectApplication`。

### 删除后为什么数据库里还有记录

当前采用逻辑删除，删除操作会把 `deleted` 字段改为 `1`，普通查询不会再返回该记录。

## 下一阶段

后续建议依次增加：

1. 部门与角色管理
2. Spring Security、登录和权限校验
3. 更完整的单元测试和集成测试
4. Redis 缓存
5. MQ 异步处理
6. 线程池和批量任务

当前版本还没有登录认证，只适合本地开发，不要直接暴露到公网。
