CREATE DATABASE IF NOT EXISTS hamster_db DEFAULT CHARACTER SET = utf8mb4;

USE hamster_db;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `account` (
                                         `ID` bigint(20) NOT NULL COMMENT '主键',
    `USERNAME` varchar(50) NOT NULL COMMENT '用户名',
    `PASSWORD` varchar(50) NOT NULL COMMENT '密码',
    `PASS_MODIFIED` DATETIME NOT NULL COMMENT '密码最后修改时间',
    `TYPE` bigint(10) NOT NULL DEFAULT '1' COMMENT '用户类型',
    `PHONE` bigint(10) NULL COMMENT '手机号',
    `EMAIL` varchar(50) NULL COMMENT '邮箱',
    PRIMARY KEY (`ID`) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `device` (
                                        `ID` bigint(20) NOT NULL COMMENT '主键',
    `NAME` varchar(50) NOT NULL UNIQUE COMMENT '设备名称',
    `TYPE` bigint(10) NOT NULL COMMENT '设备类型',
    `PARAM` TEXT NOT NULL COMMENT '连接参数',
    `CONFIGURED` TINYINT(1) NOT NULL COMMENT '连接参数',
    PRIMARY KEY (`ID`) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `strategy` (
                                          `ID` bigint(20) NOT NULL COMMENT '主键',
    `NAME` varchar(50) NOT NULL COMMENT '配置名称',
    `TYPE` bigint(10) NOT NULL COMMENT '存储策略（聚合、备份）',
    `MODE` bigint(10) NOT NULL COMMENT '存储模式（存储优先级）',
    `PERMISSION` bigint(10) NOT NULL COMMENT '权限',
    `ROOT` varchar(50) NOT NULL COMMENT '虚拟路径根目录',
    `FILE_SYSTEM` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '文件组织形式0为虚拟目录 1为真实目录',
    `PARAM` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '表示存储设备',
    PRIMARY KEY (`ID`) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `r_file` (
                                        `ID` bigint(20) NOT NULL COMMENT '主键',
    `NAME` varchar(50) NOT NULL COMMENT '文件名',
    `HASH` varchar(50) NOT NULL COMMENT '文件hash',
    `PATH` varchar(200) NOT NULL COMMENT '文件实际路径',
    `SIZE` bigint(20) NOT NULL COMMENT '文件大小',
    `DEVICE_ID` bigint(20) NOT NULL COMMENT '设备ID，临时目录使用-1',
    PRIMARY KEY (`ID`) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `v_file` (
                                        `ID` bigint(20) NOT NULL COMMENT '主键',
    `TYPE` bigint(10) NOT NULL COMMENT '文件类型',
    `NAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件名',
    `PARENT_ID` bigint(20) NOT NULL COMMENT '父文件id',
    `R_FILE_ID` bigint(20) NOT NULL COMMENT '实际文件id',
    `VERSION` bigint(20) NOT NULL DEFAULT '1' COMMENT '文件版本',
    `CREATED` datetime NOT NULL COMMENT '文件创建时间',
    `MODIFIED` datetime NOT NULL COMMENT '文件最后修改时间',
    `ACCOUNT_ID` bigint(20) NOT NULL COMMENT '用户ID',
    `SIZE` bigint(20) NOT NULL COMMENT '文件大小',
    `STRATEGY_ID` bigint(20) NOT NULL COMMENT '存储策略ID',
    `SHARE_TYPE` bigint(10) NOT NULL DEFAULT '0' COMMENT '分享类型,0继承,1分享,2不分享',
    `HASH` varchar(50) NOT NULL,
    PRIMARY KEY (`ID`) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `file_link` (
                                           `ID` bigint(20) NOT NULL COMMENT '主键',
    `TICKET` varchar(50) NOT NULL UNIQUE COMMENT '分享码',
    `R_FILE_ID` bigint(20) NOT NULL COMMENT '实际文件id',
    `EXPIRY` DATETIME NOT NULL COMMENT '过期时间',
    `PATH` varchar(1024) DEFAULT '' COMMENT '存储实际位置',
    PRIMARY KEY (`ID`) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `share` (
                                       `ID` bigint(20) NOT NULL COMMENT '主键',
    `TYPE` bigint(10) NOT NULL COMMENT '分享类型(公开、私有)',
    `TICKET` varchar(50) NOT NULL UNIQUE COMMENT '分享码',
    `FILE_INDEX` varchar(512) NOT NULL COMMENT '虚拟文件索引',
    `KEY` varchar(50) NULL COMMENT '提取码',
    `EXPIRY` DATETIME NOT NULL COMMENT '分享过期时间',
    `ACCOUNT_ID` bigint(20) NOT NULL COMMENT '用户ID',
    `NAME` varchar(50) DEFAULT NULL COMMENT '分享的名称',
    `ROOT` varchar(50) NOT NULL DEFAULT '所处策略的ROOT',
    PRIMARY KEY (`ID`) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `sys_config` (
                                            `key` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
    `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '',
    `order_id` int(11) NOT NULL DEFAULT '0',
    `hide` tinyint(1) unsigned NOT NULL COMMENT '0 表示不隐藏，1为隐藏，隐藏时数据不会发给前端',
    PRIMARY KEY (`key`) USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `rss_list` (
                                          `ID` bigint(20) NOT NULL,
    `URL` varchar(512) NOT NULL COMMENT 'rss的订阅地址',
    `USER_ID` bigint(20) NOT NULL COMMENT '用户id',
    `ROOT` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储的策略root',
    `PARENT_INDEX` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '绑定的存储位置',
    `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
    `STATE` int(4) NOT NULL COMMENT '状态',
    `LAST_HASH` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '最后一次获取的文本hash',
    `REPLACE_HOST` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用于替换的域名，不为空时将替换种子的域名',
    `MIRROR_HOST` varchar(255) DEFAULT NULL COMMENT '使用镜像站代理种子下载',
    `DOWNLOADER` int(4) NOT NULL DEFAULT '1' COMMENT '指定下载器',
    `FILTER` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '使用正则，当不为空时，匹配这条规则才会被加入任务列表',
    PRIMARY KEY (`ID`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `rss_task` (
                                          `ID` bigint(20) NOT NULL,
    `USER_ID` bigint(20) NOT NULL COMMENT '用户id',
    `RSS_LIST_ID` bigint(20) NOT NULL,
    `TITLE` varchar(255) NOT NULL,
    `URL` varchar(512) NOT NULL COMMENT '种子地址',
    `PUB_DATE` datetime NOT NULL,
    `SIZE` bigint(20) NOT NULL COMMENT 'rss订阅的声明尺寸',
    `STATE` int(4) NOT NULL COMMENT '状态',
    PRIMARY KEY (`ID`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `download_task` (
                                               `ID` bigint(20) NOT NULL,
    `NAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件名称',
    `STATE` int(4) NOT NULL COMMENT '当前状态',
    `DOWNLOADER` int(4) NOT NULL COMMENT '使用的下载器',
    `TYPE` int(4) NOT NULL COMMENT '类型',
    `URL` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '下载的地址',
    `ORIGIN_TYPE` int(4) NOT NULL COMMENT '来源的类型',
    `ORIGIN_ID` bigint(20) NOT NULL COMMENT '来源的id',
    `ROOT` varchar(50) DEFAULT NULL COMMENT '下载结束后存储的策略',
    `PARENT_INDEX` varchar(255) DEFAULT NULL COMMENT '下载结束后存储的位置',
    `USER_ID` bigint(20) NOT NULL COMMENT '用户id',
    `TAG` varchar(32) NOT NULL COMMENT '下载的标记',
    `TASK_INDEX` varchar(32) NOT NULL COMMENT '用于获取下载器的任务的索引',
    PRIMARY KEY (`ID`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;