package net.kayn.fallen_gems_affixes.util;

import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class EquipmentSlotWrapper {
    private final EquipmentSlot slot;
    private final String identifier;
    private static final Map<EquipmentSlot, EquipmentSlotWrapper> vanillaEquipmentWrapper = new HashMap<>();
    private static final Map<String, EquipmentSlotWrapper> allEquipmentWrappers = new HashMap<>();

    public EquipmentSlotWrapper(@Nullable EquipmentSlot slot, @NotNull String identifier) {
        this.slot = slot;
        if (slot != null) {
            vanillaEquipmentWrapper.put(slot, this);
        }
        this.identifier = identifier;
        allEquipmentWrappers.put(identifier, this);
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static Map<EquipmentSlot, EquipmentSlotWrapper> getVanillaWrapper() {
        return vanillaEquipmentWrapper;
    }

    public static Map<String, EquipmentSlotWrapper> getAll() {
        return allEquipmentWrappers;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj || this.slot != null && this.slot == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EquipmentSlotWrapper other = (EquipmentSlotWrapper) obj;

        return this.identifier.equals(other.identifier);
    }

    @Override
    public String toString() {
        return this.identifier;
    }
}
