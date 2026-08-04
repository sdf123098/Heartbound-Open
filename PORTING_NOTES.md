# Heartbound 0.6.9+1.21.1_WIP3 → 26.1.2 移植笔记

> 无源码 JAR 恢复 + 跨版本迁移工程。原 JAR 为 Fabric 1.21.1、intermediary 命名空间。
> 本文档是阶段一（静态分析）的完整输出，并作为后续阶段的持续记录。

## 0. 阶段状态

| 阶段 | 内容 | 状态 |
|---|---|---|
| 一 | 分析原始 JAR | ✅ 完成（本文件） |
| 二 | 恢复 1.21.1 源码工程（remap + 反编译） | ⏳ 待执行 |
| 三 | 修复恢复工程至 `./gradlew build` 通过 | ⏳ |
| 四 | 1.21.1 基线验证（runClient） | ⏳ |
| 五 | 迁移至 26.1.2 | ⏳ |

## 1. 模组元数据（fabric.mod.json）

| 字段 | 值 |
|---|---|
| id | `heartbound` |
| version | `0.6.9+1.21.1_WIP3` |
| name | 姬伴 |
| description | 我的世界珍妮老师官网lsp8.top. |
| authors | MC珍妮老师 |
| contact | https://lsp8.top |
| license（声明） | CC0-1.0（⚠️ 与 LICENSE_Heartbound 矛盾，见 §13） |
| icon | assets/heartbound/icon.png |
| environment | `*`（双端） |
| schemaVersion | 1 |

原始构建链（JAR 内 `META-INF/MANIFEST.MF`，**可据此精确复刻构建环境**）：

```
Fabric-Gradle-Version: 8.14.2
Fabric-Loom-Version: 1.13.6
Fabric-Loader-Version: 0.18.1
Fabric-Tiny-Remapper-Version: 0.12.0
Fabric-Mixin-Version: 0.16.5+mixin.0.8.7
Fabric-Mapping-Namespace: intermediary
Fabric-Jar-Type: classes
Fabric-Loom-Mixin-Remap-Type: static
```

- 字节码版本：class file major 65 = **Java 21**（与 `depends: java >=21` 一致）。
- JAR 规模：1182 个条目，310 个 class，解压后约 43.7 MB（资源占大头：435 个音效）。

## 2. Entrypoint

| 类型 | 类 |
|---|---|
| main | `com.cuddly.heartbound.Heartbound` |
| client | `com.cuddly.heartbound.HeartboundClient` |
| modmenu | `com.cuddly.heartbound.ModMenuIntegration` |
| jei_mod_plugin | `com.cuddly.heartbound.jei.HeartboundJEIPlugin` |

无 server entrypoint。

## 3. Mixin（`heartbound.mixins.json`，共 25 个类）

- `required: true`，`minVersion 0.8`，`compatibilityLevel JAVA_21`，`injectors.defaultRequire 1`
- 包：`com.cuddly.heartbound.mixins`
- **通用（4）**：
  - `bugfix.ScreenHandlerMixin`
  - `transformation.PlayerRidingMixin`
  - `transformation.PlayerTransformationMixin`
  - `transformation.ServerPlayerRespawnMixin`
- **client（21）**：
  - `freecam.*`（17 个）：BlockStateBase、BubbleColumnAmbientSoundHandler、Camera、ClientPacketListener、Entity、EntityRenderDispatcherAccessor、EntityRenderDispatcher、EntityRenderer、GameRenderer、Gui、ItemInHandRenderer、LevelRenderer、LightTexture、LivingEntity、LocalPlayer、Minecraft、MultiPlayerGameMode、Options
  - `geckolib.GeoBoneMixin`
  - `transformation.InventoryScreenMixin`
  - `transformation.PlayerEntityRendererMixin`
- ⚠️ **JAR 内没有 refmap 文件** → 反编译后 Mixin 的目标类/方法只能从反编译源码的注解中恢复（remap 会把注解里的 intermediary 名称一并转为 Yarn 名）。
- ✅ **无 access widener**（fabric.mod.json 无 accessWidener 字段，jar 内无 .accesswidener 文件）。

## 4. 依赖（fabric.mod.json depends / suggests）

