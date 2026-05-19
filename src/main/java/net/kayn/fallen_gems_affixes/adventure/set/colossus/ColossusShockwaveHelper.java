package net.kayn.fallen_gems_affixes.adventure.set.colossus;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class ColossusShockwaveHelper {

    public static void release(Player player, double cx, double cy, double cz,
                               float damage, double radius, Level level) {
        if (level.isClientSide) return;
        AABB area = new AABB(cx - radius, cy - radius, cz - radius,
                cx + radius, cy + radius, cz + radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && !e.isDeadOrDying()
                        && !(e instanceof Player)
                        && e.distanceToSqr(cx, cy, cz) <= radius * radius);

        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().playerAttack(player), damage);
            double ddx = target.getX() - cx;
            double ddz = target.getZ() - cz;
            double dist = Math.sqrt(ddx * ddx + ddz * ddz);
            if (dist > 0.01) target.knockback(1.5, -ddx / dist, -ddz / dist);
        }

        if (level instanceof ServerLevel sl) {
            int count = Math.max(10, (int)(radius * 5));
            sl.sendParticles(ParticleTypes.EXPLOSION, cx, cy + 0.5, cz, count / 3, radius * 0.5, 0.3, radius * 0.5, 0.03);
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, cx, cy + 0.5, cz, count, radius * 0.7, 0.1, radius * 0.7, 0);
            sl.sendParticles(ParticleTypes.CRIT, cx, cy + 0.5, cz, count, radius * 0.4, 0.3, radius * 0.4, 0.1);
        }
    }

    public static void releaseMega(Player player, float damage, double radius, Level level) {
        release(player, player.getX(), player.getY() + 0.5, player.getZ(), damage, radius, level);
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    player.getX(), player.getY() + 1, player.getZ(), 3, 0, 0, 0, 0);
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY() + 1, player.getZ(),
                    40, radius * 0.4, 1.0, radius * 0.4, 0.4);
        }
    }

    private ColossusShockwaveHelper() {}
}