package com.soof.nuclearcompat;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 让机械动力（Create）的机械臂把渊海祭坛识别为可交互目标（类似置物台）。
 *
 * 机械臂通过「交互点类型」注册表识别可交互方块；渊海祭坛原本不在其中。
 * 注册此类型后，机械臂即可从上方像操作置物台一样拿取/放置渊海祭坛里的物品。
 */
public final class AbyssalAltarInteractionPointType extends ArmInteractionPointType {

    private static final Block ABYSSAL_ALTAR = BuiltInRegistries.BLOCK.get(
            ResourceLocation.fromNamespaceAndPath("alexscaves", "abyssal_altar"));

    @Override
    public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
        return state.getBlock() == ABYSSAL_ALTAR;
    }

    @Override
    public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
        return new ArmInteractionPoint(this, level, pos, state);
    }
}
