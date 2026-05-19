package net.kayn.fallen_gems_affixes.adventure.set.colossus;

import net.minecraft.world.entity.LivingEntity;

public class ColossusCooldownHelper {

    public static final String HELMET_CD = "helmet";
    public static final String CHEST_CD = "chest";
    public static final String LEGS_CD = "legs";
    public static final String BOOTS_CD = "boots";
    public static final String WEAPON_CD = "weapon";

    private static final String PREFIX = "fga.colossus_cd.";

    public static boolean isOnCooldown(LivingEntity entity, String key) {
        return entity.level().getGameTime() < entity.getPersistentData().getLong(PREFIX + key);
    }

    public static void setCooldown(LivingEntity entity, String key, int ticks) {
        entity.getPersistentData().putLong(PREFIX + key, entity.level().getGameTime() + ticks);
    }

    public static void reduceCooldown(LivingEntity entity, String key, int ticks) {
        long current = entity.getPersistentData().getLong(PREFIX + key);
        long now = entity.level().getGameTime();
        entity.getPersistentData().putLong(PREFIX + key, Math.max(now, current - ticks));
    }

    public static void clearCooldown(LivingEntity entity, String key) {
        entity.getPersistentData().putLong(PREFIX + key, 0L);
    }

    public static void reduceAllCooldowns(int reduction, LivingEntity entity) {
        reduceCooldown(entity, HELMET_CD, reduction);
        reduceCooldown(entity, CHEST_CD, reduction);
        reduceCooldown(entity, LEGS_CD, reduction);
        reduceCooldown(entity, BOOTS_CD, reduction);
        reduceCooldown(entity, WEAPON_CD, reduction);
    }

    public static void clearAllCooldowns(LivingEntity entity) {
        clearCooldown(entity, HELMET_CD);
        clearCooldown(entity, CHEST_CD);
        clearCooldown(entity, LEGS_CD);
        clearCooldown(entity, BOOTS_CD);
        clearCooldown(entity, WEAPON_CD);
    }

    private ColossusCooldownHelper() {}
}