package net.kayn.fallen_gems_affixes.util;

import dev.shadowsoffire.apothic_attributes.modifiers.EntityEquipmentSlot;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

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

    public Holder<EntityEquipmentSlot> extractApothHolder() {
        return extraEntries.get(identifier);
    }

    public boolean isEmpty() {
        return this == EquipmentSlotWrappers.NONE || this.extractApothHolder() == null;
    }

    public static EquipmentSlotWrapper byESlot(Holder<EntityEquipmentSlot> eSlot) {
        EquipmentSlotWrapper wrapper = instanceEntries.get(eSlot);
        if (wrapper == null) return EquipmentSlotWrappers.NONE;
        return wrapper;
    }

    @Override
    @NotNull
    public String toString() {
        return this.identifier;
    }
}
