package net.kayn.fallen_gems_affixes.loot;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.thecelestialworkshop.celestisynth.api.item.CSWeapon;

public class CelestialLootCategory {

    public static final LootCategory CELESTIAL_MELEE = LootCategory.register(
            LootCategory.SWORD,
            "celestial_melee",
            CelestialLootCategory::isCelestialWeapon,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND}
    );

    public static final LootCategory CELESTIAL_RANGED = LootCategory.register(
            LootCategory.BOW,
            "celestial_ranged",
            CelestialLootCategory::isCelestialWeapon,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND}
    );

    private static boolean isCelestialWeapon(ItemStack stack) {
        if (stack.getItem() instanceof CSWeapon) {
            return true;
        }

        String className = stack.getItem().getClass().getName();
        return className.startsWith("org.thecelestialworkshop.celestisynth.common.item.weapons");
    }
}