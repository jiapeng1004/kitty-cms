# 🖼️ Kitty Material

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk" alt="Java 25"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=flat-square&logo=springboot" alt="Spring Boot 4"/>
</p>

<p align="center">
  <strong>Material Management Service</strong> · 素材管理服务
</p>

---

## 📖 简介 | Introduction

**Kitty Material** 是 Kitty CMS 的素材管理微服务，负责图片、视频等媒体素材的存储、元数据管理与访问。

**Kitty Material** is the material management microservice for Kitty CMS, handling storage, metadata, and access for media assets such as images and videos.

---

## 🏗️ 结构 | Structure

```
kitty-material/
├── kitty-material-api/     # DTO、接口定义
├── kitty-material-func/    # 业务逻辑
└── kitty-material-server/  # Spring Boot 入口
```

### 模块说明 | Module Overview

| 模块 | 职责 |
|------|------|
| [kitty-material-api](kitty-material-api) | DTO、接口、常量 |
| [kitty-material-func](kitty-material-func) | 素材 CRUD、存储逻辑 |
| [kitty-material-server](kitty-material-server) | Spring Boot 应用、配置、入口 |

---

## 🚀 构建 | Build

```bash
# 从仓库根目录
./mvnw -B -pl kitty-material/kitty-material-server -am clean package -DskipTests
```

---

## ⚙️ 配置 | Configuration

配置文件：`kitty-material-server/src/main/resources/application.yml`

---

## 🏃 运行 | Run

```bash
java -jar kitty-material/kitty-material-server/target/*.jar
```

---

## 📄 许可证 | License

[Apache-2.0](../LICENSE)
