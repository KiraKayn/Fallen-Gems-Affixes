package net.kayn.fallen_gems_affixes.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class MiscUtil {
    public static boolean isOnCooldown(ResourceLocation id, float cooldown, LivingEntity entity) {
        long lastApplied = entity.getPersistentData().getLong("fga._cooldown." + id.toString());
        return lastApplied != 0 && lastApplied + cooldown >= entity.level().getGameTime();
    }

    public static void startCooldown(ResourceLocation id, LivingEntity entity) {
        entity.getPersistentData().putLong("fga._cooldown." + id.toString(), entity.level().getGameTime());
    }
}
