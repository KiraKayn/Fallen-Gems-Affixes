package net.kayn.fallen_gems_affixes.adventure.entity.affix;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class EntityAffixLoader extends SimpleJsonResourceReloadListener {

    public static final EntityAffixLoader INSTANCE = new EntityAffixLoader();

    public EntityAffixLoader() {
        super(new Gson(), "entity_affixes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager manager, ProfilerFiller profiler) {
        EntityAffixRegistry.clearInstances();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation id = entry.getKey();
            EntityAffix affix = EntityAffixRegistry.DISPATCH_CODEC
                    .parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(err -> FallenGemsAffixes.LOGGER.error("[FGA] Failed to load entity affix '{}': {}", id, err))
                    .orElse(null);
            if (affix != null) EntityAffixRegistry.registerInstance(id, affix);
        }

        FallenGemsAffixes.LOGGER.info("[FGA] Loaded {} entity affixes.", EntityAffixRegistry.getInstances().size());
    }
}