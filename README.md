# newproiect 简易管理系统

这是一个可运行的 Spring Boot 后端和用户管理接口。当前已接入 Redis 缓存、多线程批量查询和 Git 版本管理，暂未引入 Docker、登录认证和 MQ。

## 已实现功能

- Spring Boot 3 + Java 17
- MySQL + MyBatis-Plus
- Flyway 自动创建和升级数据库表
- 用户新增、详情、分页查询、修改和逻辑删除
- BCrypt 密码加密，接口不会返回密码
- 参数校验和统一错误响应
- Swagger UI 在线接口文档
- Actuator 健康检查
- Redis 用户详情缓存，修改和删除时主动失效
- Redis 不可用时自动回退数据库
- 有界线程池批量查询用户，单项失败隔离并支持超时
- 控制器与响应模型的基础测试

## 本地环境

需要安装：

1. JDK 17 或更高版本
2. MySQL 8
3. Redis

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

## 3. 配置 Redis

项目默认连接本机 Redis：

```text
地址：localhost
端口：6379
数据库：0
密码：空
缓存有效期：30 分钟
```

如果 Redis 配置不同，可以通过环境变量覆盖：

```bash
export REDIS_HOST='localhost'
export REDIS_PORT='6379'
export REDIS_PASSWORD=''
export REDIS_DATABASE='0'
export USER_CACHE_TTL='30m'
```

确认 Redis 可用：

```bash
redis-cli ping
```

正常会返回：

```text
PONG
```

用户详情缓存 Key 的格式为：

```text
newproiect:user:{用户ID}
```

例如查询 ID 为 `1` 的用户后，可以查看：

```bash
redis-cli GET 'newproiect:user:1'
```

Redis 暂时不可用时，应用仍会从 MySQL 查询数据，不会因为缓存故障停止服务。

## 4. 多线程批量查询

批量查询接口：

```text
POST /api/users/batch-query
```

请求体：

```json
{
  "ids": [1, 2, 3]
}
```

线程池默认配置：

```text
核心线程数：4
最大线程数：8
队列容量：100
结果等待超时：5 秒
拒绝策略：CallerRunsPolicy
```

队列满时由调用线程执行任务，通过回压避免无限堆积。可以通过环境变量调整：

```bash
export USER_BATCH_CORE_SIZE='4'
export USER_BATCH_MAX_SIZE='8'
export USER_BATCH_QUEUE_CAPACITY='100'
export USER_BATCH_KEEP_ALIVE='60s'
export USER_BATCH_TASK_TIMEOUT='5s'
```

接口一次最多接收 100 个 ID，重复 ID 会自动去重。每个 ID 独立返回成功或失败，不会因为单个用户不存在而导致整批请求失败。

当前批量操作只做并发读查询。后续增加并发写操作时，需要额外处理事务边界、幂等和重复提交；不要假设主线程的事务或 ThreadLocal 会自动传递到工作线程。

## 5. 启动项目

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

## 6. 使用 Swagger 调试接口

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
GET    /api/users/list?name=demo@example.com
GET    /api/users?page=1&size=10&keyword=demo&status=1
POST   /api/users/batch-query
PUT    /api/users/{id}
DELETE /api/users/{id}
```

Swagger UI 底部的 `Schemas` 区域可以查看每个请求体和响应字段的说明与示例。

原始 OpenAPI JSON 地址：

```text
http://localhost:8080/v3/api-docs
```

## 7. 响应格式

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

## 8. 运行测试

```bash
./mvnw test
```

当前测试使用 H2 内存数据库，并在测试配置中关闭 Redis，不依赖本地 MySQL 和 Redis。

## 9. Git 版本管理

项目已经初始化为 Git 仓库，当前分支为 `main`。日常提交步骤：

```bash
git status
git add .
git commit -m "feat: describe your change"
git log --oneline
```

构建产物 `target/`、IDE 配置 `.idea/`、日志和环境变量文件已经写入 `.gitignore`，不会被提交。

## 10. 项目结构

```text
src/main/java/com/example/newproiect
├── common
│   ├── api          # 统一响应和分页
│   ├── config       # MyBatis-Plus、线程池、审计字段、Swagger 配置
│   └── exception    # 错误码和全局异常处理
└── system
    └── user
        ├── cache      # Redis 用户缓存
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

### Redis 连接失败会不会影响查询

不会。缓存读写失败时会回退到 MySQL，同时会在应用日志中记录警告。Redis 恢复后，后续查询会自动重新写入缓存。

## 下一阶段

后续建议依次增加：

1. 部门与角色管理
2. Spring Security、登录和权限校验
3. 更完整的单元测试和集成测试
4. MQ 异步处理

当前版本还没有登录认证，只适合本地开发，不要直接暴露到公网。
