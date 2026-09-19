package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;

public class SoulboundEventHandler {

    private static final String TAG_SOULBOUND = "fallen_gems_affixes:soulbound_items";
    private static final String TAG_EQUIPPED_ITEMS = "fallen_gems_affixes:equipped_items";
    private static final ResourceLocation SOULBOUND_ID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "soulbound");

    private static final Map<UUID, List<ItemStack>> tempEquippedItems = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        List<ItemStack> equippedSoulbound = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.MAINHAND) continue;

            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && hasSoulboundAffix(stack)) {
                equippedSoulbound.add(stack.copy());
            }
        }

        tempEquippedItems.put(player.getUUID(), equippedSoulbound);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        List<ItemStack> soulboundItems = new ArrayList<>();
        List<ItemStack> equippedItems = tempEquippedItems.getOrDefault(player.getUUID(), new ArrayList<>());

        event.getDrops().removeIf(itemEntity -> {
            ItemStack stack = itemEntity.getItem();
            if (hasSoulboundAffix(stack)) {
                soulboundItems.add(stack.copy());
                return true;
            }
            return false;
        });

        if (!soulboundItems.isEmpty()) {
            storeSoulboundItems(player, soulboundItems, equippedItems);
        }

        tempEquippedItems.remove(player.getUUID());
    }

    public static boolean hasSoulboundAffix(ItemStack stack) {
        var affixes = AffixHelper.getAffixes(stack);
        for (var affixHolder : affixes.keySet()) {
            if (SOULBOUND_ID.equals(affixHolder.getId())) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            CompoundTag originalData = event.getOriginal().getPersistentData();
            CompoundTag newData = event.getEntity().getPersistentData();

            if (originalData.contains(TAG_SOULBOUND)) {
                newData.put(TAG_SOULBOUND, originalData.get(TAG_SOULBOUND));
            }
            if (originalData.contains(TAG_EQUIPPED_ITEMS)) {
                newData.put(TAG_EQUIPPED_ITEMS, originalData.get(TAG_EQUIPPED_ITEMS));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        List<ItemStack> soulboundItems = getSoulboundItems(player);
        List<ItemStack> equippedItems = getEquippedItems(player);

        if (!soulboundItems.isEmpty()) {
            Inventory inv = player.getInventory();

            if (inv.getContainerSize() < 41 || !(player instanceof ServerPlayer)) {
                for (ItemStack stack01 : soulboundItems) {
                    if (!inv.add(stack01)) {
                        player.drop(stack01, false);
                    }
                }
                clearSoulboundItems(player);
                return;
            }

            for (int t = soulboundItems.size() - 1; t >= 0; t--) {
                ItemStack stack = soulboundItems.get(t);
                boolean wasEquipped = false;

                for (int i = 0; i < equippedItems.size(); i++) {
                    ItemStack equippedItem = equippedItems.get(i);
                    if (ItemStack.isSameItemSameComponents(stack, equippedItem)) {
                        wasEquipped = true;
                        equippedItems.remove(i);
                        break;
                    }
                }

                boolean placed = false;

                if (wasEquipped) {
                    EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(stack);
                    if (equipmentSlot != EquipmentSlot.MAINHAND) {
                        int slotIndex = getInventorySlotForEquipmentSlot(equipmentSlot);
                        if (slotIndex != -1 && inv.getItem(slotIndex).isEmpty()) {
                            inv.setItem(slotIndex, stack);
                            placed = true;
                        }
                    }
                }

                if (!placed) {
                    if (!inv.add(stack)) {
                        player.drop(stack, false);
                    }
                }
            }

            clearSoulboundItems(player);
        }
    }

    private static int getInventorySlotForEquipmentSlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case HEAD -> 39;
            case CHEST -> 38;
            case LEGS -> 37;
            case FEET -> 36;
            case OFFHAND -> 40;
            case MAINHAND -> -1;
            default -> -1;
        };
    }

    private static void storeSoulboundItems(Player player, List<ItemStack> items, List<ItemStack> equippedItems) {
        CompoundTag compound = player.getPersistentData();

        ListTag itemListTag = new ListTag();
        for (ItemStack stack : items) {
            CompoundTag itemTag = new CompoundTag();
            stack.save(player.level().registryAccess(), itemTag);
            itemListTag.add(itemTag);
        }
        compound.put(TAG_SOULBOUND, itemListTag);

        ListTag equippedListTag = new ListTag();
        for (ItemStack stack : equippedItems) {
            CompoundTag itemTag = new CompoundTag();
            stack.save(player.level().registryAccess(), itemTag);
            equippedListTag.add(itemTag);
        }
        compound.put(TAG_EQUIPPED_ITEMS, equippedListTag);
    }

    private static List<ItemStack> getSoulboundItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        CompoundTag compound = player.getPersistentData();

        if (compound.contains(TAG_SOULBOUND)) {
            ListTag listTag = compound.getList(TAG_SOULBOUND, 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                ItemStack stack = ItemStack.parseOptional(player.level().registryAccess(), itemTag);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }

        return items;
    }

    private static List<ItemStack> getEquippedItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        CompoundTag compound = player.getPersistentData();

        if (compound.contains(TAG_EQUIPPED_ITEMS)) {
            ListTag listTag = compound.getList(TAG_EQUIPPED_ITEMS, 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                ItemStack stack = ItemStack.parseOptional(player.level().registryAccess(), itemTag);
                if (!stack.isEmpty()) {
                    items.add(stack);
                }
            }
        }

        return items;
    }

    private static void clearSoulboundItems(Player player) {
        player.getPersistentData().remove(TAG_SOULBOUND);
        player.getPersistentData().remove(TAG_EQUIPPED_ITEMS);
    }
}