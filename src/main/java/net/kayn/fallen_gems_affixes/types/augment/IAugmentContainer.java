package net.kayn.fallen_gems_affixes.types.augment;

import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;

import java.util.List;
import java.util.Map;

public interface IAugmentContainer {
    void addAugment(AugmentInstance instance);

    boolean removeAugment(IAugment augment);

    boolean removeAugment(AugmentInstance instance);

    boolean hasAugment(IAugment augment);

    Map<IAugment, List<AugmentInstance>> getAugments();
}
