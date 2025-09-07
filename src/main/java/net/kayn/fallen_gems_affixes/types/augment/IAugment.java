package net.kayn.fallen_gems_affixes.types.augment;

import net.minecraft.resources.ResourceLocation;

public interface IAugment {


     //Every augment must have a unique ID.
     // Used for serialization

    ResourceLocation getId();
}