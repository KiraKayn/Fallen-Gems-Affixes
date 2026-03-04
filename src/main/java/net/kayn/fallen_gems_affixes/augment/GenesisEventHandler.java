package net.kayn.fallen_gems_affixes.augment;

import net.kayn.fallen_gems_affixes.Fallen;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side event handler Genesis Augment
 *
 * <p>When a {@link ServerPlayer} kills an entity matching the
 * {@code fallen_gems_affixes:boss_slayer} tag, every Genesis-augmented item in their
 * full inventory is updated — no guards on whether affixes or gems are present.
 * Both powers always grow; {@link GenesisAugment#applyAffixPower} handles the case
 * where the item has no affixes gracefully (it's a no-op).
 */
public class GenesisEventHandler {

    private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(
            Registries.ENTITY_TYPE,
            new ResourceLocation("fallen_gems_affixes", "boss_slayer"));

    public static void bootstrap(IEventBus eventBus) {
        eventBus.addListener(GenesisEventHandler::onLivingDeath);
    }

    private static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dying = event.getEntity();
        if (!dying.getType().is(BOSS_TAG)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        // Track by entity TYPE, so same boss only counts once
        ResourceLocation bossType = EntityType.getKey(dying.getType());
        if (bossType == null) return;

        for (ItemStack stack : getAllItems(player)) {
            if (!stack.isEmpty()) updateGenesisOnBossKill(stack, bossType);
        }
    }


    private static void updateGenesisOnBossKill(ItemStack stack, ResourceLocation bossType) {
        CompoundTag itemTag = stack.getTag();
        if (itemTag == null || !itemTag.contains(Fallen.AugmentMisc.AUGMENT_DATA)) return;

        CompoundTag augmentData = itemTag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
        ListTag augments = augmentData.getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);

        for (int i = 0; i < augments.size(); i++) {
            CompoundTag entry = augments.getCompound(i);
            ResourceLocation typeId = ResourceLocation.tryParse(entry.getString(Fallen.AugmentMisc.TYPE));
            if (!GenesisAugment.augmentId().equals(typeId)) continue;

            CompoundTag inner = entry.getCompound(Fallen.AugmentMisc.INNER_DATA);

            // Deduplicate by entity type - the same boss can only be coutned once
            String bossStr = bossType.toString();
            ListTag killedList = inner.getList("killedBosses", Tag.TAG_STRING);
            for (Tag t : killedList) {
                if (bossStr.equals(t.getAsString())) return;
            }

            // Record the kill
            killedList.add(StringTag.valueOf(bossStr));
            inner.put("killedBosses", killedList);
            inner.putInt("bossKillCount", inner.getInt("bossKillCount") + 1);

            // Always boost both powers — no hasAffixes / hasGems checks
            float newAffix = inner.getFloat("affixPower") + inner.getFloat("affixPowerBoost");
            float newGem   = inner.getFloat("gemPower")   + inner.getFloat("gemPowerBoost");
            inner.putFloat("affixPower", newAffix);
            inner.putFloat("gemPower",   newGem);

            // Re-apply affix levels immediately (no-ops if item has no affixes yet)
            GenesisAugment.applyAffixPower(stack, newAffix);

            // Gem power is picked up passively by GemBonusModifier on the next calculation
            entry.put(Fallen.AugmentMisc.INNER_DATA, inner);
            augmentData.put(Fallen.AugmentMisc.AUGMENTS, augments);
            itemTag.put(Fallen.AugmentMisc.AUGMENT_DATA, augmentData);
            break;
        }
    }

    private static List<ItemStack> getAllItems(ServerPlayer player) {
        List<ItemStack> items = new ArrayList<>();
        items.addAll(player.getInventory().items);
        items.addAll(player.getInventory().armor);
        items.addAll(player.getInventory().offhand);
        return items;
    }
}