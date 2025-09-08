package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class AugmentContainer implements IAugmentContainer {
    private final Map<IAugment, AugmentInstance> augments = new HashMap<>();

    @Override
    public void addAugment(IAugment augment, AugmentInstance instance) {
        if (!augments.containsKey(augment)) {
            augments.put(augment, instance);
        }
    }

    @Override
    public void removeAugment(ResourceLocation id) {
        IAugment augment = AugmentRegistry.get(id);
        if (augment != null) {
            augments.remove(augment);
        }
    }

    @Override
    public boolean hasAugment(ResourceLocation id) {
        IAugment augment = AugmentRegistry.get(id);
        if (augment != null) {
            return augments.containsKey(augment);
        }
        return false;
    }

    @Override
    public Map<IAugment, AugmentInstance> getAugments() {
        return Collections.unmodifiableMap(augments);
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag listTag = new ListTag();
        for (IAugment augment : augments.keySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", augment.getId().toString());
            listTag.add(tag);
        }
        CompoundTag result = new CompoundTag();
        result.put("Augments", listTag);
        return result;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        augments.clear();
        ListTag list = nbt.getList("Augments", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag augmentTag = (CompoundTag) t;
            ResourceLocation id = new ResourceLocation(augmentTag.getString("id"));
            IAugment augment = AugmentRegistry.get(id);
            if (augment != null) {
                augments.put(augment, new AugmentInstance(augment));
            }
        }
    }
}