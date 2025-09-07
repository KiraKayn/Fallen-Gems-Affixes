package net.kayn.fallen_gems_affixes.types.augment;

import net.minecraft.resources.ResourceLocation;

public interface IAugmentHandler {
    boolean addAugment(ResourceLocation id);
    boolean removeAugment(ResourceLocation id);
    boolean hasAugment(ResourceLocation id);

    IAugmentContainer getContainer();
}