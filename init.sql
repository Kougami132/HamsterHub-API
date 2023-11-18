CREATE DATABASE IF NOT EXISTS hamster_db DEFAULT CHARACTER SET = utf8mb4;

USE hamster_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
                           `ID` bigint(20) NOT NULL COMMENT '主键',
                           `USERNAME` varchar(50) NOT NULL COMMENT '用户名',
                           `PASSWORD` varchar(50) NOT NULL COMMENT '密码',
                           `TYPE` bigint(10) NOT NULL COMMENT '用户类型',
                           `EMAIL` varchar(50) NULL COMMENT '邮箱',
                           PRIMARY KEY (`ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
                          `ID` bigint(20) NOT NULL COMMENT '主键',
                          `NAME` varchar(50) NOT NULL UNIQUE COMMENT '设备名称',
                          `TYPE` bigint(10) NOT NULL COMMENT '设备类型',
                          `PARAM` varchar(50) NOT NULL COMMENT '连接参数',
                          PRIMARY KEY (`ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

DROP TABLE IF EXISTS `strategy`;
CREATE TABLE `strategy` (
                            `ID` bigint(20) NOT NULL COMMENT '主键',
                            `NAME` varchar(50) NOT NULL UNIQUE COMMENT '配置名称',
                            `TYPE` bigint(10) NOT NULL COMMENT '存储策略（聚合、备份）',
                            `MODE` bigint(10) NOT NULL COMMENT '存储模式（存储优先级）',
                            `PERMISSION` bigint(10) NOT NULL COMMENT '权限',
                            `ROOT` varchar(50) NOT NULL COMMENT '虚拟路径根目录',
                            PRIMARY KEY (`ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

DROP TABLE IF EXISTS `device_strategy`;
CREATE TABLE `device_strategy` (
                                   `ID` bigint(20) NOT NULL COMMENT '主键',
                                   `DEVICE_ID` bigint(20) NOT NULL UNIQUE COMMENT '设备ID',
                                   `STRATEGY_ID` bigint(20) NOT NULL COMMENT '存储策略ID',
                                   PRIMARY KEY (`ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

DROP TABLE IF EXISTS `r_file`;
CREATE TABLE `r_file` (
                          `ID` bigint(20) NOT NULL COMMENT '主键',
                          `NAME` varchar(50) NOT NULL COMMENT '文件名',
                          `HASH` varchar(50) NOT NULL UNIQUE COMMENT '文件hash',
                          `PATH` varchar(50) NOT NULL COMMENT '文件实际路径',
                          `SIZE` bigint(20) NOT NULL COMMENT '文件大小',
                          `DEVICE_ID` bigint(20) NOT NULL COMMENT '设备ID',
                          PRIMARY KEY (`ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

DROP TABLE IF EXISTS `v_file`;
CREATE TABLE `v_file` (
                          `ID` bigint(20) NOT NULL COMMENT '主键',
                          `TYPE` bigint(10) NOT NULL COMMENT '文件类型',
                          `NAME` varchar(50) NOT NULL COMMENT '文件名',
                          `PATH` varchar(50) NOT NULL COMMENT '文件虚拟路径',
                          `R_FILE_ID` bigint(20) NOT NULL COMMENT '实际文件id',
                          `VERSION` bigint(20) NOT NULL COMMENT '文件版本',
                          `TIMESTAMP` DATETIME NOT NULL COMMENT '文件最后修改时间',
                          `ACCOUNT_ID` bigint(20) NOT NULL COMMENT '用户ID',
                          `PERMISSION` bigint(10) NOT NULL COMMENT '文件权限（私有、公开）',
                          `STRATEGY_ID` bigint(20) NOT NULL COMMENT '存储策略ID',
                          PRIMARY KEY (`ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;