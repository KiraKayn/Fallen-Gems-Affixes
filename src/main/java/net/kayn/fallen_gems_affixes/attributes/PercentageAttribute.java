package net.kayn.fallen_gems_affixes.attributes;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class PercentageAttribute extends RangedAttribute {

    public PercentageAttribute(String id, double defaultValue, double minValue, double maxValue) {
        super(id, defaultValue, minValue, maxValue);
    }

    @Override
    public PercentageAttribute setSyncable(boolean syncable) {
        super.setSyncable(syncable);
        return this;
    }
}