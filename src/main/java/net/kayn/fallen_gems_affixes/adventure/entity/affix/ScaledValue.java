package net.kayn.fallen_gems_affixes.adventure.entity.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ScaledValue(float min, float max) {

    public static final Codec<ScaledValue> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("min").forGetter(ScaledValue::min),
            Codec.FLOAT.fieldOf("max").forGetter(ScaledValue::max)
    ).apply(inst, ScaledValue::new));

    public float get(float level) {
        return min + (max - min) * Math.max(0f, Math.min(1f, level));
    }
}