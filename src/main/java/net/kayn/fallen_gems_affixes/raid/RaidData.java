package net.kayn.fallen_gems_affixes.raid;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RaidData {

    public final ResourceLocation id;
    public final long cooldownPerPlayer;
    public final int setupTime;
    public final double chancePerPlayer;
    public final int spawnDistance;
    public final int maxDistance;
    public final List<RaidWave> waves;
    public final ResourceLocation leader;

    public RaidData(ResourceLocation id, long cooldownPerPlayer, int setupTime, double chancePerPlayer,
                    int spawnDistance, int maxDistance, List<RaidWave> waves, ResourceLocation leader) {
        this.id = id;
        this.cooldownPerPlayer = cooldownPerPlayer;
        this.setupTime = setupTime;
        this.chancePerPlayer = chancePerPlayer;
        this.spawnDistance = spawnDistance;
        this.maxDistance = maxDistance;
        this.waves = List.copyOf(waves);
        this.leader = leader;
    }

    public static final class RaidWave {
        public final Map<ResourceLocation, Integer> bosses;
        public final int waveCooldown;

        public RaidWave(Map<ResourceLocation, Integer> bosses, int waveCooldown) {
            this.bosses = new LinkedHashMap<>(bosses);
            this.waveCooldown = waveCooldown;
        }
    }
}