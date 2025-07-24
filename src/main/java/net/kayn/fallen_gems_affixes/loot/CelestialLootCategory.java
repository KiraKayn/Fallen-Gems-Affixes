//package net.kayn.fallen_gems_affixes.loot;
//
//import dev.shadowsoffire.apotheosis.loot.LootCategory;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.tags.ItemTags;
//import net.minecraft.world.entity.EquipmentSlot;
//import net.minecraft.world.item.ItemStack;
//
//public class CelestialLootCategory {
//
//    public static final LootCategory CELESTIAL_WEAPONS = LootCategory.register(
//            LootCategory.SWORD,
//            "celestial_weapons",
//            CelestialLootCategory::isCelestialWeapon,
//            new EquipmentSlot[]{EquipmentSlot.MAINHAND}
//    );
//
//    private static boolean isCelestialWeapon(ItemStack stack) {
//        return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "celestial_weapons")));
//    }
//}