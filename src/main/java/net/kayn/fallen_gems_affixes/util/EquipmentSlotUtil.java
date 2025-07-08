package net.kayn.fallen_gems_affixes.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EquipmentSlotUtil {
    public static EquipmentSlotWrapper getVanillaWrapper(@NotNull EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> EquipmentSlotWrappers.HEAD;
            case CHEST -> EquipmentSlotWrappers.CHEST;
            case LEGS -> EquipmentSlotWrappers.LEGS;
            case FEET -> EquipmentSlotWrappers.FEET;
            case OFFHAND -> EquipmentSlotWrappers.OFF_HAND;
            case MAINHAND -> EquipmentSlotWrappers.MAIN_HAND;
            default -> null;
        };
    }

    public static EquipmentSlot slotFromAllSlotsIndex(int index) {
        return switch (index) {
            case 0 -> EquipmentSlot.MAINHAND;
            case 1 -> EquipmentSlot.OFFHAND;
            case 2 -> EquipmentSlot.FEET;
            case 3 -> EquipmentSlot.LEGS;
            case 4 -> EquipmentSlot.CHEST;
            case 5 -> EquipmentSlot.HEAD;
            default -> null;
        };
    }

    public static EquipmentSlot slotFromInventoryIndex(int index) {
        return switch (index) {
            case 40 -> EquipmentSlot.OFFHAND;
            case 39 -> EquipmentSlot.FEET;
            case 38 -> EquipmentSlot.LEGS;
            case 37 -> EquipmentSlot.CHEST;
            case 36 -> EquipmentSlot.HEAD;
            default -> null;
        };
    }

    public static boolean matchesSlot(ItemStack itemStack, EquipmentSlot givenSlot) {
        for (EquipmentSlot slot : LootCategory.forItem(itemStack).getSlots()) {
            if (givenSlot == slot) return true;
        }
        return false;
    }

    public static Iterable<ItemStack> getOffHandAndArmors(Player player){
        return Iterables.concat(Lists.newArrayList(player.getOffhandItem()), player.getArmorSlots());
    }
}
