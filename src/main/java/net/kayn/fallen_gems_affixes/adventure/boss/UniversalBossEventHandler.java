package net.kayn.fallen_gems_affixes.adventure.boss;

import dev.shadowsoffire.apotheosis.adventure.boss.BossStats;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class UniversalBossEventHandler {

    private static final String TAG = "fga.universal_boss";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoin(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Monster mob)) return;

        CompoundTag data = mob.getPersistentData();
        if (data.contains(TAG)) return;
        if (data.contains("apoth.boss") || data.contains("apoth.miniboss")) return;

        UniversalBossConfig config = UniversalBossLoader.getConfig();
        if (config == null) return;

        var entityId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (config.isBlacklisted(mob.getType(), entityId)) return;

        ResourceLocation dimensionId = event.getLevel().getLevel().dimension().location();

        List<LootRarity> allowed = config.getRaritiesForDimension(dimensionId);
        if (allowed != null && allowed.isEmpty()) return;

        Set<LootRarity> allowedSet = allowed != null ? new java.util.HashSet<>(allowed) : null;
        LootRarity rarity = config.rollRarity(mob.getRandom(), allowedSet);
        if (rarity == null) return;

        BossStats stats = config.stats().get(rarity);
        if (stats == null) {
            FallenGemsAffixes.LOGGER.warn("No stats defined for rarity {} in universal boss config", config.getRarityKey(rarity));
            return;
        }

        apply(mob, stats, rarity);

        data.putBoolean(TAG, true);
        data.putString(TAG + ".rarity", config.getRarityKey(rarity));
        data.putBoolean("apoth.boss", true);
        data.putString("apoth.rarity", config.getRarityKey(rarity));
    }

    private static void apply(Mob mob, BossStats stats, LootRarity rarity) {
        int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;

        for (ChancedEffectInstance inst : stats.effects()) {
            if (mob.getRandom().nextFloat() <= inst.chance()) {
                mob.addEffect(inst.create(mob.getRandom(), duration));
            }
        }

        for (RandomAttributeModifier modif : stats.modifiers()) {
            modif.apply(mob.getRandom(), mob);
        }

        mob.setHealth(mob.getMaxHealth());

        String rarityPath = RarityRegistry.INSTANCE.getKey(rarity) != null
                ? Objects.requireNonNull(RarityRegistry.INSTANCE.getKey(rarity)).getPath() : "unknown";
        String rarityTitle = rarityPath.substring(0, 1).toUpperCase() + rarityPath.substring(1);
        Component mobName = mob.hasCustomName() ? mob.getCustomName() : mob.getName();
        assert mobName != null;
        MutableComponent name;

        if (net.kayn.fallen_gems_affixes.config.ModConfig.SHOW_BOSS_RARITY_NAME.get()) {
            name = Component.literal(rarityTitle + " ")
                    .withStyle(Style.EMPTY.withColor(rarity.getColor()))
                    .append(mobName.copy().withStyle(Style.EMPTY.withColor(rarity.getColor())));
        } else {
            name = mobName.copy().withStyle(Style.EMPTY.withColor(rarity.getColor()));
        }
        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
    }
}