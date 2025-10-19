package net.kayn.fallen_gems_affixes.types.augment;

import net.kayn.fallen_gems_affixes.attachment.AugmentInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IAugment {


     //Every augment must have a unique ID.
     // Used for serialization

    ResourceLocation getId();

    default IAugmentInnerData parse(CompoundTag augmentData) {
        if (!needsInstance()) {
            return null;
        }
        return new EmptyAugmentInnerData();
    }

    boolean isUnique();

    boolean needsInstance();

    default AugmentInstance createInstanceFromStack(ItemStack stack) {
        if (!needsInstance()) {
            return null;
        }
        return new AugmentInstance();
    }
}