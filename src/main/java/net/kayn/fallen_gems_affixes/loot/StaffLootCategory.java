package net.kayn.fallen_gems_affixes.loot;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class StaffLootCategory {

    public static final LootCategory STAFF = LootCategory.register(
            LootCategory.SWORD,
            "staffs",
            StaffLootCategory::isStaffItem,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND}
    );

    private static boolean isStaffItem(ItemStack stack) {
        return stack.getItem() instanceof StaffItem;
    }
}