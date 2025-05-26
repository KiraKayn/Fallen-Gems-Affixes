package net.kayn.fallen_gems_affixes;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class AALootCategories {

    public static final LootCategory GUN;

    static {
        if (ModList.get().isLoaded("scguns")) {
            GUN = LootCategory.register(LootCategory.SWORD, "gun",
                    s -> s.getItem() instanceof top.ribs.scguns.item.GunItem, arr(EquipmentSlot.MAINHAND));
        } else {
            GUN = null;
        }
    }

    private static EquipmentSlot[] arr(EquipmentSlot... s) {
        return s;
    }

    public static boolean isGun(ItemStack stack) {
        return GUN != null && LootCategory.forItem(stack).equals(GUN);
    }

    public static void init() {}
}