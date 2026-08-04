# Heartbound-Open v0.6.9+1.21.1_WIP3 (源码还原与编译修复版)

🔞 **提示**：本模组为 Minecraft 成人/NSFW 内容模组 **姬伴 (Heartbound / Pleasure Horizons)** 的开源恢复版本。

---

## 🌟 发行说明 (Release Overview)

本 Release 为 **Heartbound 0.6.9+1.21.1_WIP3** 的完整源码还原与重新编译产物。
原始模组无公开源码且发布为 Fabric Intermediary 命名空间字节码。本项目成功完成了全量逆向重映射、反编译与 70 处编译错误的修复，建立了 100% 干净通过构建的 1.21.1 基线工程。

---

## 📦 包含资产 (Release Assets)

- **`heartbound-0.6.9+1.21.1_WIP3.jar`**：
  - 基于还原修复后的 Yarn 源码，在 Fabric Loom 1.17.17 + Gradle 9.5.1 + Java 21 环境下全新编译构建的模组 JAR。
  - 产物 Mixin 注解经过 static remap，补全与原版模组一致的字节码拦截点。

---

## ⚙️ 技术修复与还原亮点 (Technical Summary)

1. **Yarn 命名空间全量重映射**：
   - 310 个 Class 全量映射至 `Yarn 1.21.1+build.3`；
   - 恢复 Mixin 注解中的拦截点与目标类/方法。
2. **70 组反编译缺陷修复**：
   - **Mixin 转型**：修复 Vineflower 反编译造成的 `(Object)this` 双转型丢失导致的类型校验错误；
   - **SmartBrainLib (SBL) 依赖**：使用 Yarn 原生 `1.16.10` 版本，修复 SBL 泛型推断退化；
   - **Switch 作用域与命名**：补全 case 作用域花括号，解决 Java 局部变量遮蔽错误；
   - 修复包含 `DoubleSliderEntry`、`FreecamKeyMapping` 伪 `@Override` 标记在内的多项反编译遗留问题。

---

## 💻 运行环境 (Requirements)

- **Minecraft**: `1.21.1`
- **Mod Loader**: `Fabric Loader >= 0.18.1`
- **Java**: `Java 21`
- **核心依赖**:
  - Fabric API (`0.116.15+1.21.1`)
  - GeckoLib (`4.9.2`)
  - SmartBrainLib (`1.16.10`)
  - Cloth Config (`15.0.140+fabric`)

---

> **声明**：本产物仅用于技术学习与社区版本移植验证。版权归原作者所有。
