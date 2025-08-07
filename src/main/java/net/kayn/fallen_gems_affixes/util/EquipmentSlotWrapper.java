package net.kayn.fallen_gems_affixes.util;

import dev.shadowsoffire.apothic_attributes.modifiers.EntityEquipmentSlot;
import dev.shadowsoffire.apothic_attributes.modifiers.EntitySlotGroup;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EquipmentSlotWrapper {
    public final EquipmentSlot slot;
    private final String identifier;
    private static final Map<EquipmentSlot, EquipmentSlotWrapper> vanillaEquipmentWrapper = new IdentityHashMap<>();
    private static final Map<String, EquipmentSlotWrapper> allEquipmentWrappers = new HashMap<>();
    private static final Map<String, Holder<EntityEquipmentSlot>> extraEntries = new HashMap<>();
    private static final Map<Holder<EntityEquipmentSlot>, EquipmentSlotWrapper> instanceEntries = new IdentityHashMap<>();

    public EquipmentSlotWrapper(@Nullable EquipmentSlot slot, @NotNull String identifier, Holder<EntityEquipmentSlot> extra) {
        this.slot = slot;
        if (slot != null) {
            vanillaEquipmentWrapper.put(slot, this);
        }
        this.identifier = identifier;
        allEquipmentWrappers.put(identifier, this);
        if (extra != null) {
            extraEntries.put(identifier, extra);
            instanceEntries.put(extra, this);
        }
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

    public Holder<EntityEquipmentSlot> extractHolder() {
        return extraEntries.get(identifier);
    }

    public boolean isEmpty() {
        return this == EquipmentSlotWrappers.NONE || this.extractHolder() == null;
    }

    public static EquipmentSlotWrapper byESlot(Holder<EntityEquipmentSlot> eSlot) {
        return instanceEntries.get(eSlot);
    }

    @Override
    @NotNull
    public String toString() {
        return this.identifier;
    }
}
