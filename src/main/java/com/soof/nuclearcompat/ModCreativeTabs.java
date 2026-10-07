package com.soof.nuclearcompat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * 把本模组新增/关联的矿石插入到两个前置模组的创造物品栏中：
 * 1. Alex's Caves Up 的「毒化洞穴」tab（alexscaves:toxic_caves）：
 *    新增的辐射铁/煤/铅/硝酸盐矿石跟在辐射铀矿石（radrock_uranium_ore）后。
 * 2. Create Nuclear 的主 tab（createnuclear:main）：
 *    辐射铀矿石、辐射铅矿石、辐射硝酸盐矿石跟在深板岩硝酸盐矿石（deepslate_nitrate_ore）后。
 */
@EventBusSubscriber(modid = NuclearCompat.MODID)
public final class ModCreativeTabs {

    private static final ResourceKey<CreativeModeTab> AC_TOXIC_CAVES =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath("alexscaves", "toxic_caves"));
    private static final ResourceKey<CreativeModeTab> CN_MAIN =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath("createnuclear", "main"));

    private ModCreativeTabs() {
    }

    @SubscribeEvent
    public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        CreativeModeTab.TabVisibility vis = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;

        if (event.getTabKey().equals(AC_TOXIC_CAVES)) {
            // 顺序：radrock_uranium_ore, iron, coal, lead, nitrate
            ItemStack anchor = itemStack("alexscaves", "radrock_uranium_ore");
            ItemStack iron = new ItemStack(ModBlocks.RADROCK_IRON_ORE.get());
            ItemStack coal = new ItemStack(ModBlocks.RADROCK_COAL_ORE.get());
            ItemStack lead = new ItemStack(ModBlocks.RADROCK_LEAD_ORE.get());
            ItemStack nitrate = new ItemStack(ModBlocks.RADROCK_NITRATE_ORE.get());
            event.insertAfter(anchor, iron, vis);
            event.insertAfter(iron, coal, vis);
            event.insertAfter(coal, lead, vis);
            event.insertAfter(lead, nitrate, vis);
        }

        if (event.getTabKey().equals(CN_MAIN)) {
            // 顺序：deepslate_nitrate_ore, radrock_uranium_ore, lead, nitrate
            ItemStack anchor = itemStack("createnuclear", "deepslate_nitrate_ore");
            ItemStack uranium = itemStack("alexscaves", "radrock_uranium_ore");
            ItemStack lead = new ItemStack(ModBlocks.RADROCK_LEAD_ORE.get());
            ItemStack nitrate = new ItemStack(ModBlocks.RADROCK_NITRATE_ORE.get());
            event.insertAfter(anchor, uranium, vis);
            event.insertAfter(uranium, lead, vis);
            event.insertAfter(lead, nitrate, vis);
        }
    }

    private static ItemStack itemStack(String namespace, String path) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
        return new ItemStack(item);
    }
}
