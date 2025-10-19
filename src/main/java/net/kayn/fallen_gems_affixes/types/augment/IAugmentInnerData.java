package net.kayn.fallen_gems_affixes.types.augment;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IAugmentInnerData extends INBTSerializable<CompoundTag> {
    void enable();
    void disable();
    boolean isFunctional();
}
