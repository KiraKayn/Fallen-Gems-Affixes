package net.kayn.fallen_gems_affixes.types.augment;

import net.kayn.fallen_gems_affixes.attachment.AugmentInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public interface IAugmentContainer {
    void addAugment(IAugment augment, AugmentInstance instance);

    void removeAugment(ResourceLocation id);

    boolean hasAugment(ResourceLocation id);

    Map<IAugment, AugmentInstance> getAugments();

    CompoundTag serializeNBT();

    void deserializeNBT(CompoundTag nbt);
}
