package net.kayn.fallen_gems_affixes.loot;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import net.kayn.fallen_gems_affixes.util.LootCategoryUtil;
import net.minecraft.world.item.ItemStack;
import org.thecelestialworkshop.celestisynth.api.item.CSWeapon;

public class CelestialLootCategory {

    public static final LootCategory CELESTIAL_MELEE = LootCategoryUtil.registerLootCategoryOrFalse(
            "celestial_melee",
            ALObjects.EquipmentSlotGroups.MAINHAND,
            CelestialLootCategory::isCelestialMelee,
            1200,
            true
    );

    public static final LootCategory CELESTIAL_RANGED = LootCategoryUtil.registerLootCategoryOrFalse(
            "celestial_ranged",
            ALObjects.EquipmentSlotGroups.MAINHAND,
            CelestialLootCategory::isCelestialRanged,
            1200,
            true
    );

    private static boolean isCelestialMelee(ItemStack stack) {
        return stack.getItem() instanceof CSWeapon && !isRanged(stack);
    }

    private static boolean isCelestialRanged(ItemStack stack) {
        return stack.getItem() instanceof CSWeapon && isRanged(stack);
    }

    private static boolean isRanged(ItemStack stack) {
        return LootCategory.forItem(stack).isRanged();
    }
}