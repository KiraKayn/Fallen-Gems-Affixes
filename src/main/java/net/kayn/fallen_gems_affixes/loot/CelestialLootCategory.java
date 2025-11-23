package net.kayn.fallen_gems_affixes.loot;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.thecelestialworkshop.celestisynth.api.item.CSWeapon;

import java.util.Set;

public class CelestialLootCategory {

    static final Set<LootCategory> RANGED_SET = Set.of(LootCategory.BOW, LootCategory.CROSSBOW, LootCategory.TRIDENT);

    public static final LootCategory CELESTIAL_MELEE = LootCategory.register(
            LootCategory.BOW,
            "celestial_melee",
            CelestialLootCategory::isCelestialMelee,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND}
    );

    public static final LootCategory CELESTIAL_RANGED = LootCategory.register(
            LootCategory.BOW,
            "celestial_ranged",
            CelestialLootCategory::isCelestialRanged,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND}
    );

    private static boolean isCelestialMelee(ItemStack stack) {
        return stack.getItem() instanceof CSWeapon && !isRanged(stack);
    }

    private static boolean isCelestialRanged(ItemStack stack) {
        return stack.getItem() instanceof CSWeapon && isRanged(stack);
    }

    private static boolean isRanged(ItemStack stack) {
        return RANGED_SET.stream().anyMatch(c -> c.isValid(stack));
    }
}