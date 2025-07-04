package net.kayn.fallen_gems_affixes.util;

import net.minecraft.world.entity.EquipmentSlot;

public class EquipmentSlotWrappers {
    public static final EquipmentSlotWrapper NONE = new EquipmentSlotWrapper(null, "NONE");
    public static final EquipmentSlotWrapper HEAD = new EquipmentSlotWrapper(EquipmentSlot.HEAD, EquipmentSlot.HEAD.getName());
    public static final EquipmentSlotWrapper CHEST = new EquipmentSlotWrapper(EquipmentSlot.CHEST, EquipmentSlot.CHEST.getName());
    public static final EquipmentSlotWrapper LEGS = new EquipmentSlotWrapper(EquipmentSlot.LEGS, EquipmentSlot.LEGS.getName());
    public static final EquipmentSlotWrapper FEET = new EquipmentSlotWrapper(EquipmentSlot.FEET, EquipmentSlot.FEET.getName());;
    public static final EquipmentSlotWrapper MAIN_HAND = new EquipmentSlotWrapper(EquipmentSlot.MAINHAND, EquipmentSlot.MAINHAND.getName());
    public static final EquipmentSlotWrapper OFF_HAND = new EquipmentSlotWrapper(EquipmentSlot.OFFHAND, EquipmentSlot.OFFHAND.getName());
}
