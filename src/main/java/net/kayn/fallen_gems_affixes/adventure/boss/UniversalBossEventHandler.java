package net.kayn.fallen_gems_affixes.adventure.boss;

import dev.shadowsoffire.apotheosis.adventure.boss.BossStats;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class UniversalBossEventHandler {

    private static final String TAG = "fga.universal_boss";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Monster mob)) return;

        CompoundTag data = mob.getPersistentData();
        if (data.contains(TAG)) return;
        if (data.contains("apoth.boss") || data.contains("apoth.miniboss")) return;

        UniversalBossConfig config = UniversalBossLoader.getConfig();
        if (config == null) return;

        var entityId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (entityId != null && config.blacklist().contains(entityId)) return;

        LootRarity rarity = config.rollRarity(mob.getRandom());
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

        Component name = mob.hasCustomName()
                ? mob.getCustomName().copy().withStyle(Style.EMPTY.withColor(rarity.getColor()))
                : mob.getName().copy().withStyle(Style.EMPTY.withColor(rarity.getColor()));
        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
    }
}