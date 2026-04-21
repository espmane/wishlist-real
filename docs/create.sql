DROP SCHEMA IF EXISTS wishlist;
CREATE SCHEMA wishlist;
USE
wishlist;

CREATE TABLE users
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email    VARCHAR(64)  NOT NULL UNIQUE
);

CREATE TABLE wish
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    link VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(64)        NOT NULL,
    price DOUBLE(5, 2) NOT NULL
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