| 依赖 | 要求 | 1.21.1 恢复工程选用 | 来源仓库 | 已验证 |
|---|---|---|---|---|
| fabricloader | >=0.18.1 | 0.18.6 | maven.fabricmc.net | ✅ |
| minecraft | ~1.21.1 | 1.21.1 | — | ✅ |
| java | >=21 | 21（PATH java 21.0.12） | — | ✅ |
| fabric-api | * | 0.116.15+1.21.1（1.21.1 最新线） | maven.fabricmc.net | ✅ |
| geckolib | >=4.8.3 | 4.9.2（geckolib-fabric-1.21.1 最新 4.x） | dl.cloudsmith.io geckolib3/geckolib | ✅ |
| cloth-config | >=15.0.140 | 15.0.140+fabric | api.modrinth.com/maven | ✅ |
| smartbrainlib | >=1.16.8 | 1.16.11（1.21.1 最新） | api.modrinth.com/maven | ✅ |
| patchouli | * | 1.21.1-93-FABRIC | maven.blamejared.com | ✅ |
| suggests: jei | * | 19.43.0.393（编译期 modCompileOnly） | maven.blamejared.com | ✅ |
| suggests: another-mod | * | 不存在该 mod，忽略 | — | — |
| modmenu（代码实际引用） | — | 11.0.0（编译期 modCompileOnly） | maven.terraformersmc.com | ✅ |

- **无 jar-in-jar**（META-INF/jars 为空）。
- 恢复工程实际采用的构建工具链：Gradle **9.5.1**（wrapper）+ Fabric Loom **1.17.17** + Yarn **1.21.1+build.3** + Java 21。
- 工具链选型说明：GeckoLib 4.9.2 等 2026 年版依赖是用 **Loom 1.17.13** 构建的，Loom 1.13.6 会直接拒绝（`Mod was built with a newer version of Loom`）；Loom 1.17.x 要求 Gradle 9.5+。故采用 Loom 1.17.17 + Gradle 9.5.1（本机已缓存发行版）。
- Loom 的 plugin marker 由 settings.gradle 的 `resolutionStrategy.useModule` 直接解析 `net.fabricmc:fabric-loom` 库构件（marker 不在 Fabric Maven 上）。

## 5. 命名空间确认

对全部 310 个 class 的字节扫描：

- `net/minecraft/class_*` 引用：**6180 处**
- `method_*`：**2214 处**；`field_*`：**783 处**
- Yarn/Mojmap 风格引用（`net/minecraft/world/entity`、`net/minecraft/client/render`）：**0 处**

结论：JAR 已 remap 到 **Fabric intermediary**（与 MANIFEST 一致）。模组自身类名（`com.cuddly.heartbound.*`）不受影响，全部可读。

## 6. 代码分布（310 类，按包）

| 包 | 类数 | 说明 |
|---|---|---|
| networking/C2S | 33 | 客户端→服务端包（含内部类） |
| networking/S2C | 10 | 服务端→客户端包 |
| networking、networking/codec | 3 | 通道注册与编解码 |
| entity/ai/goal | 23 | 基于 vanilla Goal 的 AI |
| entity/ai/brain、pathing | 4 | Brain/寻路 |
| entity/girls | 9 | 各“姬”实体（Aly/Bia/Coppie/Ellie/Jenny/Kobold/Slime/CustomGirl 等） |
| entity/base、tamable、wild | 7 | 实体基类（tamable 含 SBL AI） |
| mixins/freecam | 18 | freecam 客户端 Mixin |
| mixins/transformation、bugfix、geckolib | 8 | 变身/修复/GeckoLib Mixin |
| client/gui/screen(+customize,hud) | 26 | 客户端 GUI |
| client/models、rendering(+renderers,layers) | 29 | GeckoLib 模型与渲染器 |
| client | 3 | HeartboundClient 等 |
| config(+gui,keys) | 23 | 配置（Cloth Config 集成） |
| freecam(+tripod) | 7 | freecam 核心逻辑 |
| advancement/criterion | 17 | 自定义进度标准 |
| registries | 13 | 注册入口 |
| screen | 10 | 服务端 ScreenHandler |
| util(+variables,rendering,json,managers,inventory,slot) | 40 | 工具 |
| block(+blocks,entity) | 6 | 方块与方块实体 |
| item(+items) | 7 | 物品 |
| command | 2 | 命令 |
| transformation | 3 | 变身逻辑 |
| component、jei | 4 | 数据组件？/JEI 集成 |

