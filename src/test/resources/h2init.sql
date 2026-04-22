DROP SCHEMA IF EXISTS wishlist;
CREATE SCHEMA wishlist;
USE wishlist;

CREATE TABLE users
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE wish
(
    id    INT AUTO_INCREMENT PRIMARY KEY,
    link  VARCHAR(255)   NOT NULL,
    name  VARCHAR(255)   NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE wishlist
(
    id      INT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(64) NOT NULL,
    user_id INT         NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE (user_id, name)
);

CREATE TABLE wishlist_wish
(
    wishlist_id INT NOT NULL,
    wish_id     INT NOT NULL,
    PRIMARY KEY (wishlist_id, wish_id),
    FOREIGN KEY (wishlist_id) REFERENCES wishlist (id) ON DELETE CASCADE,
    FOREIGN KEY (wish_id) REFERENCES wish (id) ON DELETE CASCADE
);

-- init-data
USE wishlist;

INSERT INTO users (username, password)
VALUES ('julius', 'pass123'),
       ('marie', 'pass456'),
       ('peter', 'pass789'),
       ('user', 'user');

INSERT INTO wish (link, name, price)
VALUES ('https://www.elgiganten.dk/airpods-pro', 'AirPods Pro', 249.99),
       ('https://www.lego.com/da-dk/product/42156', 'LEGO Technic Peugeot', 199.99),
       ('https://www.zalando.dk/adidas-samba', 'Adidas Samba', 899.99),
       ('https://www.elgiganten.dk/iphone-15', 'iPhone 15', 7999.99),
       ('https://www.coolshop.dk/nintendo-switch', 'Nintendo Switch', 2299.99),
       ('https://www.saxo.com/dk/atomvaner', 'Atomvaner - James Clear', 149.99),
       ('https://www.ikea.com/dk/da/p/kallax-reolsystem', 'IKEA Kallax Reol', 499.99),
       ('https://www.elgiganten.dk/samsung-galaxy-watch', 'Samsung Galaxy Watch 6', 2499.99),
       ('https://www.garmin.com/da-DK/forerunner265', 'Garmin Forerunner 265', 3499.99),
       ('https://www.bog-ide.dk/kindle-paperwhite', 'Kindle Paperwhite', 1099.99),
       ('https://www.lego.com/da-dk/product/botanical', 'LEGO Botanicals Blomster', 399.99),
       ('https://www.zalando.dk/nike-air-force-1', 'Nike Air Force 1', 849.99),
       ('https://www.whisky.dk/johnnie-walker-blue', 'Johnnie Walker Blue Label', 1299.99),
       ('https://www.elgiganten.dk/airpods-max', 'AirPods Max', 4299.99),
       ('https://www.coolshop.dk/playstation-5', 'PlayStation 5', 3999.99);

INSERT INTO wishlist (name, user_id)
VALUES ('Fødselsdag', 1),
       ('Jul', 1),
       ('Sommer', 1),
       ('Ønsker', 2),
       ('Jul', 2),
       ('Gadgets', 3),
       ('Ønsker', 4);

INSERT INTO wishlist_wish (wishlist_id, wish_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 7),
       (2, 4),
       (2, 5),
       (2, 11),
       (3, 8),
       (3, 9),
       (3, 12),
       (4, 6),
       (4, 10),
       (5, 14),
       (5, 15),
       (6, 1),
       (6, 4),
       (6, 9),
       (6, 13),
       (7, 2),
       (7, 5),
       (7, 6),
       (7, 11);
