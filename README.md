# Alex's Caves: Nuclear Convergence

一个 NeoForge 1.21.1 桥接模组，把「原子核动（Create Nuclear）」与「Alex's Caves Up」的铀/辐射体系打通，
统一为一套以 Create Nuclear 为中心的机制。

## 功能

### 1. 辐射效果统一
- 拦截 `createnuclear:radiation`（Create Nuclear 的辐射效果），改为施加 `alexscaves:irradiated`
  （Alex's Caves Up 的辐照效果），等级 / 时长 / 氛围 / 可见性保持一致；
- 当 Create Nuclear 移除辐射效果时，同步移除辐照效果。

### 2. 铀物品映射（配方 + 掉落）
| Alex's Caves Up | Create Nuclear |
|---|---|
| `alexscaves:uranium` | `createnuclear:raw_uranium` |
| `alexscaves:uranium_shard` | `createnuclear:uranium_powder` |
| `alexscaves:uranium_rod` | `createnuclear:uranium_rod` |
| `alexscaves:block_of_uranium` | `createnuclear:raw_uranium_block` |

- `radrock_uranium_ore` 掉落改为 **基础 2 个 `raw_uranium`**（受时运、爆炸衰减影响）；
- 生物掉落（tremorzilla）中的 uranium / shard 替换为 raw_uranium / uranium_powder；
- 禁用 Alex's Caves Up 产出 `uranium` / `uranium_shard` / `block_of_uranium` 的全部配方；
- `nuclear_siren`、`nuclear_furnace_component` 配方中的铀材料改为 `raw_uranium`；
- `raygun`、`nuclear_bomb` 配方中的 `uranium_rod` 改为 `createnuclear:uranium_rod`（核弹中心格改为 `raw_uranium_block`）；
- `unrefined_waste` 熔炉/高炉烧炼改为产出 1 个 `createnuclear:uranium_powder`。

### 3. 铀烛配方
- 新增 `alexscaves:uranium_rod` 合成配方（工作台竖列：铁锭 + `createnuclear:uranium_rod` + 铁锭 → 1 个）；
- 语言文件将其显示名重命名为「铀烛」。

### 4. 核能熔炉燃料
核能熔炉（Nuclear Furnace）燃料棒改为：
- `createnuclear:uranium_rod` → 25600 裂变时间（约 256 个物品）；
- `createnuclear:thorium_rod` → 6400 裂变时间（约 64 个物品）。

### 5. 核弹群系转化
`alexscaves:nuclear_bomb` 爆炸时，把爆炸区域转化为 Create Nuclear 的辐照之地群系
（`createnuclear:irradiated_land`），复刻反应堆失控爆炸的群系转化效果，且可被
`createnuclear:biome_irradiation_extractor` 正常消除。

### 6. 护甲兼容
防辐射套装（`createnuclear:default_anti_radiation_*`）与防化套装（`alexscaves:hazmat_*`）能力互通：

| 能力 | 防辐射套装 | 防化套装 |
|---|---|---|
| 辐照伤害减免（每件 -25%） | ✅ | ✅ |
| 穿齐 4 件免疫辐照（清除效果） | ✅ | ✅ |
| 酸液伤害减免（每件 -25%） | ✅ | ✅ |
| 穿齐 4 件免疫酸液 | ✅ | ✅ |

### 7. 剧毒遗迹箱子战利品
`toxic_ruins` 箱子中：
- `alexscaves:uranium_rod` 改为 `createnuclear:uranium_rod`；
- 新增 `createnuclear:reactor_blueprint_item`（数量 1，权重 2）。

### 8. 废弃反应堆结构（自然生成）
在毒化洞穴新增「废弃反应堆」结构（4 种变体，`abandoned_reactor_0~3`），复用 Alex's Caves Up 的
`toxic_ruins` 生成逻辑，以**锈蚀金属桶**作为战利品箱（通过 `loot_chest` data 标记定位）。

战利品表（`alexscaves_nuclear_convergence:chests/abandoned_reactor`）包含：
- 原子核动：铀棒、液氮桶、石墨棒、钢锭、反应堆蓝图、铅锭；
- Alex's Caves Up：洞穴石板、氡瓶、铀烛、绿色豆粮、废料桶、防化套装、唱片 11。

### 9. 辐射岩矿石
在毒化洞穴的辐射岩（radrock）中生成 4 种矿石：

| 矿石 | 掉落 |
|---|---|
| `radrock_iron_ore` 辐射铁矿石 | `minecraft:raw_iron` |
| `radrock_coal_ore` 辐射煤矿石 | `minecraft:coal` |
| `radrock_lead_ore` 辐射铅矿石 | `createnuclear:raw_lead` |
| `radrock_nitrate_ore` 辐射硝酸盐矿石 | `createnuclear:nitrate` |

### 10. 机械动力自动化兼容
- 为 Alex's Caves Up 的核能熔炉（含 4×4 组件方块）、金属桶/锈蚀金属桶、渊海祭坛补充
  NeoForge 的 `ItemHandler` capability，使 Create 的漏斗、溜槽、动力机械臂等能正常输入输出；
