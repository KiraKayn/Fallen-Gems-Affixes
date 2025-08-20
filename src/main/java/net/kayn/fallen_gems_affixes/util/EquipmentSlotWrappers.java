package net.kayn.fallen_gems_affixes.util;

import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.apothic_attributes.compat.CurioEquipmentSlot;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EquipmentSlotWrappers {
    public static final Map<String, EquipmentSlotWrapper> curioWrappers = new HashMap<>();
    @SuppressWarnings("ConstantConditions")
    public static final EquipmentSlotWrapper NONE = new EquipmentSlotWrapper(null, "NONE", null);

    public static final EquipmentSlotWrapper HEAD = new EquipmentSlotWrapper(EquipmentSlot.HEAD, EquipmentSlot.HEAD.getName(), ALObjects.EquipmentSlots.HEAD);
    public static final EquipmentSlotWrapper CHEST = new EquipmentSlotWrapper(EquipmentSlot.CHEST, EquipmentSlot.CHEST.getName(), ALObjects.EquipmentSlots.CHEST);
    public static final EquipmentSlotWrapper LEGS = new EquipmentSlotWrapper(EquipmentSlot.LEGS, EquipmentSlot.LEGS.getName(), ALObjects.EquipmentSlots.LEGS);
    public static final EquipmentSlotWrapper FEET = new EquipmentSlotWrapper(EquipmentSlot.FEET, EquipmentSlot.FEET.getName(), ALObjects.EquipmentSlots.FEET);
    public static final EquipmentSlotWrapper MAIN_HAND = new EquipmentSlotWrapper(EquipmentSlot.MAINHAND, EquipmentSlot.MAINHAND.getName(), ALObjects.EquipmentSlots.MAINHAND);
    public static final EquipmentSlotWrapper OFF_HAND = new EquipmentSlotWrapper(EquipmentSlot.OFFHAND, EquipmentSlot.OFFHAND.getName(), ALObjects.EquipmentSlots.OFFHAND);
}
