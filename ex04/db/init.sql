DROP TABLE IF EXISTS delivery_tb;
DROP TABLE IF EXISTS order_item_tb;
DROP TABLE IF EXISTS order_tb;
DROP TABLE IF EXISTS product_tb;
DROP TABLE IF EXISTS user_tb;

CREATE TABLE user_tb (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50),
  email VARCHAR(50),
  password VARCHAR(50),
  roles VARCHAR(50),
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE product_tb (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_name VARCHAR(50),
  quantity INT,
  price BIGINT,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE order_tb (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  product_id INT,
  quantity INT,
  status VARCHAR(50),
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE order_item_tb (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT,
  product_id INT,
  quantity INT,
  price BIGINT,
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE delivery_tb (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT,
  address VARCHAR(50),
  status VARCHAR(50),
  created_at DATETIME,
  updated_at DATETIME
);

INSERT INTO user_tb (username, email, password, roles, created_at, updated_at) VALUES ('ssar','ssar@metacoding.com','1234','USER',now(),now());
INSERT INTO user_tb (username, email, password, roles, created_at, updated_at) VALUES ('cos','cos@metacoding.com','1234','USER',now(),now());
INSERT INTO user_tb (username, email, password, roles, created_at, updated_at) VALUES ('love','love@metacoding.com','1234','USER',now(),now());

-- 재고 부족 문제 해결을 위해 100으로 설정 (원래값: 10, 3, 10)
INSERT INTO product_tb (product_name, quantity, price, created_at, updated_at) VALUES ('MacBook Pro', 100, 2500000, now(), now());
INSERT INTO product_tb (product_name, quantity, price, created_at, updated_at) VALUES ('iPhone 15', 100, 1300000, now(), now());
INSERT INTO product_tb (product_name, quantity, price, created_at, updated_at) VALUES ('AirPods', 100, 300000, now(), now());

-- 기존 샘플 주문/배달 데이터 복구 (단, 위에서 재고를 바꿨으니 여기 주문된 수량만큼은 사실 빠졌어야 정상이지만, 테스트용 샘플이니 그대로 둡니다)
INSERT INTO order_tb (user_id, product_id, quantity, status, created_at, updated_at) VALUES (1, 1, 1, 'COMPLETED', now(), now()); 
INSERT INTO order_tb (user_id, product_id, quantity, status, created_at, updated_at) VALUES (2, 3, 1, 'CANCELLED', now(), now()); 
INSERT INTO order_tb (user_id, product_id, quantity, status, created_at, updated_at) VALUES (3, 2, 2, 'PENDING', now(), now());

INSERT INTO order_item_tb (order_id, product_id, quantity, price, created_at, updated_at) VALUES (1, 1, 1, 2500000, now(), now());
INSERT INTO order_item_tb (order_id, product_id, quantity, price, created_at, updated_at) VALUES (2, 3, 1, 300000, now(), now());
INSERT INTO order_item_tb (order_id, product_id, quantity, price, created_at, updated_at) VALUES (3, 2, 2, 1300000, now(), now());

INSERT INTO delivery_tb (order_id, address, status, created_at, updated_at) VALUES (1, 'Addr 1', 'COMPLETED', NOW(), NOW());
INSERT INTO delivery_tb (order_id, address, status, created_at, updated_at) VALUES (2, 'Addr 2', 'COMPLETED', NOW(), NOW());
INSERT INTO delivery_tb (order_id, address, status, created_at, updated_at) VALUES (3, 'Addr 3', 'COMPLETED', NOW(), NOW());
