package net.kayn.fallen_gems_affixes.util;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.TreeSet;

public class EffectInstanceBucket {
    private final Holder<MobEffect> effect;
    private final TreeSet<Integer> instances = new TreeSet<>();

    public EffectInstanceBucket(Holder<MobEffect> effect) {
        this.effect = effect;
    }

    public void add(int amplifier) {
        instances.add(amplifier);
    }

    public boolean contains(int amplifier) {
        return instances.contains(amplifier);
    }

    public Holder<MobEffect> getEffect() {
        return effect;
    }

    public void remove(int amplifier) {
        instances.remove(amplifier);
    }

    public int size() {
        return instances.size();
    }

    public int getLast() {
        return instances.last();
    }
}
