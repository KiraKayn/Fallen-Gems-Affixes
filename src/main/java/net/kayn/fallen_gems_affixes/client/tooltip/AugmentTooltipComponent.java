package net.kayn.fallen_gems_affixes.client.tooltip;

import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;


 //Holds data for an Empty Augment slot tooltip.
 //Right now just a marker class, can hold fields later (e.g. number of slots).

public record AugmentTooltipComponent(IAugment augment, IAugmentInnerData augmentInnerData) implements TooltipComponent {
}
