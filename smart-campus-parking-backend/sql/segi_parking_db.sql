CREATE DATABASE IF NOT EXISTS segi_parking_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE segi_parking_db;

CREATE TABLE IF NOT EXISTS parking_approvals
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id        VARCHAR(50)  NOT NULL,
    target_bay_code VARCHAR(10)  NOT NULL,
    plate           VARCHAR(30)  NOT NULL,
    date_range      VARCHAR(100) DEFAULT '',
    apply_time      VARCHAR(50)  DEFAULT '',
    status          VARCHAR(20)  DEFAULT 'Pending',
    is_notified     INT          DEFAULT 0
) CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS parking_bays
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    bay_code     VARCHAR(10)  UNIQUE,
    status       VARCHAR(20)  DEFAULT 'Vacant',
    plate        VARCHAR(100) DEFAULT '',
    staff_id     VARCHAR(50)  DEFAULT '',
    expiry_date  VARCHAR(100) DEFAULT '',
    shared_dates VARCHAR(100) DEFAULT ''
) CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_number VARCHAR(50) DEFAULT '',
    staff_id    VARCHAR(50) NOT NULL UNIQUE,
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20) NOT NULL,
    create_time VARCHAR(50) DEFAULT ''
) CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS waiting_list
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    staff_id   VARCHAR(50),
    plate      VARCHAR(30),
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status     VARCHAR(20) DEFAULT 'Pending'
) CHARACTER SET utf8mb4;

INSERT INTO parking_bays (bay_code)
VALUES
    ('A01'), ('A02'), ('A03'),
    ('A04'), ('A05'), ('A06'),
    ('A07'), ('A08'), ('A09');

-- Local demonstration accounts only.
-- Admin login: demo_admin / AdminDemo123!
-- Staff login: demo_staff / StaffDemo123!

INSERT INTO sys_user
    (user_number, staff_id, username, password, role, create_time)
VALUES
    (
        'DEMO-ADMIN',
        'DEMO-ADMIN',
        'demo_admin',
        '$2b$12$scsX46LGE2y/B9gMnYLzGuFaLS/AJ60fnMcg6CB9RM4x2Lv/rGne6',
        'ADMIN',
        '2026-09-15'
    ),
    (
        'DEMO-STAFF',
        'DEMO-STAFF',
        'demo_staff',
        '$2b$12$PwWYMOkuSxXvKO6nvhEWi.j1evJN7E8FRL.D1O2hlIhdCmntQlcqu',
        'USER',
        '2026-09-15'
    );
