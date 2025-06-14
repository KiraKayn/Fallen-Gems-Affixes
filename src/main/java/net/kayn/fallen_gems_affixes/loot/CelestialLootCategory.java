package net.kayn.fallen_gems_affixes.loot;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class CelestialLootCategory {

    public static final LootCategory CELESTIAL_WEAPONS = LootCategory.register(
            LootCategory.SWORD,
            "celestial_weapons",
            CelestialLootCategory::isCelestialWeapon,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND}
    );

    private static boolean isCelestialWeapon(ItemStack stack) {
        return stack.is(ItemTags.create(new ResourceLocation("fallen_gems_affixes", "celestial_weapons")));
    }
}