客户端/服务端分布：无 server entrypoint，服务端逻辑（ScreenHandler、C2S 包处理、实体 AI）都在 common 类中；客户端专用代码集中在 `client.*`、`freecam.*`、`mixins.freecam/transformation` 等包。**服务端加载风险点**：需要确认 common 类中是否有对 `net.minecraft.client.*` 的直接引用（阶段三/四重点检查，技能文档已记录此类崩溃模式）。

## 7. 网络（约 46 个类）

- C2S 33 + S2C 10 + codec 1 + 注册 2 → 自定义 Payload 体系，1.21.1 上应为 Fabric Networking API v1（`CustomPayload` + `PacketCodec`）。
- 迁移到 26.1.2 时全部需要改为 **StreamCodec** 体系并重写注册表 —— 这是最大的一块机械性工作。
- 具体包名在反编译后补充到 §10 表格。

## 8. GeckoLib 使用位置（19 个类）

- 模型：`client/models/AbstractGirlModel`、`CustomGirlModel`、`TransformedPlayerModel`
- 渲染器：`client/rendering/renderers/AbstractGirlRenderer`（基类）+ Aly、Bia、Coppie、CustomGirl、Ellie、Jenny、Kobold、Slime、TransformedPlayerRenderer
- 其他：`client/rendering/TransformedPlayerAnimatable`、`client/rendering/layers/BoneOverrideRenderLayer`、`entity/base/GirlSceneEntity`（Animatable 实体）、`mixins/geckolib/GeoBoneMixin`
- 资源：19 个 `assets/heartbound/geo/*.geo.json`、9 个 `animations/*.animation.json`、6 个 `keyframe_events/*.json`（aly/bia/ellie/example/jenny，配合代码中的 keyframe 事件处理）
- 26.1.2 迁移：GeckoLib 4 → 5 全 API 变动（Animatable、Renderer、keyframe 事件、ResourceManager 加载）。

## 9. SmartBrainLib / Patchouli / Cloth Config / JEI / ModMenu 使用位置

- **SmartBrainLib（1 类）**：`entity/base/tamable/BaseGirlEntityAI` —— 唯一 SBL 入口，其余 23 个 goal 类用 vanilla Goal。SBL 迁移面小。
- **Patchouli（1 类）**：`item/items/HeartGuide`（书本物品）；资源：assets 152 个 + data 3 个 patchouli_books 文件。26.1.2 是否有可用 Patchouli 待查。
- **Cloth Config（6 类）**：`config/gui/AutoConfigExtensions`、`DoubleSliderEntry`、`ModBindingsConfigImpl` + `config/ModConfig` 三个枚举（FlightMode/InteractionMode/Perspective）—— 用到 Cloth Config 的自动配置/自定义条目 API。
- **JEI（3 类）**：`jei/FusionRecipe`、`FusionRecipeCategory`、`HeartboundJEIPlugin`（融合配方展示，注册为 jei_mod_plugin entrypoint）。
- **ModMenu（1 类）**：`ModMenuIntegration`。
- 无 Architectury。

## 10. 网络包 / 实体 / 注册清单（反编译后回填）

- [ ] C2S/S2C payload 全列表（类名 + 用途）
- [ ] 实体注册表与属性
- [ ] 方块/物品/声音/进度/世界生成注册表
- [ ] ScreenHandler 列表

## 11. 资源结构（不修改内容，原样复制）

- **assets/heartbound**：animations 9、blockstates 4、geo 19、icon.png、items 12、keyframe_events 6、lang 6（en_us/ja_jp/pt_br/vi_vn/zh_cn）、models 25、patchouli_books 152、**python 13**（`animation_fixer.py`、`convert_sounds_to_ogg.py`、`generate_sounds_json.py` 等构建辅助脚本误入资源，保留不动）、sounds 435、sounds.json、textures 43
- **data/heartbound**：advancement 20、loot_table 4、patchouli_books 3、recipe 15、structure 6、tags 9、worldgen 24
- 语言文件 5 种（en_us、ja_jp、pt_br、vi_vn、zh_cn）。

