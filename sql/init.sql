-- ==================== 一键初始化脚本 ====================
-- 使用方法：在 MySQL 中执行 source /path/to/init.sql
-- 或直接复制全部内容在 TablePlus/IDEA 中执行

-- 1. 创建数据库（如果不存在）
CREATE
DATABASE IF NOT EXISTS `xingyun_mall`
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE
`xingyun_mall`;

-- ==================== 建表语句 ====================

-- 1. 用户表
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`
(
    `id`              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username`        VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password`        VARCHAR(60) NOT NULL COMMENT '密码（BCrypt加密）',
    `phone`           VARCHAR(20) UNIQUE COMMENT '手机号',
    `email`           VARCHAR(100) UNIQUE COMMENT '邮箱',
    `gender`          TINYINT  DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    `status`          TINYINT  DEFAULT 1 COMMENT '状态 1正常 0禁用',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip`   VARCHAR(45) COMMENT '最后登录IP',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      TINYINT  DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
    INDEX             idx_username (`username`),
    INDEX             idx_phone (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 商品表
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    `name`        VARCHAR(200)   NOT NULL COMMENT '商品名称',
    `price`       DECIMAL(10, 2) NOT NULL COMMENT '价格（元）',
    `stock`       INT            NOT NULL DEFAULT 0 COMMENT '库存数量',
    `description` VARCHAR(500) COMMENT '商品描述',
    `status`      TINYINT        NOT NULL DEFAULT 1 COMMENT '状态 1-上架 0-下架',
    `category_id` BIGINT COMMENT '分类ID',
    `image_url`   VARCHAR(255) COMMENT '商品图片URL',
    `version`     INT                     DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT                 DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删',
    INDEX         idx_status (`status`),
    INDEX         idx_category (`category_id`),
    INDEX         idx_name (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 3. 订单表
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order`
(
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `order_no`     VARCHAR(32)    NOT NULL UNIQUE COMMENT '订单号（雪花算法）',
    `user_id`      BIGINT         NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    `status`       TINYINT        NOT NULL DEFAULT 0 COMMENT '状态 0-待支付 1-已支付 2-已取消 3-已关闭',
    `payment_time` DATETIME COMMENT '支付时间',
    `remark`       VARCHAR(200) COMMENT '订单备注',
    `create_time`  DATETIME                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`   TINYINT                 DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删',
    INDEX          idx_order_no (`order_no`),
    INDEX          idx_user_id (`user_id`),
    INDEX          idx_status (`status`),
    INDEX          idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 4. 订单明细表
DROP TABLE IF EXISTS `t_order_item`;
CREATE TABLE `t_order_item`
(
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `order_no`     VARCHAR(32)    NOT NULL COMMENT '订单号',
    `product_id`   BIGINT         NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(200)   NOT NULL COMMENT '商品名称（快照）',
    `price`        DECIMAL(10, 2) NOT NULL COMMENT '下单时价格（快照）',
    `quantity`     INT            NOT NULL COMMENT '购买数量',
    `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX          idx_order_no (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 5. 支付日志表（本地消息表）
DROP TABLE IF EXISTS `t_payment_log`;
CREATE TABLE `t_payment_log`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `payment_no`    VARCHAR(32)    NOT NULL UNIQUE COMMENT '支付流水号（雪花算法）',
    `order_no`      VARCHAR(32)    NOT NULL COMMENT '订单号',
    `amount`        DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `status`        TINYINT        NOT NULL DEFAULT 0 COMMENT '状态 0-待支付 1-支付成功 2-支付失败',
    `callback_data` TEXT COMMENT '支付回调原始数据（JSON格式）',
    `retry_count`   INT                     DEFAULT 0 COMMENT '重试次数',
    `error_msg`     VARCHAR(500) COMMENT '错误信息',
    `create_time`   DATETIME                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX           idx_order_no (`order_no`),
    INDEX           idx_status (`status`),
    INDEX           idx_payment_no (`payment_no`),
    INDEX           idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付日志表（本地消息表）';

-- ==================== 测试数据 ====================

-- 插入测试用户（密码：123456）
-- BCrypt 加密后的密码：$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr6Rj5qQjZ5sB6FQqZ5sB6FQqZ5sB6
INSERT INTO `t_user` (`username`, `password`, `phone`, `status`)
VALUES ('test001', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr6Rj5qQjZ5sB6FQqZ5sB6FQqZ5sB6', '13800138001', 1),
       ('test002', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr6Rj5qQjZ5sB6FQqZ5sB6FQqZ5sB6', '13800138002', 1);

-- 插入测试商品
INSERT INTO `t_product` (`name`, `price`, `stock`, `description`, `status`, `version`)
VALUES ('Java编程思想', 99.00, 100, 'Java经典书籍，深入理解Java', 1, 0),
       ('Spring Boot实战', 79.00, 50, 'Spring Boot从入门到精通', 1, 0),
       ('Redis深度历险', 49.00, 200, 'Redis核心原理与应用实践', 1, 0),
       ('MySQL必知必会', 39.00, 80, 'MySQL快速入门教程', 1, 0),
       ('设计模式之禅', 69.00, 30, '设计模式经典书籍', 1, 0);

-- ==================== 验证 ====================

-- 查看所有表
SHOW
TABLES;

-- 查看测试数据
SELECT '用户表' AS '表名', COUNT(*) AS '记录数'
FROM t_user
UNION ALL
SELECT '商品表', COUNT(*)
FROM t_product;