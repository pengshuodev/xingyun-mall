```markdown
# 星云商城 - 订单支付系统

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.14-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.3-blue.svg)](https://baomidou.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://mysql.com/)

## 📖 项目简介

星云商城是一个基于 Spring Boot 的 B2C 电商订单支付系统，支持用户认证、商品管理、购物车、订单创建、支付回调等核心功能。

项目特点：
- 🛡️ **库存防超卖**：乐观锁（版本号机制）保证高并发数据一致性
- 💰 **支付最终一致性**：本地消息表 + 定时任务补偿方案
- 🚀 **购物车高性能**：Redis Hash 存储，O(1) 读写
- 🔄 **幂等性设计**：唯一索引 + 状态机防止重复处理
- 📊 **订单状态机**：封装状态流转规则，业务语义清晰

## 🛠 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 核心语言 |
| Spring Boot | 2.7.14 | 基础框架 |
| MyBatis-Plus | 3.5.3 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.0 | 缓存 + 购物车存储 |
| JWT | 0.9.1 | 用户认证 |
| Hutool | 5.8.20 | 工具类（雪花算法） |
| Swagger | 1.7.0 | API 文档 |
| Maven | 3.8+ | 项目构建 |
| Docker | Latest | 容器化部署 |

## 📁 项目结构

```text
xingyun-mall/
├── sql/                            # 建表脚本
│   └── schema.sql                  # 完整建表语句
├── src/main/java/com/xingyun/orderpayment/
│   ├── common/                     # 通用模块
│   │   ├── config/                 # 配置类
│   │   ├── constant/               # 常量类
│   │   ├── context/                # ThreadLocal 上下文
│   │   ├── enums/                  # 枚举（状态、异常码）
│   │   ├── exception/              # 全局异常处理
│   │   └── utils/                  # 工具类
│   ├── infrastructure/             # 基础设施
│   │   ├── interceptor/            # 拦截器（JWT 认证）
│   │   └── config/                 # 框架配置
│   └── modules/                    # 业务模块
│       ├── user/                   # 用户模块
│       ├── product/                # 商品模块
│       ├── cart/                   # 购物车模块
│       ├── order/                  # 订单模块
│       └── payment/                # 支付模块
└── pom.xml
```

## 🚀 快速启动

### 1. 环境要求

- JDK 21+（推荐 Eclipse Temurin 或 Amazon Corretto 21）
- Docker（用于运行 MySQL、Redis）
- Maven 3.8+
- Git

### 2. 克隆项目

```bash
git clone https://github.com/pengshuodev/xingyun-mall.git
cd xingyun-mall
```

### 3. 启动 MySQL 容器

```bash
docker run -d \
  --name mysql-xingyun \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=xingyun_mall \
  -v mysql-data:/var/lib/mysql \
  mysql:8.0
```

### 4. 启动 Redis 容器

```bash
docker run -d \
  --name redis-xingyun \
  -p 6379:6379 \
  -v redis-data:/data \
  redis:7.0 \
  redis-server --appendonly yes
```

### 5. 执行建表 SQL

使用 TablePlus 或 IDEA Database 执行 `sql/schema.sql` 文件。

### 6. 修改配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    password: 123456  # 改为你的 MySQL 密码
```

### 7. 启动项目

在 IDEA 中运行 `XyOrderPaymentApplication.java`，或使用 Maven：

```bash
mvn spring-boot:run
```

### 8. 访问 API 文档

启动成功后访问：http://localhost:8089/swagger-ui/index.html

## 📋 核心 API

### 用户模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户注册 | POST | `/api/user/register` | 用户名、密码、手机号 |
| 用户登录 | POST | `/api/user/login` | 返回 JWT Token |

**登录请求体**：
```json
{
  "username": "test001",
  "password": "123456"
}
```

