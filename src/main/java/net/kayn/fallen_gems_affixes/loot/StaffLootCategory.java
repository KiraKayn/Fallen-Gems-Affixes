package net.kayn.fallen_gems_affixes.loot;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class StaffLootCategory {

    public static final LootCategory STAFF = LootCategory.register(
            LootCategory.SWORD,
            "staffs",
            StaffLootCategory::CastingItem,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND}
    );

    private static boolean CastingItem (ItemStack stack) {
        return stack.is(ItemTags.create(new ResourceLocation("fallen_gems_affixes", "staffs")));
    }
}