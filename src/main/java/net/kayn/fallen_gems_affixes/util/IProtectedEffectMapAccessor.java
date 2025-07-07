package net.kayn.fallen_gems_affixes.util;

import net.minecraft.world.effect.MobEffect;

import java.util.Map;

public interface IProtectedEffectMapAccessor {
    Map<MobEffect, Integer> getProtectedMobEffectMap();
}
