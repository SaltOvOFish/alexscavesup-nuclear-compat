package com.soof.nuclearcompat.mixin;

import com.github.alexmodguy.alexscaves.server.block.blockentity.NuclearFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Alex's Caves Neo（包名 com.github.alexmodguy.alexscaves）专用版。
 * 逻辑与 {@code NuclearFurnaceBlockEntityMixin}（Alex's Caves Up）完全一致。
 */
@Mixin(NuclearFurnaceBlockEntity.class)
public abstract class NuclearFurnaceBlockEntityNeoMixin {

    @Shadow
    private int fissionTime;

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/github/alexmodguy/alexscaves/server/block/blockentity/NuclearFurnaceBlockEntity;fissionTime:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private static void acup$adjustFissionTimeByRod(Level level, BlockPos pos, BlockState state, NuclearFurnaceBlockEntity entity, CallbackInfo ci) {
        ItemStack rod = entity.getItem(1);
        Item thoriumRod = BuiltInRegistries.ITEM
                .get(ResourceLocation.fromNamespaceAndPath("createnuclear", "thorium_rod"));

        if (thoriumRod != null && rod.is(thoriumRod)) {
            ((NuclearFurnaceBlockEntityNeoMixin) (Object) entity).fissionTime =
                    (int) Math.ceil(6400.0f * NuclearFurnaceBlockEntity.getSpeedReduction());
        } else {
            ((NuclearFurnaceBlockEntityNeoMixin) (Object) entity).fissionTime =
                    (int) Math.ceil(25600.0f * NuclearFurnaceBlockEntity.getSpeedReduction());
        }
    }
}
