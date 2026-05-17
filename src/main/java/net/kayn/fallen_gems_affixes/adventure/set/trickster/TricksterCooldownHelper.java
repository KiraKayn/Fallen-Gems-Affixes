package net.kayn.fallen_gems_affixes.adventure.set.trickster;

import net.minecraft.world.entity.player.Player;

public final class TricksterCooldownHelper {
    public static final String CHESTPLATE_CD = "chestplate";
    public static final String BOOTS_CD = "boots";
    public static final String WEAPON_CD = "weapon";
    private static final String PREFIX = "fga.trickster_cd.";

    public static boolean isOnCooldown(Player player, String key) {
        return player.level().getGameTime() < player.getPersistentData().getLong(PREFIX + key);
    }

    public static void setCooldown(Player player, String key, int ticks) {
        player.getPersistentData().putLong(PREFIX + key, player.level().getGameTime() + ticks);
    }

    public static void reduceCooldown(Player player, String key, int ticks) {
        long current = player.getPersistentData().getLong(PREFIX + key);
        long now = player.level().getGameTime();
        player.getPersistentData().putLong(PREFIX + key, Math.max(now, current - ticks));
    }

    public static void clearCooldown(Player player, String key) {
        player.getPersistentData().putLong(PREFIX + key, 0L);
    }

    private TricksterCooldownHelper() {}
}