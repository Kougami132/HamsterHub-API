# Hamster Hub

Hamster Hub网盘的后端程序  
开发中......

## 目录结构
    Hamster Hub/
    ├── hamsterhub-application/ # 接口模块, 主程序
    │ ├── src/
    │ │ ├── main/
    │ │ │ ├── java/
    │ │ │ │ ├── com/
    │ │ │ │ │ ├── hamsterhub/
    │ │ │ │ │ │ ├── annotation/ # 自定义注解
    │ │ │ │ │ │ ├── config/ # 一些配置与 Bean
    │ │ │ │ │ │ ├── controller/ # 接口
    │ │ │ │ │ │ ├── convert/ # 自动转换类
    │ │ │ │ │ │ ├── initialize/ # 初始化
    │ │ │ │ │ │ ├── interceptor/ # 拦截器
    │ │ │ │ │ │ ├── response/ # 响应类
    │ │ │ │ │ │ ├── util/ # 工具类
    │ │ │ │ │ │ ├── vo/ # 接收类
    │ │ │ │ │ │ ├── webdav/ # WebDAV相关
    │ │ │ ├── resources/
    │ │ │ │ ├── application.yml # 配置文件
    │ │ │ │ ├── schema.sql # 数据库初始化脚本
    │ ├── pom.xml # 子模块 Maven 配置文件
    ├── hamsterhub-service/ # 业务模块
    ├── hamsterhub-database/ # 持久化模块
    ├── hamsterhub-common/ # 通用模块
    ├── pom.xml # Maven 配置文件, 统一所有依赖版本
    ├── .gitignore # Git 忽略文件配置
    ├── Dockerfile # Docker 镜像构建文件
    ├── docker-compose.yml # Docker Compose 部署配置文件
    └── README.md # 项目说明文件

## TODO

### 基础功能

- [ ] **文件操作**
    - [x] **上传**
        - [x] **秒传**
    - [x] **下载**
    - [x] **删除**
    - [x] **查看**
    - [x] **复制**
    - [x] **移动**
    - [ ] **迁移**
    - [x] **重命名**
    - [x] **覆盖**
    - [ ] **分享**
        - [x] **新增**
            - [x] **公开**
            - [x] **需要密码**
        - [ ] **删除**
            - [x] **手动删除**
            - [x] **定时删除**
            - [ ] **限定访问次数**
        - [ ] **修改**
            - [ ] **修改限制时间**
            - [ ] **修改限制次数**
        - [x] **下载**
- [ ] **目录操作**
    - [x] **创建**
    - [x] **删除**
    - [x] **查看**
    - [ ] **复制**
    - [x] **移动**
    - [ ] **迁移**
    - [x] **重命名**
- [x] **存储设备**
- [x] **存储策略**
    - [ ] **聚合存储策略**
        - [ ] **存储优先级**
            - [x] **剩余空间大小**
            - [ ] **手动**
    - [ ] **备份策略**
        - [ ] **定时备份**
- [x] **统一API返回格式**
- [x] **用户系统**
- [x] **权限组系统**
- [ ] **任务系统**
- [x] **Redis缓存**
- [ ] **暂存区**

### 设备适配

- [x] **本地磁盘**
- [x] **阿里云**
- [ ] **OneDrive**
- [ ] **Samba**
- [ ] **WebDAV**

### 拓展功能

- [ ] **公共空间**
- [ ] **流量管控**
- [x] **WebDAV**
- [x] **离线下载**
- [x] **RSS订阅**