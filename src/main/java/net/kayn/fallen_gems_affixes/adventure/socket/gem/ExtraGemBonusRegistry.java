package net.kayn.fallen_gems_affixes.adventure.socket.gem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

public class ExtraGemBonusRegistry extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {

    public static final ExtraGemBonusRegistry INSTANCE = new ExtraGemBonusRegistry();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    protected Multimap<ResourceLocation, ExtraGemBonus> extraBonuses = HashMultimap.create();
    private Map<ResourceLocation, JsonElement> pendingData = new HashMap<>();
    private boolean hasAppliedBonuses = false;

    @Override
    protected @NotNull Map<ResourceLocation, JsonElement> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> map = new HashMap<>();

        FallenGemsAffixes.LOGGER.info("Loading extra gem bonuses...");

        // Load all JSON files from data/[namespace]/extra_gem_bonuses/ but DON'T parse them yet
        resourceManager.listResources("extra_gem_bonuses", location -> location.getPath().endsWith(".json")).forEach((location, resource) -> {
            try {
                JsonElement jsonElement = GSON.fromJson(resource.openAsReader(), JsonElement.class);

                // Extract the filename without extension as the key
                String path = location.getPath();
                String filename = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
                ResourceLocation key = new ResourceLocation(location.getNamespace(), filename);

                map.put(key, jsonElement);
                FallenGemsAffixes.LOGGER.debug("Loaded JSON for extra gem bonus {}", key);
            } catch (Exception e) {
                FallenGemsAffixes.LOGGER.error("Failed to load extra gem bonus JSON from {}", location, e);
            }
        });

        FallenGemsAffixes.LOGGER.info("Loaded {} extra gem bonus JSON files", map.size());
        return map;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> prepared, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        // Store the prepared data for later application
        this.pendingData = prepared;
        this.hasAppliedBonuses = false;

        // Clear old data
        this.extraBonuses.clear();
        this.clearExtraGemBonuses();

