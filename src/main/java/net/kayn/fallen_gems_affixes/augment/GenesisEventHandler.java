package net.kayn.fallen_gems_affixes.augment;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentHelper;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
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
            ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "boss_slayer"));

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
        AugmentInstance inst = AugmentHelper.getAugments(stack).get(Fallen.Augments.GENESIS);
        if (inst == null) return;
        GenesisAugment.GenesisData data = (GenesisAugment.GenesisData) inst.getData();
        data.bossKillCount += 1;
        data.killedBossIds.add(bossType.toString());
        AugmentHelper.applyAugment(stack, new AugmentInstance(inst.getAugment(), data));
        // Deduplicate by entity type - the same boss can only be coutned once
        // Re-apply affix levels immediately (no-ops if item has no affixes yet)
        GenesisAugment.applyAffixPower(stack, data.affixPower + data.affixPowerBoost);
    }

    private static List<ItemStack> getAllItems(ServerPlayer player) {
        List<ItemStack> items = new ArrayList<>();
        items.addAll(player.getInventory().items);
        items.addAll(player.getInventory().armor);
        items.addAll(player.getInventory().offhand);
        return items;
    }
}