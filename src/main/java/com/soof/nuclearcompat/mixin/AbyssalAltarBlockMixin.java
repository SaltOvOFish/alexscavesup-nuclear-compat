package com.soof.nuclearcompat.mixin;

import com.alexscaves.server.block.AbyssalAltarBlock;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让渊海祭坛更像置物台（depot）：
 * 手持 Create 扳手或机械臂物品右键时放行，避免误把工具/机械臂放进祭坛。
 */
@Mixin(AbyssalAltarBlock.class)
public abstract class AbyssalAltarBlockMixin extends BaseEntityBlock {

    protected AbyssalAltarBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void acnc$allowWrenchAndArmOnAltar(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit,
                                               CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (stack.getItem() instanceof WrenchItem || stack.getItem() instanceof ArmItem) {
            cir.setReturnValue(super.useItemOn(stack, state, level, pos, player, hand, hit));
        }
    }
}