## 12. 预计需要重写的功能

1. **网络层**（46 类）：PacketCodec → StreamCodec，Fabric Networking v1 注册方式变化。
2. **GeckoLib 4 → 5**：19 个类 + 资源加载 + keyframe 事件系统。
3. **渲染管线**（26.x RenderState 架构）：renderers、layers、模型、GUI。
4. **客户端 Mixin 群**（21 个）：freecam 涉及 Camera/GameRenderer/LevelRenderer/Minecraft/ClientPacketListener 等 26.x 大变动的类；transformation 涉及 PlayerEntityRenderer/InventoryScreen。
5. **SmartBrainLib 版本**（1.16.x → 26.x 对应版本，需确认存在）。
6. **世界生成**（24 个 data 文件）：1.21.1 → 26.x 的 worldgen 格式与注册 API。
7. **可选集成**：Patchouli/JEI/ModMenu 的 26.x 可用性。
8. 服务端安全审计：common 类中的 client 引用（见 §6 注）。

## 13. 许可证问题（⚠️ 重要）

- `fabric.mod.json` 声明 `license: CC0-1.0`，但 JAR 根目录的 **`LICENSE_Heartbound`** 文件内容为：
  > Copyright (c) 2025 Chronos. All rights reserved. … 未经书面许可，禁止使用、复制、修改、**反编译、逆向工程**、再分发。
- 两份声明互相矛盾；`LICENSE_Heartbound` 更具体且是随包分发的正式文件，应视为有效许可。
- **结论**：本工程仅限本地恢复与私人使用。恢复出的源码与移植版 JAR **不得公开分发**（GitHub/Modrinth/CurseForge 等），否则有侵权风险。若需发布，必须先联系作者（lsp8.top）取得授权。
- 已按用户要求继续技术工作，风险记录在案。

## 14. Remap + 反编译方案（阶段二执行）

工具（全部已验证可下载）：

| 工具 | 版本 | 下载 |
|---|---|---|
| tiny-remapper（fat） | 0.14.0 | https://maven.fabricmc.net/net/fabricmc/tiny-remapper/0.14.0/tiny-remapper-0.14.0-fat.jar |
| Yarn 映射（mergedv2） | 1.21.1+build.3 | https://maven.fabricmc.net/net/fabricmc/yarn/1.21.1+build.3/yarn-1.21.1+build.3-mergedv2.jar |
| Vineflower | 1.10.1 | https://repo1.maven.org/maven2/org/vineflower/vineflower/1.10.1/vineflower-1.10.1.jar |

流程：

```bash
# 1) 下载三个工具到 analysis/tools/
# 2) intermediary -> named（Yarn）remap
java -jar analysis/tools/tiny-remapper-0.14.0-fat.jar \
    original/Heartbound-0.6.9+1.21.1_WIP3.jar \
    analysis/heartbound-named.jar \
    analysis/tools/yarn-1.21.1+build.3-mergedv2.jar \
    intermediary named \
    -l <geckolib.jar> -l <sbl.jar> -l <cloth-config.jar> -l <patchouli.jar>   # 依赖 jar 作 library classpath
# 3) 反编译到 restore-1.21.1/src/main/java
java -jar analysis/tools/vineflower-1.10.1.jar \
    -e=<依赖 jar 列表> \
    analysis/heartbound-named.jar \
    restore-1.21.1/src/main/java
# 4) 资源原样复制：assets/ data/ fabric.mod.json heartbound.mixins.json LICENSE_Heartbound META-INF/ icon
```

- 执行时先用 `--help` 核对 tiny-remapper / vineflower 的具体参数（版本较新，flag 可能有变化）。
- 依赖 jar 从 `~/.gradle/caches/modules-2/files-2.1/`（首次 `./gradlew` 解析后）或直接按 §4 的 URL 下载。
- 反编译产物将包含泛型丢失、`switch` 映射错乱、lambda 破损等典型问题 → 按阶段三的 13 组错误类别逐组修复。
- 备用反编译器：CFR 0.152（https://repo1.maven.org/maven2/org/benf/cfr/0.152/cfr-0.152.jar）。