        // Try to apply immediately, but if not ready, schedule for later
        if (!tryApplyBonuses()) {
            FallenGemsAffixes.LOGGER.info("Apotheosis gem registry not ready, scheduling delayed application");
            scheduleDelayedApplication();
        }
    }

    private boolean tryApplyBonuses() {
        // Check if Apotheosis registries are ready
        if (GemRegistry.INSTANCE.getValues().isEmpty()) {
            FallenGemsAffixes.LOGGER.debug("Apotheosis gem registry not ready, has {} gems", GemRegistry.INSTANCE.getValues().size());
            return false;
        }

        FallenGemsAffixes.LOGGER.info("Apotheosis gem registry is ready with {} gems", GemRegistry.INSTANCE.getValues().size());

        // Parse and apply the bonuses
        return parseAndApplyBonuses();
    }

    private boolean parseAndApplyBonuses() {
        boolean res = true;
        if (this.pendingData.isEmpty()) {
            FallenGemsAffixes.LOGGER.debug("No pending data to apply");
        } else {// Parse the JSON files now that registries are ready
            Map<ResourceLocation, ExtraGemBonus> parsedBonuses = new HashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : this.pendingData.entrySet()) {
                ResourceLocation key = entry.getKey();
                JsonElement jsonElement = entry.getValue();

                try {
                    // Parse the JSON using the codec now that registries are ready
                    var result = ExtraGemBonus.CODEC.parse(JsonOps.INSTANCE, jsonElement);

                    if (result.error().isPresent()) {
                        FallenGemsAffixes.LOGGER.error("Failed to parse extra gem bonus {}: {}", key, result.error().get().message());
                        continue;
                    }

                    ExtraGemBonus bonus = result.result().orElse(null);
                    if (bonus != null) {
                        parsedBonuses.put(key, bonus);
                        FallenGemsAffixes.LOGGER.debug("Parsed extra gem bonus {} for gem {}", key, bonus.gemId());
                    }
                } catch (Exception e) {
                    FallenGemsAffixes.LOGGER.error("Failed to parse extra gem bonus {}", key, e);
                }
            }// Store the parsed data
            for (ExtraGemBonus bonus : parsedBonuses.values()) {
                this.extraBonuses.put(bonus.gemId(), bonus);
            }
            FallenGemsAffixes.LOGGER.info("Registered {} extra gem bonuses", this.extraBonuses.size());// Apply the bonuses
            this.applyExtraGemBonuses();
            this.hasAppliedBonuses = true;
        }

        return res;
    }

    private void scheduleDelayedApplication() {
        // Schedule a task to try applying bonuses after a short delay
        CompletableFuture.runAsync(() -> {
            int maxRetries = 10;
            int retryDelay = 1000; // 1 second

            for (int i = 0; i < maxRetries; i++) {
                try {
                    Thread.sleep(retryDelay);

                    if (tryApplyBonuses()) {
                        FallenGemsAffixes.LOGGER.info("Successfully applied extra gem bonuses after {} retries", i + 1);
                        return;
                    }

                    FallenGemsAffixes.LOGGER.debug("Retry {} failed, gem registry still not ready", i + 1);
                } catch (InterruptedException e) {
                    FallenGemsAffixes.LOGGER.error("Delayed application interrupted", e);
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            FallenGemsAffixes.LOGGER.error("Failed to apply extra gem bonuses after {} retries", maxRetries);
        });
    }

    /**
     * Force application of bonuses. Can be called from external events.
     */
    public void forceApplyBonuses() {
        if (!this.hasAppliedBonuses && !this.pendingData.isEmpty()) {
            FallenGemsAffixes.LOGGER.info("Force applying extra gem bonuses");
            tryApplyBonuses();
        }
    }

    public static Collection<ExtraGemBonus> getBonusesFor(ResourceLocation gemId) {
        return INSTANCE.extraBonuses.get(gemId);
    }

    public void applyExtraGemBonuses() {
        FallenGemsAffixes.LOGGER.info("Applying extra gem bonuses to loaded gems...");

        int appliedCount = 0;
        int failedCount = 0;

        for (Gem gem : GemRegistry.INSTANCE.getValues()) {
            DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(gem);
            ResourceLocation gemId = holder.getId();

            Collection<ExtraGemBonus> bonusesForGem = this.extraBonuses.get(gemId);
            if (bonusesForGem.isEmpty()) {
                continue;
            }

            FallenGemsAffixes.LOGGER.debug("Processing {} extra bonuses for gem {}", bonusesForGem.size(), gemId);

            for (ExtraGemBonus extraBonus : bonusesForGem) {
                for (GemBonus bonus : extraBonus.bonuses()) {
                    try {
                        if (gem instanceof GemBonusExtension extension) {
                            extension.fallen_gems_affixes$appendExtraBonus(bonus);
                            appliedCount++;
                            FallenGemsAffixes.LOGGER.debug("Applied extra gem bonus for class {} to gem {}.",
                                    bonus.getGemClass().key(), gemId);
                        } else {
                            FallenGemsAffixes.LOGGER.warn("Gem {} does not implement GemBonusExtension", gemId);
                            failedCount++;
                        }
                    } catch (Exception ex) {
                        failedCount++;
                        FallenGemsAffixes.LOGGER.warn("Failed to apply extra gem bonus for class {} to gem {}.",
                                bonus.getGemClass().key(), gemId);
                        FallenGemsAffixes.LOGGER.warn("Exception while applying extra gem bonus: ", ex);
                    }
                }
            }
        }

        FallenGemsAffixes.LOGGER.info("Finished applying extra gem bonuses: {} applied, {} failed", appliedCount, failedCount);
    }

    private void clearExtraGemBonuses() {
        FallenGemsAffixes.LOGGER.info("Clearing extra gem bonuses from loaded gems...");

        int clearedCount = 0;
        for (Gem gem : GemRegistry.INSTANCE.getValues()) {
            if (gem instanceof GemBonusExtension extension) {
                extension.fallen_gems_affixes$clearExtraBonuses();
                clearedCount++;
            }
        }

        FallenGemsAffixes.LOGGER.info("Cleared extra bonuses from {} gems", clearedCount);
    }

    public record ExtraGemBonus(ResourceLocation gemId,
                                List<GemBonus> bonuses) implements CodecProvider<ExtraGemBonus> {

        public static final Codec<ExtraGemBonus> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                        ResourceLocation.CODEC.fieldOf("gem").forGetter(ExtraGemBonus::gemId),
                        GemBonus.CODEC.listOf().fieldOf("bonuses").forGetter(ExtraGemBonus::bonuses))
                .apply(inst, ExtraGemBonus::new));

        @Override
        public Codec<? extends ExtraGemBonus> getCodec() {
            return CODEC;
        }

        public static Builder builder(ResourceLocation gemId) {
            return new Builder(gemId);
        }

        public static class Builder {

            protected final ResourceLocation gemId;
            protected List<GemBonus> bonuses = new ArrayList<>();

            public Builder(ResourceLocation gemId) {
                this.gemId = gemId;
            }

            public Builder bonus(GemBonus bonus) {
                this.bonuses.add(bonus);
                return this;
            }

            public Builder bonus(LootCategory cat, GemBonus bonus) {
                this.bonuses.add(bonus);
                return this;
            }

            public Builder bonus(GemClass gClass, GemBonus bonus) {
                this.bonuses.add(bonus);
                return this;
            }

            public ExtraGemBonus build() {
                return new ExtraGemBonus(this.gemId, this.bonuses);
            }
        }
    }
}