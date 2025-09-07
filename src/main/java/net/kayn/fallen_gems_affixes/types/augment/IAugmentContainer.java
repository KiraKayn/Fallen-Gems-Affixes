package net.kayn.fallen_gems_affixes.types.augment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IAugmentContainer {
    void addAugment(IAugment augment);

    void removeAugment(ResourceLocation id);

    boolean hasAugment(ResourceLocation id);

    List<IAugment> getAugments();

    CompoundTag serializeNBT();

    void deserializeNBT(CompoundTag nbt);
}
