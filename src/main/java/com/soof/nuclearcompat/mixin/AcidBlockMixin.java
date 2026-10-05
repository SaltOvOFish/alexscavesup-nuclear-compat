package com.soof.nuclearcompat.mixin;

import com.alexscaves.server.block.AcidBlock;
import com.alexscaves.server.item.HazmatArmorItem;
import com.soof.nuclearcompat.ArmorCompat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 让 Create Nuclear 防辐射套装在毒化洞穴的酸液中也能获得与防化套装相同的减伤：
 * 在 {@link AcidBlock#entityInside} 计算「防化服数量」时，把防辐射护甲也计入，
 * 从而同样享受每件 -25% 酸液伤害、穿齐 4 件免疫酸液。
 */
@Mixin(AcidBlock.class)
public abstract class AcidBlockMixin {

    @Redirect(
            method = "entityInside",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/alexscaves/server/item/HazmatArmorItem;getWornAmount(Lnet/minecraft/world/entity/LivingEntity;)I"
            )
    )
    private static int acup$countAntiRadiationAsHazmat(LivingEntity entity) {
        return Math.min(HazmatArmorItem.getWornAmount(entity) + ArmorCompat.countAntiRadiationArmor(entity), 4);
    }

    @Redirect(
            method = "entityInside",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"
            )
    )
    private void acup$skipAntiRadiationDurability(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot) {
        // 防辐射套装在酸液中不掉耐久（与防化服一致）；其余护甲照常损耗。
        if (!ArmorCompat.isAntiRadiationArmor(stack)) {
            stack.hurtAndBreak(amount, entity, slot);
        }
    }
}