- 渊海祭坛注册为机械臂交互点类型（类似置物台），机械臂可从中拿取/放置物品；
- 手持 Create 扳手或机械臂物品右键渊海祭坛时放行，避免误把工具放进祭坛。

### 11. 反应堆熔毁与警报器 / 撼地斯拉孵化联动
- 当原子核动的反应堆热量达到 DANGER（进入失控爆炸倒计时）时，附近的 Alex's Caves 核能警报器提前响起；
- 反应堆失控爆炸时，会孵化爆炸范围内（36 格）的 `alexscaves:tremorzilla_egg`（撼地斯拉蛋），
  且孵出的撼地斯拉对本次核爆免疫（不会被秒杀或击飞），复刻 Alex's Caves 核弹孵化 Boss 的行为。

### 12. 辐射生物转化
- 原版的猫（Cat）与豹猫（Ocelot）在持续受到辐照 III 及以上效果 45 秒后，转化为
  Alex's Caves 的辐射猫（`raycat`）。

## 前置

- Minecraft 1.21.1 + NeoForge 21.1.x
- Create Nuclear（`createnuclear`）—— **硬依赖**（直接调用其 `BiomeIrradiationService`）
- Alex's Caves（`alexscaves`）—— **可选依赖**（缺失时 mixin 自动停用）。兼容两个 1.21.1 移植版：
  - Alex's Caves Up（PixellCubed）
  - Alex's Caves Neo（TysonTheEmber）

### 依赖下载（构建前自备，不随仓库提交）

本项目作为这两个模组的桥接，遵循相同的 **GPL-3.0** 许可证。构建前请自行下载两个前置 jar 放入 `libs/` 目录：

| 模组 | 放入 `libs/` 的文件名 | 出处 |
|---|---|---|
| Create Nuclear（NeoForge） | `createnuclear-2.0.0-neoforge.jar` | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/createnuclear) · [Modrinth](https://modrinth.com/project/z611fdf7) · [GitHub 源码](https://github.com/Create-Nuclear-Team/CreateNuclearNeoForge) |
| Alex's Caves Up | `alexscaves-up-0.1.4.jar` | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/alexs-caves-up) |
| Alex's Caves Neo | `alexscaves-neo-2.0.3.jar` | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/alexs-caves-neo) · [Modrinth](https://modrinth.com/mod/alexscavesneo) |

> 两个前置模组均为 GPL-3.0。本项目在编译期直接引用它们的类（`BiomeIrradiationService`、
> `NuclearFurnaceBlockEntity` 等），依据 GPL 传染性条款，本项目同样以 GPL-3.0 分发。

## 实现要点

- 辐射效果替换在 `MobEffectEvent.Applicable` 中拦截（可取消、且在效果写入实体之前）；
- 核弹群系转化监听 `EntityJoinLevelEvent`（`alexscaves:nuclear_explosion`），延迟一 tick 后
  直接调用 Create Nuclear 的 `BiomeIrradiationService.circularArea`（同步登记 `PersistentIrradiatedZones`）；
- 护甲能力互通通过 mixin 修改 `IrradiatedEffect`（辐照）与 `AcidBlock`（酸液）实现；
- 核能熔炉燃料数量差异通过 mixin 修改 `NuclearFurnaceBlockEntity` 的裂变时间实现；
- 反应堆熔毁联动通过 mixin 修改 `NuclearSirenBlockEntity`（警报器提前响起）、
  `ReactorMeltdownExecutor`（爆炸孵化蛋）与 `NuclearExplosionEntity`（撼地斯拉免疫）实现；
- 猫/豹猫变 raycat 通过 `EntityTickEvent` + NeoForge data attachment 累计辐照时间实现；
- 上述 mixin 各提供两套（Alex's Caves Up 的 `com.alexscaves` 包名 + Alex's Caves Neo 的
  `com.github.alexmodguy.alexscaves` 包名），`required: false`，运行时按实际加载的版本自动生效；
  两套 mixin 中不依赖 Alex's Caves 具体类的共享逻辑集中在 `NuclearCompatUtil` / `ArmorCompat`；
- 配方 / 掉落修改通过内置数据包覆盖 `data/alexscaves/...` 实现（声明 AFTER alexscaves 保证覆盖优先级）。

## 构建

1. 按上文「依赖下载」把两个前置 jar 放入 `libs/`（该目录已被 `.gitignore` 忽略，不提交）；
2. 运行构建：

```bash
./gradlew build
```

产物位于 `build/libs/`。`libs/` 内的两个前置 jar 仅用于编译期（compileOnly），不会打进产物。

## 可调项

| 常量 | 位置 | 说明 |
|---|---|---|
| `CN_RADIATION_ID` / `AC_IRRADIATED_ID` | `NuclearCompat.java` | 两个辐射效果的注册名 |
| `IRRADIATION_RADIUS` | `NuclearBombBiomeHandler.java` | 核弹群系转化半径（默认 90，对应核弹默认规模 3.0 × 30） |
| 燃料烧制量 | `NuclearFurnaceBlockEntityMixin.java` | 铀棒 25600 / 钍棒 6400 裂变时间系数 |

## 许可证

本项目以 [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html)（GPL-3.0）分发，详见 `LICENSE`。
