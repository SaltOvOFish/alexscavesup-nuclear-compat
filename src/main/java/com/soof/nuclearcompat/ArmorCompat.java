package com.soof.nuclearcompat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 两套护甲的公共判断工具：
 * - Create Nuclear 防辐射套装（default_anti_radiation_*）
 * - Alex's Caves Up 防化套装（hazmat_*）
 */
public final class ArmorCompat {

    private static final ResourceLocation CN_HELMET = ResourceLocation.fromNamespaceAndPath("createnuclear", "default_anti_radiation_helmet");
    private static final ResourceLocation CN_CHESTPLATE = ResourceLocation.fromNamespaceAndPath("createnuclear", "default_anti_radiation_chestplate");
    private static final ResourceLocation CN_LEGGINGS = ResourceLocation.fromNamespaceAndPath("createnuclear", "default_anti_radiation_leggings");
    private static final ResourceLocation CN_BOOTS = ResourceLocation.fromNamespaceAndPath("createnuclear", "default_anti_radiation_boots");

    private static final ResourceLocation AC_MASK = ResourceLocation.fromNamespaceAndPath("alexscaves", "hazmat_mask");
    private static final ResourceLocation AC_CHESTPLATE = ResourceLocation.fromNamespaceAndPath("alexscaves", "hazmat_chestplate");
    private static final ResourceLocation AC_LEGGINGS = ResourceLocation.fromNamespaceAndPath("alexscaves", "hazmat_leggings");
    private static final ResourceLocation AC_BOOTS = ResourceLocation.fromNamespaceAndPath("alexscaves", "hazmat_boots");

    private ArmorCompat() {
    }

    private static boolean is(ItemStack stack, ResourceLocation id) {
        Item item = BuiltInRegistries.ITEM.get(id);
        return item != null && stack.is(item);
    }

    /** 该物品是否为原子核动的防辐射护甲（任一部件）。 */
    public static boolean isAntiRadiationArmor(ItemStack stack) {
        return is(stack, CN_HELMET) || is(stack, CN_CHESTPLATE) || is(stack, CN_LEGGINGS) || is(stack, CN_BOOTS);
    }

    /** 穿戴防辐射套装的数量（0~4） */
    public static int countAntiRadiationArmor(LivingEntity entity) {
        int n = 0;
        if (is(entity.getItemBySlot(EquipmentSlot.HEAD), CN_HELMET)) n++;
        if (is(entity.getItemBySlot(EquipmentSlot.CHEST), CN_CHESTPLATE)) n++;
        if (is(entity.getItemBySlot(EquipmentSlot.LEGS), CN_LEGGINGS)) n++;
        if (is(entity.getItemBySlot(EquipmentSlot.FEET), CN_BOOTS)) n++;
        return n;
    }

    /** 是否穿戴整套防辐射套装（4 件） */
    public static boolean hasFullAntiRadiationArmor(LivingEntity entity) {
        return countAntiRadiationArmor(entity) == 4;
    }

    /** 是否穿戴整套防化套装（4 件） */
    public static boolean hasFullHazmatArmor(LivingEntity entity) {
        return is(entity.getItemBySlot(EquipmentSlot.HEAD), AC_MASK)
                && is(entity.getItemBySlot(EquipmentSlot.CHEST), AC_CHESTPLATE)
                && is(entity.getItemBySlot(EquipmentSlot.LEGS), AC_LEGGINGS)
                && is(entity.getItemBySlot(EquipmentSlot.FEET), AC_BOOTS);
    }
}
