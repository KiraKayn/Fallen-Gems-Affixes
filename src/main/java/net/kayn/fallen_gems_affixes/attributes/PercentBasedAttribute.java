package net.kayn.fallen_gems_affixes.attributes;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.jetbrains.annotations.NotNull;

public class PercentBasedAttribute extends RangedAttribute {

    public PercentBasedAttribute(String id, double defaultValue, double minValue, double maxValue) {
        super(id, defaultValue, minValue, maxValue);
    }

    @Override
    public @NotNull PercentBasedAttribute setSyncable(boolean syncable) {
        super.setSyncable(syncable);
        return this;
    }
}