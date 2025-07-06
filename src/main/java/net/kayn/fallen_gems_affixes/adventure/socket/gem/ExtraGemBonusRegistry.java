package net.kayn.fallen_gems_affixes.adventure.socket.gem;

import java.util.*;
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
        resourceManager.listResources("extra_gem_bonuses", location -> location.getPath().endsWith(".json")).forEach((location, resource) -> {
            try {
                JsonElement jsonElement = GSON.fromJson(resource.openAsReader(), JsonElement.class);
                String path = location.getPath();
                String filename = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
                ResourceLocation key = new ResourceLocation(location.getNamespace(), filename);
                map.put(key, jsonElement);
            } catch (Exception ignored) {}
        });
        return map;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> prepared, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        this.pendingData = prepared;
        this.hasAppliedBonuses = false;
        this.extraBonuses.clear();
        this.clearExtraGemBonuses();
        if (!tryApplyBonuses()) {
            scheduleDelayedApplication();
        }
    }

    private boolean tryApplyBonuses() {
        if (GemRegistry.INSTANCE.getValues().isEmpty()) return false;
        return parseAndApplyBonuses();
    }

    private boolean parseAndApplyBonuses() {
        if (this.pendingData.isEmpty()) return false;

        Map<ResourceLocation, ExtraGemBonus> parsedBonuses = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : this.pendingData.entrySet()) {
            ResourceLocation key = entry.getKey();
            JsonElement jsonElement = entry.getValue();
            var result = ExtraGemBonus.CODEC.parse(JsonOps.INSTANCE, jsonElement);
            result.result().ifPresent(bonus -> parsedBonuses.put(key, bonus));
        }

        if (parsedBonuses.isEmpty()) return false;

        for (ExtraGemBonus bonus : parsedBonuses.values()) {
            this.extraBonuses.put(bonus.gemId(), bonus);
        }

        this.applyExtraGemBonuses();
        this.hasAppliedBonuses = true;
        return true;
    }

    private void scheduleDelayedApplication() {
        CompletableFuture.runAsync(() -> {
            int maxRetries = 10;
            int retryDelay = 1000;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    Thread.sleep(retryDelay);
                    if (tryApplyBonuses()) return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
    }

    public void forceApplyBonuses() {
        if (!this.hasAppliedBonuses && !this.pendingData.isEmpty()) {
            tryApplyBonuses();
        }
    }

    public static Collection<ExtraGemBonus> getBonusesFor(ResourceLocation gemId) {
        return INSTANCE.extraBonuses.get(gemId);
    }

    public void applyExtraGemBonuses() {
        for (Gem gem : GemRegistry.INSTANCE.getValues()) {
            DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(gem);
            ResourceLocation gemId = holder.getId();
            Collection<ExtraGemBonus> bonusesForGem = this.extraBonuses.get(gemId);
            if (bonusesForGem.isEmpty()) continue;
            for (ExtraGemBonus extraBonus : bonusesForGem) {
                for (GemBonus bonus : extraBonus.bonuses()) {
                    try {
                        if (gem instanceof GemBonusExtension extension) {
                            extension.fallen_gems_affixes$appendExtraBonus(bonus);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private void clearExtraGemBonuses() {
        for (Gem gem : GemRegistry.INSTANCE.getValues()) {
            if (gem instanceof GemBonusExtension extension) {
                extension.fallen_gems_affixes$clearExtraBonuses();
            }
        }
    }

    public record ExtraGemBonus(ResourceLocation gemId, List<GemBonus> bonuses) implements CodecProvider<ExtraGemBonus> {
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