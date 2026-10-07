package com.soof.nuclearcompat;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

/**
 * Alex's Caves Up: Nuclear Compat
 *
 * 一个轻量级桥接模组：把「原子核动（Create Nuclear）」的辐射状态效果
 * （createnuclear:radiation）替换为「Alex's Caves Up」的辐照效果
 * （alexscaves:irradiated），让两个模组共用同一套辐射机制。
 *
 * 实现方式：运行时通过注册表动态解析两个效果（效果缺失时对应桥接自动停用），
 * 在 NeoForge 的 {@code MobEffectEvent.Applicable} 事件里拦截
 * {@code createnuclear:radiation} 的施加，改为施加 {@code alexscaves:irradiated}。
 */
@Mod(NuclearCompat.MODID)
public class NuclearCompat {

    /** 本模组的 Mod ID，必须与 META-INF/neoforge.mods.toml 及 gradle.properties 一致。 */
    public static final String MODID = "alexscavesup_nuclear_compat";

    public static final Logger LOGGER = LogUtils.getLogger();

    /** 原子核动（Create Nuclear）的辐射效果注册名。 */
    public static final ResourceLocation CN_RADIATION_ID = ResourceLocation.fromNamespaceAndPath("createnuclear", "radiation");

    /** Alex's Caves Up 的辐照效果注册名。 */
    public static final ResourceLocation AC_IRRADIATED_ID = ResourceLocation.fromNamespaceAndPath("alexscaves", "irradiated");

    /** 在 commonSetup 阶段解析得到的两个效果 Holder（未找到时为 null，桥接自动停用）。 */
    public static Holder<MobEffect> CN_RADIATION = null;
    public static Holder<MobEffect> AC_IRRADIATED = null;

    public NuclearCompat(IEventBus modEventBus) {
        // 注册本模组新增的方块（四种辐射岩矿石）及对应的方块物品。
        ModBlocks.register(modEventBus);

        // FMLCommonSetupEvent 在所有模组的 RegisterEvent 之后触发，
        // 此时两个目标模组的效果都已注册完毕，可以安全查询。
        modEventBus.addListener(this::commonSetup);
        // RegisterEvent 在注册表冻结前触发，用于注册 Create 的机械臂交互点类型。
        modEventBus.addListener(this::registerArmInteractionPoint);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        CN_RADIATION = BuiltInRegistries.MOB_EFFECT.getHolder(CN_RADIATION_ID).orElse(null);
        AC_IRRADIATED = BuiltInRegistries.MOB_EFFECT.getHolder(AC_IRRADIATED_ID).orElse(null);

        if (CN_RADIATION == null) {
            LOGGER.warn("[ACUp Nuclear Compat] Effect '{}' not found. Create Nuclear missing? Radiation replacement disabled.", CN_RADIATION_ID);
        }
        if (AC_IRRADIATED == null) {
            LOGGER.warn("[ACUp Nuclear Compat] Effect '{}' not found. Alex's Caves Up missing? Radiation replacement disabled.", AC_IRRADIATED_ID);
        }
        if (CN_RADIATION != null && AC_IRRADIATED != null) {
            LOGGER.info("[ACUp Nuclear Compat] Radiation unified: {} -> {}", CN_RADIATION_ID, AC_IRRADIATED_ID);
        }
    }

    /** 注册渊海祭坛为机械臂交互点，让机械臂能像置物台一样拿取/放置其中的物品。 */
    private void registerArmInteractionPoint(RegisterEvent event) {
        if (event.getRegistry() != CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE) {
            return;
        }
        Registry.register(
                CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE,
                ResourceLocation.fromNamespaceAndPath(MODID, "abyssal_altar"),
                new AbyssalAltarInteractionPointType()
        );
        ArmInteractionPointType.init();
        LOGGER.info("[ACUp Nuclear Compat] Registered abyssal altar as a mechanical arm interaction point.");
    }
}
