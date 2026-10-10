package com.soof.nuclearcompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;

/**
 * 跨 mixin 共享的核能相关工具方法。
 *
 * <p>Alex's Caves 的 Up / Neo 两个移植版包名不同，对应的 mixin 必须各写一份，
 * 但其中的核心判断逻辑完全相同。这里把不依赖 Alex's Caves 具体类的纯逻辑集中
 * 到一处，避免双份维护、改一处漏一处。</p>
 */
public final class NuclearCompatUtil {

    private static final ResourceLocation REACTOR_CONTROLLER_ID =
            ResourceLocation.fromNamespaceAndPath("createnuclear", "reactor_controller");
    private static final ResourceLocation THORIUM_ROD_ID =
            ResourceLocation.fromNamespaceAndPath("createnuclear", "thorium_rod");

    /** 铀棒（默认）裂变时间系数，约 256 个物品。 */
    private static final float URANIUM_FISSION_FACTOR = 25600.0f;
    /** 钍棒裂变时间系数，约 64 个物品。 */
    private static final float THORIUM_FISSION_FACTOR = 6400.0f;

    private NuclearCompatUtil() {
    }

    /**
     * 在中心点附近（默认 32 格）查找热量达到 DANGER、即已进入失控倒计时的
     * 原子核动反应堆控制器，返回其坐标；未找到返回 null。
     *
     * <p>与 CN 自身的 {@code ReactorAlarmCoordinator#computeDanger} 判定完全等价：
     * {@code IHeat.HeatLevel.of(heat, size) == DANGER}。通过反射读取
     * {@code getConfiguredPatternHeat()} / {@code getMultiblockSize()}，
     * 避免对 CN 反应堆控制器具体类的编译期依赖。</p>
     */
    public static BlockPos findMeltdownReactor(Level level, BlockPos center) {
        return findMeltdownReactor(level, center, 32);
    }

    public static BlockPos findMeltdownReactor(Level level, BlockPos center, int radius) {
        Block controller = BuiltInRegistries.BLOCK.get(REACTOR_CONTROLLER_ID);
        if (controller == null) {
            return null;
        }
        for (BlockPos p : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (!level.getBlockState(p).is(controller)) {
                continue;
            }
            BlockEntity be = level.getBlockEntity(p);
            if (be == null) {
                continue;
            }
            try {
                int heat = (Integer) be.getClass().getMethod("getConfiguredPatternHeat").invoke(be);
                int size = (Integer) be.getClass().getMethod("getMultiblockSize").invoke(be);
                if (!IHeat.HeatLevel.isNotDanger(heat, size)) {
                    return p.immutable();
                }
            } catch (Exception ignored) {
                // 反射失败时忽略（类结构差异或加载时序）。
            }
        }
        return null;
    }

    /**
     * 根据燃料棒类型返回裂变时间：钍棒 6400、其余（铀棒等）25600，再乘以核能熔炉的
     * 速度系数（{@code getSpeedReduction()}）。
     */
    public static int fissionTimeForRod(ItemStack rod, float speedReduction) {
        Item thoriumRod = BuiltInRegistries.ITEM.get(THORIUM_ROD_ID);
        float factor = (thoriumRod != null && rod.is(thoriumRod))
                ? THORIUM_FISSION_FACTOR
                : URANIUM_FISSION_FACTOR;
        return (int) Math.ceil(factor * speedReduction);
    }
}
