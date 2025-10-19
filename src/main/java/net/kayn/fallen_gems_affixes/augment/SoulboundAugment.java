package net.kayn.fallen_gems_affixes.augment;

import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.resources.ResourceLocation;

public class SoulboundAugment implements IAugment {
    private static final ResourceLocation SOULBOUND_ID = new ResourceLocation("fallen_gems_affixes", "soulbound");

    @Override
    public ResourceLocation getId() {
        return SOULBOUND_ID;
    }

    public static ResourceLocation augmentId() {
        return SOULBOUND_ID;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public boolean needsInstance() {
        return false;
    }

    @Override
    public String toString() {
        return "SoulboundAugment{" + "id=" + augmentId() + "}";
    }
}