package net.kayn.fallen_gems_affixes.adventure.set.colossus;


import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public final class BastionOrbManager {

    private static final String ORB_KEY         = "fga.bastion_orbs";
    private static final String ORB_DAMAGE_KEY  = "fga.bastion_orb_damage";
    private static final String STANDING_TICKS  = "fga.colossus_standing_ticks";
    private static final String STANDING_BONUS  = "fga.colossus_standing_bonus";

    public static int getOrbs(Player player) {
        return Mth.clamp(player.getPersistentData().getInt(ORB_KEY), 0, ColossusSetConstants.MAX_ORBS);
    }

    public static void setOrbsDirect(Player player, int count) {
        player.getPersistentData().putInt(ORB_KEY, Mth.clamp(count, 0, ColossusSetConstants.MAX_ORBS));
    }

    public static int addOrbs(Player player, int amount) {
        int current = getOrbs(player);
        int toAdd = Math.min(amount, ColossusSetConstants.MAX_ORBS - current);
        if (toAdd <= 0) return 0;
        int newTotal = current + toAdd;
        setOrbsDirect(player, newTotal);
        MinecraftForge.EVENT_BUS.post(new BastionOrbGainedEvent(player, toAdd, newTotal));
        return toAdd;
    }

    public static int consumeOrbs(Player player, int amount) {
        int current = getOrbs(player);
        int toConsume = Math.min(amount, current);
        if (toConsume <= 0) return 0;
        int newTotal = current - toConsume;
        setOrbsDirect(player, newTotal);
        MinecraftForge.EVENT_BUS.post(new BastionOrbConsumedEvent(player, toConsume, current, newTotal));
        return toConsume;
    }

    public static boolean isUnstoppable(Player player) {
        return getOrbs(player) >= ColossusSetConstants.MAX_ORBS;
    }

    public static float getAccumulatedDamage(Player player) {
        return player.getPersistentData().getFloat(ORB_DAMAGE_KEY);
    }

    public static void addAccumulatedDamage(Player player, float amount) {
        player.getPersistentData().putFloat(ORB_DAMAGE_KEY, getAccumulatedDamage(player) + amount);
    }

    public static void resetAccumulatedDamage(Player player) {
        player.getPersistentData().putFloat(ORB_DAMAGE_KEY, 0f);
    }

    public static boolean isStandingBonusReady(Player player) {
        return player.getPersistentData().getBoolean(STANDING_BONUS);
    }

    public static void setStandingBonusReady(Player player, boolean ready) {
        player.getPersistentData().putBoolean(STANDING_BONUS, ready);
    }

    public static int getStandingTicks(Player player) {
        return player.getPersistentData().getInt(STANDING_TICKS);
    }

    public static void setStandingTicks(Player player, int ticks) {
        player.getPersistentData().putInt(STANDING_TICKS, ticks);
    }

    public static void spawnOrbVisuals(Player player) {
        if (!(player.level() instanceof ServerLevel sl)) return;
        int orbs = getOrbs(player);
        if (orbs == 0) return;

        double[] dx = {0, 1.2, 0, -1.2};
        double[] dz = {-1.2, 0, 1.2, 0};

        for (int i = 0; i < orbs; i++) {
            double x = player.getX() + dx[i % dx.length];
            double y = player.getY() + 1.1;
            double z = player.getZ() + dz[i % dz.length];
            sl.sendParticles(ParticleTypes.END_ROD, x, y, z, 2, 0.04, 0.04, 0.04, 0.005);
        }

        if (orbs >= ColossusSetConstants.MAX_ORBS) {
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY() + 1, player.getZ(),
                    4, 0.3, 0.6, 0.3, 0.15);
        }
    }

    private BastionOrbManager() {}
}
