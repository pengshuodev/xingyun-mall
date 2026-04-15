-- 测试数据
-- 用户测试数据（密码是 BCrypt 加密后的 '123456'）
INSERT INTO `t_user` (`username`, `password`, `phone`)
VALUES ('test', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr6Rj5qQjZ5sB6FQqZ5sB6FQqZ5sB6', '13800138000');

-- 商品测试数据
INSERT INTO `t_product` (`name`, `price`, `stock`, `description`, `status`)
VALUES ('Java编程思想', 99.00, 100, 'Java经典书籍，深入理解Java', 1),
       ('Spring Boot实战', 79.00, 50, 'Spring Boot从入门到精通', 1),
       ('Redis深度历险', 49.00, 200, 'Redis核心原理与应用实践', 1),
       ('MySQL必知必会', 39.00, 80, 'MySQL快速入门教程', 1);





