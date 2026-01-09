package net.kayn.fallen_gems_affixes.raid;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RaidManager {

    private static final RaidManager INSTANCE = new RaidManager();

    private final Map<ResourceLocation, RaidData> definitions = new ConcurrentHashMap<>();
    private final Map<UUID, RaidInstance> active = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastRollTick = new ConcurrentHashMap<>();

    private RaidManager() {
    }

    public static RaidManager get() {
        return INSTANCE;
    }

    public void registerRaid(RaidData data) {
        definitions.put(data.id, data);
    }

    public Optional<RaidData> getRaid(ResourceLocation id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public Collection<RaidData> knownRaids() {
        return definitions.values();
    }

    public void clearAllDefinitions() {
        definitions.clear();
    }

    public boolean isPlayerInRaid(UUID player) {
        return active.containsKey(player);
    }

    public Optional<RaidInstance> getActiveFor(UUID player) {
        return Optional.ofNullable(active.get(player));
    }

    public void startRaid(ServerPlayer player, RaidData raid, boolean ignoreCooldown) {
        UUID pu = player.getUUID();
        long now = player.serverLevel().getGameTime();

        if (!ignoreCooldown) {
            long last = lastRollTick.getOrDefault(pu, -Long.MAX_VALUE);
            if (now - last < raid.cooldownPerPlayer) {
                player.displayClientMessage(Component.translatable("raid.already_on_cooldown"), true);
                return;
            }
        }
        if (active.containsKey(pu)) {
            player.displayClientMessage(Component.translatable("raid.already_active"), true);
            return;
        }

        RaidInstance inst = new RaidInstance(player, raid);
        active.put(pu, inst);
        lastRollTick.put(pu, now);
        inst.scheduleStart(now + raid.setupTime);

        player.displayClientMessage(Component.translatable("raid.started"), false);
    }

    public void stopRaid(UUID playerUuid, boolean failed, String failKey) {
        RaidInstance inst = active.remove(playerUuid);
        if (inst != null) inst.terminate(failed, failKey);
    }

    public void onTick(ServerLevel level) {
        long currentTick = level.getGameTime();

        for (ServerPlayer p : level.players()) {
            if (p == null || p.isSpectator()) continue;
            UUID pu = p.getUUID();

            if (isPlayerInRaid(pu)) {
                getActiveFor(pu).ifPresent(RaidInstance::onTick);
                continue;
            }
            for (RaidData rd : definitions.values()) {
                if (rd.chancePerPlayer <= 0) continue;

                long lastRoll = lastRollTick.getOrDefault(pu, -Long.MAX_VALUE);
                if (currentTick - lastRoll < rd.cooldownPerPlayer) continue;

                lastRollTick.put(pu, currentTick);

                if (p.serverLevel().random.nextDouble() < rd.chancePerPlayer) {
                    startRaid(p, rd, true);
                    break;
                }
            }
        }
    }

    public void onEntityDeath(UUID entityUuid) {
        for (RaidInstance inst : active.values()) {
            if (inst.notifyEntityDeath(entityUuid)) return;
        }
    }

    public void onPlayerDeath(UUID playerUuid) {
        if (active.containsKey(playerUuid)) stopRaid(playerUuid, true, "raid.failed.player_dead");
    }
}