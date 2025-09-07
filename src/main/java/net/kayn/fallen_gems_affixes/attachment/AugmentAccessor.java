package net.kayn.fallen_gems_affixes.attachment;

import net.kayn.fallen_gems_affixes.types.augment.IAugmentAccessor;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentHandler;

public class AugmentAccessor implements IAugmentAccessor {
    @Override
    public IAugmentHandler getHandler() {
        return new IAugmentHandler() {};
    }

    @Override
    public IAugmentContainer getContainer() {
        return new IAugmentContainer() {};
    }
}
