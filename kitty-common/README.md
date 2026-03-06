# 📦 Kitty Common

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk" alt="Java 25"/>
  <img src="https://img.shields.io/badge/Module-Common-6DB33F?style=flat-square" alt="Common"/>
</p>

<p align="center">
  <strong>Shared utilities & base components</strong> · 公共工具与基础能力
</p>

---

## 📖 简介 | Introduction

**Kitty Common** 是 Kitty CMS  monorepo 的公共模块，提供各业务模块共用的工具类、注解、DTO 及基础依赖封装。

**Kitty Common** provides shared utilities, annotations, DTOs, and base dependencies for all Kitty CMS modules.

---

## 🏗️ 结构 | Structure

```
kitty-common/
└── kitty-common-core/    # 核心公共库
```

### 模块说明 | Module Overview

| 模块 | 职责 |
|------|------|
| [kitty-common-core](kitty-common-core) | Swagger 注解、Lombok、gRPC、MyBatis-Plus、Spring Data 等 provided 依赖；通用工具与常量 |

---

## 🚀 构建 | Build

```bash
# 从仓库根目录
./mvnw -B -pl kitty-common -am clean package -DskipTests
```

---

## 📦 依赖 | Dependencies

本模块被 `kitty-user`、`kitty-cms`、`kitty-material`、`kitty-transcoder` 等引用，作为基础依赖。

---

## 📄 许可证 | License

[Apache-2.0](../LICENSE)
