package net.kayn.fallen_gems_affixes.types.augment;

import net.minecraft.nbt.CompoundTag;

public class EmptyAugmentInnerData implements IAugmentInnerData {
    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @Override
    public boolean isFunctional() {
        return false;
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {}
}
