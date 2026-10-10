package com.soof.nuclearcompat.mixin;

import com.github.alexmodguy.alexscaves.server.block.AcidBlock;
import com.github.alexmodguy.alexscaves.server.item.HazmatArmorItem;
import com.soof.nuclearcompat.ArmorCompat;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Alex's Caves Neo（包名 com.github.alexmodguy.alexscaves）专用版。
 * 逻辑与 {@code AcidBlockMixin}（Alex's Caves Up）完全一致。
 */
@Mixin(AcidBlock.class)
public abstract class AcidBlockNeoMixin {

    @Redirect(
            method = "entityInside",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexmodguy/alexscaves/server/item/HazmatArmorItem;getWornAmount(Lnet/minecraft/world/entity/LivingEntity;)I"
            )
    )
    private static int acnc$countAntiRadiationAsHazmat(LivingEntity entity) {
        return ArmorCompat.countHazmatLikeArmor(HazmatArmorItem.getWornAmount(entity), entity);
    }

    @Redirect(
            method = "entityInside",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"
            )
    )
    private void acnc$skipAntiRadiationDurability(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot) {
        if (!ArmorCompat.isAntiRadiationArmor(stack)) {
            stack.hurtAndBreak(amount, entity, slot);
        }
    }
}
