package com.soof.nuclearcompat.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.nuclearteam.createnuclear.content.multiblock.controller.service.ReactorMeltdownExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

/**
 * 原子核动反应堆熔毁爆炸时，像 Alex's Caves 核爆一样孵化附近的撼地斯拉蛋。
 *
 * <p>通过注册名 + 反射调用 {@code spawnDinosaurs}，避免对 AC 具体类的编译期依赖，
 * 从而同时兼容 Alex's Caves Up 与 Alex's Caves Neo。</p>
 */
@Mixin(ReactorMeltdownExecutor.class)
public abstract class ReactorMeltdownExecutorMixin {

    private static final ResourceLocation TREMORZILLA_EGG_ID =
            ResourceLocation.fromNamespaceAndPath("alexscaves", "tremorzilla_egg");

    @Inject(method = "triggerExplosion", at = @At("HEAD"))
    private void acnc$hatchNearbyTremorzillaEggs(
            ServerLevel level, BlockPos pos, BoundingBox box,
            int sizeA, int sizeB, int sizeC, boolean flag, CallbackInfo ci) {
        hatchNearby(level, pos);
    }

    private static void hatchNearby(Level level, BlockPos center) {
        Block egg = BuiltInRegistries.BLOCK.get(TREMORZILLA_EGG_ID);
        if (egg == null) {
            return;
        }
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int x = -36; x <= 36; x++) {
            for (int y = -36; y <= 36; y++) {
                for (int z = -36; z <= 36; z++) {
                    mpos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (mpos.distSqr(center) > 1296.0d) {
                        continue;
                    }
                    BlockState state = level.getBlockState(mpos);
                    if (!state.is(egg)) {
                        continue;
                    }
                    hatchEgg(level, mpos.immutable(), state);
                }
            }
        }
    }

    private static void hatchEgg(Level level, BlockPos pos, BlockState state) {
        try {
            Method method = state.getBlock().getClass()
                    .getMethod("spawnDinosaurs", Level.class, BlockPos.class, BlockState.class);
            method.invoke(state.getBlock(), level, pos, state);
        } catch (Exception ignored) {
            // 反射失败时静默忽略。
        }
    }
}
