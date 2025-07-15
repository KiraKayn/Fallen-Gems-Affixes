package net.kayn.fallen_gems_affixes.util;

import net.minecraft.world.effect.MobEffect;

import java.util.*;

public class EffectInstanceBucket {
    private final MobEffect effect;
    private final TreeSet<Integer> instances = new TreeSet<>();

    public EffectInstanceBucket(MobEffect effect) {
        this.effect = effect;
    }

    public void add(int amplifier) {
        instances.add(amplifier);
    }

    public boolean contains(int amplifier) {
        return instances.contains(amplifier);
    }

    public MobEffect getEffect() {
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
