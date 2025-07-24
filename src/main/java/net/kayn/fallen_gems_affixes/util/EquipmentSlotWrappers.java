package net.kayn.fallen_gems_affixes.util;

import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import net.minecraft.world.entity.EquipmentSlot;

public class EquipmentSlotWrappers {
    public static final EquipmentSlotWrapper NONE = new EquipmentSlotWrapper(null, "NONE", null);
    public static final EquipmentSlotWrapper HEAD = new EquipmentSlotWrapper(EquipmentSlot.HEAD, EquipmentSlot.HEAD.getName(), ALObjects.EquipmentSlots.HEAD);
    public static final EquipmentSlotWrapper CHEST = new EquipmentSlotWrapper(EquipmentSlot.CHEST, EquipmentSlot.CHEST.getName(), ALObjects.EquipmentSlots.CHEST);
    public static final EquipmentSlotWrapper LEGS = new EquipmentSlotWrapper(EquipmentSlot.LEGS, EquipmentSlot.LEGS.getName(), ALObjects.EquipmentSlots.LEGS);
    public static final EquipmentSlotWrapper FEET = new EquipmentSlotWrapper(EquipmentSlot.FEET, EquipmentSlot.FEET.getName(), ALObjects.EquipmentSlots.FEET);;
    public static final EquipmentSlotWrapper MAIN_HAND = new EquipmentSlotWrapper(EquipmentSlot.MAINHAND, EquipmentSlot.MAINHAND.getName(), ALObjects.EquipmentSlots.MAINHAND);
    public static final EquipmentSlotWrapper OFF_HAND = new EquipmentSlotWrapper(EquipmentSlot.OFFHAND, EquipmentSlot.OFFHAND.getName(), ALObjects.EquipmentSlots.OFFHAND);
}