## 15. 1.21.1 恢复工程（restore-1.21.1）推荐版本

| 组件 | 版本 | 理由 |
|---|---|---|
| Gradle（wrapper） | 9.5.1 | Loom 1.17 要求 Gradle 9.5+；腾讯镜像下载 |
| Fabric Loom | 1.17.17 | 依赖（GeckoLib 4.9.2）用 Loom 1.17.13 构建，旧 Loom 拒绝加载；useModule 解析 |
| Fabric Loader | 0.18.6 | 满足 >=0.18.1，0.18.x 最新 |
| Yarn | 1.21.1+build.3 | 1.21.1 最终 build |
| Fabric API | 0.116.15+1.21.1 | 1.21.1 最新线 |
| Java | 21 | 本机 PATH java 21.0.12；**运行 Gradle 前必须 `unset JAVA_HOME`**（JAVA_HOME 指向 JDK 25，Gradle 8.14.2 会拒绝） |

## 16. 阶段三错误修复清单（按组）

1. 反编译语法错误 → 2. 泛型 → 3. lambda/匿名类 → 4. enum switch 映射 → 5. 静态初始化 → 6. 缺失 Yarn 名称 → 7. 缺失依赖 → 8. GeckoLib 4 API → 9. SmartBrainLib API → 10. Fabric 网络 API → 11. Mixin target/@Shadow/注入点 → 12. 客户端渲染 → 13. 资源加载注册

禁止：删类、整块注释、空实现、统一返回固定值、删 Mixin/实体/网络包/渲染器。无法恢复的代码保留结构 + `TODO` 注释并在本文档记录影响。

## 17. 阶段五（26.1.2）特别检查项

- Java 25 要求、Fabric Loom 新版配置、Mojang 官方命名（26.x 不再用 Yarn？）
- StreamCodec / 新 Payload 注册
- RenderState 渲染架构
- GeckoLib 5 API
- Mixin 目标方法描述符全部重对
- Patchouli / JEI / ModMenu 的 26.1.2 版本可用性；依赖不支持时按模块隔离或临时替代，并记录功能差异（不切换到 NeoForge）

## 18. 本机环境要点（执行用）

- 工作目录：`D:\Project\Heart\Heartbound-Port`
- 运行 Gradle：`unset JAVA_HOME`（JAVA_HOME=JDK 25 会被 Gradle 8.14.2 拒绝）；PATH java = JDK 21.0.12
- 备选 JDK 21：`C:\Program Files\Zulu\zulu-21`（LuminaBox 工程在用）
- git-bash 环境；`./gradlew` 脚本可直接运行（无需 gradlew.bat）
- 无全局 Gradle。⚠️ LuminaBox 系列工程的 gradlew / gradlew.bat / gradle-wrapper.jar 均已损坏（脚本含 LLM 垃圾文本且双重 `set --`；jar 缺 `Main-Class`）——**不可复用**，wrapper 一律用发行版 `gradle wrapper` 重新生成
- **网络**：`services.gradle.org` 直连可用（307 → github.com），但 Java 客户端连 github 发行资产被重置 → wrapper 用腾讯镜像 `https://mirrors.cloud.tencent.com/gradle/gradle-9.5.1-bin.zip`（已验证 200）。生成 wrapper 时加 `--no-validate-url` 跳过联网校验。本机另有代理 127.0.0.1:7897（存活，备用）。

## 19. Git 提交计划

- [x] `chore: initialize 1.21.1 restoration workspace`
- [x] `docs: document original jar structure`
- [x] `build: configure fabric 1.21.1 environment`
- [x] `fix: regenerate canonical wrapper, upgrade to gradle 9.5.1 + loom 1.17.17`
- [ ] `refactor: restore decompiled source tree`（阶段二）
- [ ] `fix: ...`（阶段三，按组提交）
- [ ] `build: establish working 1.21.1 baseline`（阶段四）
- [ ] `chore: initialize 26.1.2 port`（阶段五）
