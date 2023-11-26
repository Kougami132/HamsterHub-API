USE hamster_db;

/* 初始化用户，密码为admin132 */
INSERT INTO account (ID, USERNAME, PASSWORD, TYPE)
VALUES (0, 'admin', '66854f1b110143269dbffdd806fa66eb', 1)
ON DUPLICATE KEY UPDATE ID = ID;
