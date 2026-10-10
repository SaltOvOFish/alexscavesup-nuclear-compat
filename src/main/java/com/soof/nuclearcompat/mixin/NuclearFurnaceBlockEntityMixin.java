package com.soof.nuclearcompat.mixin;

import com.alexscaves.server.block.blockentity.NuclearFurnaceBlockEntity;
import com.soof.nuclearcompat.NuclearCompatUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 让 Alex's Caves Up 的核能熔炉（Nuclear Furnace）支持不同燃料棒的不同烧制量：
 * - createnuclear:uranium_rod  → 25600 裂变时间（约 256 个物品）
 * - createnuclear:thorium_rod  → 6400 裂变时间（约 64 个物品）
 *
 * 原版 {@code getMaxFissionTime()} 恒为 {@code 6400 * getSpeedReduction()}（不区分燃料棒），
 * 这里在 {@code fissionTime = getMaxFissionTime()} 赋值完成后，按燃料棒类型改写裂变时间。
 */
@Mixin(NuclearFurnaceBlockEntity.class)
public abstract class NuclearFurnaceBlockEntityMixin {

    @Shadow
    private int fissionTime;

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/alexscaves/server/block/blockentity/NuclearFurnaceBlockEntity;fissionTime:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private static void acnc$adjustFissionTimeByRod(Level level, BlockPos pos, BlockState state, NuclearFurnaceBlockEntity entity, CallbackInfo ci) {
        ((NuclearFurnaceBlockEntityMixin) (Object) entity).fissionTime =
                NuclearCompatUtil.fissionTimeForRod(entity.getItem(1), NuclearFurnaceBlockEntity.getSpeedReduction());
    }
}
