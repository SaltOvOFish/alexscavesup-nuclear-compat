package com.soof.nuclearcompat.mixin;

import com.github.alexmodguy.alexscaves.server.item.HazmatArmorItem;
import com.github.alexmodguy.alexscaves.server.potion.IrradiatedEffect;
import com.soof.nuclearcompat.ArmorCompat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Alex's Caves Neo（包名 com.github.alexmodguy.alexscaves）专用版。
 * 逻辑与 {@code IrradiatedEffectMixin}（Alex's Caves Up）完全一致。
 */
@Mixin(IrradiatedEffect.class)
public abstract class IrradiatedEffectNeoMixin {

    private static final ResourceLocation AC_IRRADIATED = ResourceLocation.fromNamespaceAndPath("alexscaves", "irradiated");

    @Inject(
            method = "applyEffectTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void acup$immuneWhenFullArmor(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        if (ArmorCompat.hasFullAntiRadiationArmor(entity) || ArmorCompat.hasFullHazmatArmor(entity)) {
            BuiltInRegistries.MOB_EFFECT.getHolder(AC_IRRADIATED).ifPresent(entity::removeEffect);
            cir.setReturnValue(false);
        }
    }

    @Redirect(
            method = "applyEffectTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexmodguy/alexscaves/server/item/HazmatArmorItem;getWornAmount(Lnet/minecraft/world/entity/LivingEntity;)I"
            )
    )
    private static int acup$countAntiRadiationAsHazmat(LivingEntity entity) {
        return Math.min(HazmatArmorItem.getWornAmount(entity) + ArmorCompat.countAntiRadiationArmor(entity), 4);
    }
}
