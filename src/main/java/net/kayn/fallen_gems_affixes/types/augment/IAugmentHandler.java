package net.kayn.fallen_gems_affixes.types.augment;

import net.kayn.fallen_gems_affixes.attachment.AugmentInstance;
import net.minecraft.resources.ResourceLocation;

public interface IAugmentHandler {
    void addAugment(AugmentInstance instance);

    boolean removeAugment(IAugment augment);

    boolean hasAugment(IAugment augment);

    IAugmentContainer getContainer();
}