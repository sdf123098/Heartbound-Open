# Heartbound-Open

> 🔞 **提示**：本项目为 Minecraft 成人/NSFW 模组 **姬伴 (Heartbound / Pleasure Horizons)** 的开源恢复与移植工程。包含成人内容，未满 18 岁请勿浏览或使用。

[![License: CC0-1.0](https://img.shields.io/badge/License-CC0--1.0-blue.svg)](LICENSE_Heartbound)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)](https://fabricmc.net/)
[![Fabric Loom](https://img.shields.io/badge/Fabric--Loom-1.17.17-blue.svg)](https://fabricmc.net/)
[![Build Status](https://img.shields.io/badge/Build-Passing-success.svg)](#构建与运行)

---

## 📌 项目简介

**Heartbound-Open** 是针对无源码 Fabric 1.21.1 模组 `Heartbound 0.6.9+1.21.1_WIP3` 进行**逆向工程、反编译恢复、修复与跨版本移植**的完整开源工作区。

本项目已完成对原始 intermediary 字节码 JAR 的反编译与 Yarn 命名空间重映射，并成功修复了反编译产生的全部 70 处编译错误。目前恢复工程可以在标准 Fabric 开发环境中通过 `./gradlew build` 编译通过并正常运行。

---

## 🛠️ 项目特色与状态

- 🔞 **NSFW / 成人模组**：完整还原原模组的动态场景、实体交互、GUI 界面与动画逻辑。
- 🔄 **源码 100% 恢复**：
  - 将 310 个 Class 重映射至 Fabric **Yarn** 命名空间 (`1.21.1+build.3`)；
  - 完整修复反编译产生的 Mixin 双转型、SBL 泛型推断退化、Switch 变量作用域混淆等 70 组编译问题；
  - 产物 Mixin 注解字符串经 Loom static remap 校验，与原版 JAR 逐字节映射一致。
- ⚡ **完备的构建工具链**：集成 Gradle 9.5.1 + Fabric Loom 1.17.17，支持开箱即用构建与运行。

---

## 📂 项目结构

```text
Heartbound-Open/
├── restore-1.21.1/         # 1.21.1 源码恢复工程（完整可构建基线）
│   ├── src/main/java/      # 恢复后的 Yarn 命名空间 Java 源码 (310 个类)
│   ├── src/main/resources/ # 原始模组资源（模型、动画、音效、语言包等）
│   └── build.gradle        # Fabric Loom 构建脚本
├── port-26.1.2/            # 26.1.2 / 跨版本移植工作目录
├── PORTING_NOTES.md        # 详细的逆向分析、修复日志与网络/Mixin 清单
└── README.md               # 项目说明文档
```

---

## 🚀 构建与运行

### 前置要求
- **Java**：JDK 21 或更高版本
- **构建工具**：项目自带 Gradle Wrapper（无需单独安装）

### 编译步骤

1. 克隆本项目：
   ```bash
   git clone https://github.com/sdf123098/Heartbound-Open.git
   cd Heartbound-Open/restore-1.21.1
   ```

2. 运行构建命令：
   ```bash
   ./gradlew build
   ```
   *构建产物 JAR 将输出至 `restore-1.21.1/build/libs/` 目录下。*

3. 启动开发客户端进行验证：
   ```bash
   ./gradlew runClient
   ```

---

## 📄 版权与免责声明

1. 本模组包含成人/NSFW 内容，请遵守当地法律法规。
2. 原始模组元数据 `fabric.mod.json` 标记为 `CC0-1.0` 许可，但随包分发文本含 `LICENSE_Heartbound` 声明。本项目仅出于技术学习、代码恢复与社区版本移植目的开源，版权归原作者所有。
