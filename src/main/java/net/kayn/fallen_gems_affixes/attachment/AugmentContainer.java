package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class AugmentContainer implements IAugmentContainer {
    private final List<IAugment> augments = new ArrayList<>();

    @Override
    public void addAugment(IAugment augment) {
        if (!augments.contains(augment)) {
            augments.add(augment);
        }
    }

    @Override
    public void removeAugment(ResourceLocation id) {
        augments.removeIf(a -> a.getId().equals(id));
    }

    @Override
    public boolean hasAugment(ResourceLocation id) {
        return augments.stream().anyMatch(a -> a.getId().equals(id));
    }

    @Override
    public List<IAugment> getAugments() {
        return Collections.unmodifiableList(augments);
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag listTag = new ListTag();
        for (IAugment augment : augments) {
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
                augments.add(augment);
            }
        }
    }
}