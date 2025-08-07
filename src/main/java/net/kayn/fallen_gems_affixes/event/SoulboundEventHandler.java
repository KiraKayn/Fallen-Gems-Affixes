package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class SoulboundEventHandler {

    private static final String TAG_SOULBOUND = "fallen_gems_affixes:soulbound_items";
    private static final ResourceLocation SOULBOUND_ID = new ResourceLocation("fallen_gems_affixes", "soulbound");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        List<ItemStack> soulboundItems = new ArrayList<>();

        event.getDrops().removeIf(itemEntity -> {
            ItemStack stack = itemEntity.getItem();
            var affixes = AffixHelper.getAffixes(stack);
            for (var affixHolder : affixes.keySet()) {
                if (SOULBOUND_ID.equals(affixHolder.getId())) {
                    soulboundItems.add(stack.copy());
                    return true;
                }
            }
            return false;
        });

        if (!soulboundItems.isEmpty()) {
            storeSoulboundItems(player, soulboundItems);
        }
    }
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            CompoundTag originalData = event.getOriginal().getPersistentData();
            CompoundTag newData = event.getEntity().getPersistentData();

            if (originalData.contains(TAG_SOULBOUND)) {
                newData.put(TAG_SOULBOUND, originalData.get(TAG_SOULBOUND));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        List<ItemStack> soulboundItems = getSoulboundItems(player);
        if (!soulboundItems.isEmpty()) {
            Inventory inv = player.getInventory();
            for (int t = soulboundItems.size() - 1, i = 40; t >= 0; t--, i--) {
                ItemStack stack = soulboundItems.get(t);
                if (i >= 36) {
                    for (;i >= 36; i--) {
                        if (LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlotUtil.slotFromInventoryIndex(i)) {
                            if (inv.getItem(i).isEmpty() && !inv.add(i, stack) && !inv.add(stack)) {
                                player.drop(stack, false);
                            }
                            break;
                        }
                    }
                }
                else if (!inv.add(stack)) {
                    player.drop(stack, false);
                }
            }
            clearSoulboundItems(player);
        }
    }

    private static void storeSoulboundItems(Player player, List<ItemStack> items) {
        CompoundTag compound = player.getPersistentData();
        ListTag listTag = new ListTag();

        for (ItemStack stack : items) {
            CompoundTag itemTag = new CompoundTag();
            stack.save(itemTag);
            listTag.add(itemTag);
        }

        compound.put(TAG_SOULBOUND, listTag);
    }

    private static List<ItemStack> getSoulboundItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        CompoundTag compound = player.getPersistentData();

        if (compound.contains(TAG_SOULBOUND)) {
            ListTag listTag = compound.getList(TAG_SOULBOUND, 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                ItemStack stack = ItemStack.of(itemTag);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }

        return items;
    }

    private static void clearSoulboundItems(Player player) {
        player.getPersistentData().remove(TAG_SOULBOUND);
    }
}