**登录响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "username": "test001",
    "userId": 1
  }
}
```

### 商品模块

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 商品列表 | GET | `/api/product/list` | 分页查询，支持关键词搜索 |
| 商品详情 | GET | `/api/product/{id}` | 查询单个商品 |

### 购物车模块（需认证）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 添加商品 | POST | `/api/cart/add` | 添加商品到购物车 |
| 查询购物车 | GET | `/api/cart/list` | 查看购物车列表 |
| 修改数量 | PUT | `/api/cart/update` | 修改商品数量 |
| 删除商品 | DELETE | `/api/cart/remove/{productId}` | 移除商品 |
| 清空购物车 | DELETE | `/api/cart/clear` | 清空购物车 |

### 订单模块（需认证）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建订单 | POST | `/api/order/create` | 从购物车生成订单 |
| 订单列表 | GET | `/api/order/list` | 分页查询用户订单 |
| 订单详情 | GET | `/api/order/{orderNo}` | 查询单个订单 |
| 取消订单 | PUT | `/api/order/cancel/{orderNo}` | 未支付订单取消 |

### 支付模块（需认证）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发起支付 | POST | `/api/payment/create` | 创建支付单 |
| 支付回调 | POST | `/api/payment/callback` | 模拟支付回调 |
| 查询状态 | GET | `/api/payment/status/{orderNo}` | 查询支付结果 |

## 🔄 核心流程图

### 下单流程

```
用户登录 → 浏览商品 → 加购物车 → 创建订单 → 扣减库存 → 发起支付 → 支付回调 → 订单状态更新
```

### 库存防超卖（乐观锁）

```sql
-- MyBatis-Plus 自动生成的 SQL
UPDATE t_product 
SET stock = ?, version = version + 1 
WHERE id = ? AND version = ?
```

### 支付最终一致性（本地消息表 + 定时补偿）

- **正常路径**：支付平台回调 → 更新支付状态 → 更新订单状态
- **异常补偿路径**：定时任务（每 5 分钟）扫描 `t_payment_log` 中**创建超过 10 分钟**且状态为待支付的记录，主动查询支付平台结果，最多重试 3 次

### 超时订单自动关单

- **触发条件**：定时任务（每 2 分钟）扫描 `t_order` 中**创建超过 30 分钟**且状态为待支付的订单
- **处理动作**：恢复库存（乐观锁）→ 更新订单状态为**3（已关闭）**

## 📊 数据库设计

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `t_user` | 用户表 | id, username, password, status |
| `t_product` | 商品表 | id, name, price, stock, **version**（乐观锁字段） |
| `t_order` | 订单表 | order_no, user_id, total_amount, **status**（0待支付/1已支付/2已取消/3已关闭） |
| `t_order_item` | 订单明细表 | order_no, product_id, product_name, price, quantity |
| `t_payment_log` | 支付日志表 | payment_no, order_no, status（0待支付/1成功/2失败）, retry_count |

## 🎯 项目亮点

1. **库存防超卖**：基于 MyBatis-Plus 的 `@Version` 注解实现乐观锁，防止高并发下超卖
2. **支付最终一致性**：采用**本地消息表 + 定时任务补偿**方案，定时任务（每 5 分钟）扫描超过 10 分钟未支付的记录进行主动查询，最多重试 3 次
3. **超时订单自动处理**：独立定时任务扫描超过 30 分钟未支付订单，自动恢复库存并关闭订单（状态码 3）
4. **购物车高性能**：使用 **Redis Hash** 结构（`cart:user:{userId}`），实现购物车商品的 O(1) 读写
5. **严格幂等设计**：支付回调接口通过唯一索引 + 状态机双重校验，防止重复回调导致数据错乱
6. **统一状态码规范**：基于 `ResultCodeEnum` 分段管理错误码（如 3000-3999 为商品模块），业务语义清晰
7. **订单状态机**：枚举封装状态流转，提供 `canPay()`、`canCancel()`、`isFinalStatus()` 等业务判断方法

## 📝 待优化项

- [ ] 对接支付宝沙箱（真实支付体验）
- [ ] Redis 预扣库存（高并发优化）
- [ ] RabbitMQ 延迟消息（超时关单实时化）
- [ ] 单元测试覆盖
- [ ] JMeter 压测报告

## 📄 许可证

本项目仅供学习交流使用。

## 📧 联系方式

- GitHub：https://github.com/pengshuodev/xingyun-mall
- Email：m18526612169@163.com

## 🙏 致谢

- Spring Boot 官方文档
- MyBatis-Plus 官方文档
- 阿里巴巴 Java 开发手册
```
