package net.kayn.fallen_gems_affixes.raid;

import dev.shadowsoffire.apotheosis.adventure.boss.ApothBoss;
import dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RaidInstance {

    private final ServerPlayer player;
    private final RaidData raid;

    private int waveIndex = -1;
    private boolean finalLeaderSpawned = false;
    private boolean terminated = false;

    private final Map<Integer, Set<UUID>> alive = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> initialCounts = new ConcurrentHashMap<>();

    private long scheduledStart;
    private long nextWaveTick;
    private BlockPos startPos;
    private ServerBossEvent bossBar;

    public RaidInstance(ServerPlayer player, RaidData raid) {
        this.player = player;
        this.raid = raid;
    }

    public RaidData getRaid() {
        return this.raid;
    }

    public void scheduleStart(long tick) {
        scheduledStart = tick;
        startPos = player.blockPosition();
    }

    public void onTick() {
        if (terminated) return;
        ServerLevel level = player.serverLevel();
        long time = level.getGameTime();

        if (waveIndex == -1) {
            if (time >= scheduledStart) advanceWave();
            return;
        }

        if (nextWaveTick > 0 && time >= nextWaveTick) {
            advanceWave();
            nextWaveTick = 0;
        }

        double dx = player.getX() - startPos.getX();
        double dz = player.getZ() - startPos.getZ();
        if (dx * dx + dz * dz > raid.maxDistance * raid.maxDistance) {
            RaidManager.get().stopRaid(player.getUUID(), true, "raid.failed.left_area");
        }
    }

    private void advanceWave() {
        waveIndex++;
        if (waveIndex < raid.waves.size()) spawnWave(waveIndex);
        else if (!finalLeaderSpawned && raid.leader != null) spawnFinalLeader();
        else finishSuccess();
    }

    private void spawnWave(int index) {
        RaidData.RaidWave wave = raid.waves.get(index);
        Set<UUID> set = ConcurrentHashMap.newKeySet();

        for (var e : wave.bosses.entrySet()) {
            ApothBoss boss = BossRegistry.INSTANCE.getValue(e.getKey());
            if (boss == null) {
                FallenGemsAffixes.LOGGER.warn("Boss {} not found in registry!", e.getKey());
                continue;
            }

            for (int i = 0; i < e.getValue(); i++) {
                Mob mob = spawnBossAtRandomNearPlayer(boss);
                if (mob != null) set.add(mob.getUUID());
            }
        }

        alive.put(index, set);
        initialCounts.put(index, set.size());
        updateBossBar();
    }

    private void spawnFinalLeader() {
        finalLeaderSpawned = true;
        ApothBoss boss = BossRegistry.INSTANCE.getValue(raid.leader);
        if (boss == null) {
            finishSuccess();
            return;
        }

        Mob mob = spawnBossAtRandomNearPlayer(boss);
        if (mob != null) {
            Set<UUID> set = ConcurrentHashMap.newKeySet();
            set.add(mob.getUUID());
            alive.put(waveIndex, set);
            initialCounts.put(waveIndex, 1);
            updateBossBar();
        }
    }

    private Mob spawnBossAtRandomNearPlayer(ApothBoss boss) {
        ServerLevel level = player.serverLevel();
        BlockPos base = player.blockPosition();

        int offsetX = level.random.nextInt(raid.spawnDistance * 2 + 1) - raid.spawnDistance;
        int offsetZ = level.random.nextInt(raid.spawnDistance * 2 + 1) - raid.spawnDistance;
        BlockPos posXZ = base.offset(offsetX, 0, offsetZ);

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, posXZ.getX(), posXZ.getZ());
        BlockPos spawnPos = new BlockPos(posXZ.getX(), y, posXZ.getZ());

        Mob mob = boss.createBoss(level, spawnPos, level.random, 0);
        if (mob != null) level.addFreshEntity(mob);
        return mob;
    }

    public boolean notifyEntityDeath(UUID uuid) {
        Set<UUID> set = alive.get(waveIndex);
        if (set == null || !set.remove(uuid)) return false;

        updateBossBar();

        if (set.isEmpty()) {
            if (waveIndex < raid.waves.size()) {
                nextWaveTick = player.serverLevel().getGameTime() + raid.waves.get(waveIndex).waveCooldown;
            } else {
                finishSuccess();
            }
        }
        return true;
    }

    private void updateBossBar() {
        Set<UUID> set = alive.getOrDefault(waveIndex, Set.of());
        int initial = Math.max(1, initialCounts.getOrDefault(waveIndex, 1));

        Component title = (waveIndex < raid.waves.size())
                ? Component.translatable("raid.wave.title", raid.id.getPath(), waveIndex + 1, set.size())
                : Component.translatable("raid.final_boss", raid.id.getPath());

        if (bossBar == null) {
            bossBar = new ServerBossEvent(title, BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
            bossBar.addPlayer(player);
        } else bossBar.setName(title);

        bossBar.setProgress((float) set.size() / initial);
    }

    public void terminate(boolean failed, String key) {
        terminated = true;
        if (bossBar != null) bossBar.removePlayer(player);
        if (failed) player.displayClientMessage(Component.translatable(key), false);
    }

    private void finishSuccess() {
        terminate(false, "raid.complete");
    }
}