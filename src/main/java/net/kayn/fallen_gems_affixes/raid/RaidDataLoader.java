package net.kayn.fallen_gems_affixes.raid;

import com.google.gson.*;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;

public class RaidDataLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();

    public RaidDataLoader() {
        super(GSON, "raids");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager rm, ProfilerFiller profiler) {
        RaidManager.get().clearAllDefinitions();

        for (var entry : prepared.entrySet()) {
            try {
                RaidData raid = parse(entry.getKey(), entry.getValue().getAsJsonObject());
                RaidManager.get().registerRaid(raid);
            } catch (Exception e) {
                FallenGemsAffixes.LOGGER.error("Failed to load raid {}: {}", entry.getKey(), e.getMessage());
            }
        }
    }

    private RaidData parse(ResourceLocation key, JsonObject obj) {
        ResourceLocation id = new ResourceLocation(
                key.getNamespace(),
                obj.has("raid_id") ? obj.get("raid_id").getAsString() : key.getPath()
        );

        long cooldown = obj.get("cooldown_per_player").getAsLong();
        int setup = obj.get("setup_time").getAsInt();
        double chance = obj.get("chance_per_player").getAsDouble();
        int spawnDistance = obj.get("spawn_distance").getAsInt();
        int maxDistance = obj.get("max_distance").getAsInt();

        List<RaidData.RaidWave> waves = new ArrayList<>();
        for (JsonElement el : obj.getAsJsonArray("waves")) {
            JsonObject w = el.getAsJsonObject();
            Map<ResourceLocation, Integer> bosses = new LinkedHashMap<>();
            for (var e : w.getAsJsonObject("bosses").entrySet()) {
                bosses.put(new ResourceLocation(e.getKey()), e.getValue().getAsInt());
            }
            int cooldownWave = w.has("wave_cooldown") ? w.get("wave_cooldown").getAsInt() : 100;
            waves.add(new RaidData.RaidWave(bosses, cooldownWave));
        }

        ResourceLocation leader = obj.has("leader") ? new ResourceLocation(obj.get("leader").getAsString()) : null;

        return new RaidData(id, cooldown, setup, chance, spawnDistance, maxDistance, waves, leader);
    }
}