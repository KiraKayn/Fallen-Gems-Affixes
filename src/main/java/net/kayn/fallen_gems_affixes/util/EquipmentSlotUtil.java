package net.kayn.fallen_gems_affixes.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apothic_attributes.compat.CurioEquipmentSlot;
import dev.shadowsoffire.apothic_attributes.modifiers.EntityEquipmentSlot;
import dev.shadowsoffire.apothic_attributes.modifiers.EntitySlotGroup;
import net.kayn.fallen_gems_affixes.types.util.Indexed;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class EquipmentSlotUtil {
    public static EquipmentSlotWrapper getVanillaWrapper(EquipmentSlot slot) {
        if (slot != null) {
            return switch (slot) {
                case HEAD -> EquipmentSlotWrappers.HEAD;
                case CHEST -> EquipmentSlotWrappers.CHEST;
                case LEGS -> EquipmentSlotWrappers.LEGS;
                case FEET -> EquipmentSlotWrappers.FEET;
                case OFFHAND -> EquipmentSlotWrappers.OFF_HAND;
                case MAINHAND -> EquipmentSlotWrappers.MAIN_HAND;
                default -> EquipmentSlotWrappers.NONE;
            };
        }
        return EquipmentSlotWrappers.NONE;
    }

    public static EquipmentSlotWrapper getWrapperByIdentifier(String identifier) {
        EquipmentSlotWrapper wrapper = EquipmentSlotWrapper.getAll().get(identifier);
        if (wrapper == null) return EquipmentSlotWrappers.NONE;
        return wrapper;
    }

    public static EquipmentSlotWrapper getWrapper(LivingEntity entity, ItemStack stack, String identifier) {
        EquipmentSlotWrapper wrapper = getWrapperByIdentifier(identifier);
        if (entity != null && !stack.isEmpty() && CuriosApi.getCurio(stack).isPresent()) {
            return getCurioWrapper(entity, stack, identifier);
        }
        return wrapper;
    }

    @Deprecated
    public static EquipmentSlotWrapper getOrCreateWrapper(ItemStack itemStack, @Nullable EquipmentSlot slot) {
        EntitySlotGroup slotGroup1 = LootCategory.forItem(itemStack).getSlots();
        HolderSet<EntityEquipmentSlot> holderSet = slotGroup1.slots();
        if (CuriosApi.getCurio(itemStack).isPresent()) {
            Map<String, ISlotType> curioSlotMap = CuriosApi.getItemStackSlots(itemStack, false);
            for (String slot0 : curioSlotMap.keySet()) {
                for (Holder<EntityEquipmentSlot> slot1 : holderSet) {
                    if (!(slot1.value() instanceof CurioEquipmentSlot curioSlot)) continue;
                    if (curioSlot.curioType().equals(slot0)) {
                        EquipmentSlotWrapper wrapper = getWrapperByIdentifier(slot0);
                        if (wrapper != null) return wrapper;
                        return new EquipmentSlotWrapper(null, slot0, slot1);
                    }
                }
            }
        }
        return getVanillaWrapper(slot);
    }

    public static EquipmentSlot slotFromAllSlotsIndex(int index) {
        return switch (index) {
            case 0 -> EquipmentSlot.MAINHAND;
            case 1 -> EquipmentSlot.OFFHAND;
            case 2 -> EquipmentSlot.FEET;
            case 3 -> EquipmentSlot.LEGS;
            case 4 -> EquipmentSlot.CHEST;
            case 5 -> EquipmentSlot.HEAD;
            default -> null;
        };
    }

    public static EquipmentSlot slotFromInventoryIndex(int index) {
        return switch (index) {
            case 40 -> EquipmentSlot.OFFHAND;
            case 39 -> EquipmentSlot.HEAD;
            case 38 -> EquipmentSlot.CHEST;
            case 37 -> EquipmentSlot.LEGS;
            case 36 -> EquipmentSlot.FEET;
            default -> null;
        };
    }

    public static boolean matchesSlot(ItemStack itemStack, EquipmentSlotWrapper slotWrapper) {
        LootCategory category = LootCategory.forItem(itemStack);
        if (slotWrapper.isEmpty()) return false;
        return category.getSlots().test(slotWrapper.extractApothHolder());
    }

    public static boolean simpleMatchesSlot(ItemStack itemStack, EquipmentSlot slot) {
        EquipmentSlotWrapper wrapper = getVanillaWrapper(slot);
        if (wrapper.isEmpty()) return false;
        Holder<EntityEquipmentSlot> eSlot = wrapper.extractApothHolder();
        return LootCategory.forItem(itemStack).getSlots().test(eSlot);
    }

    public static boolean simpleMatchesSlot(ItemStack itemStack, EquipmentSlotWrapper slotWrapper) {
        if (slotWrapper.isEmpty()) return false;
        Holder<EntityEquipmentSlot> eSlot = slotWrapper.extractApothHolder();
        return LootCategory.forItem(itemStack).getSlots().test(eSlot);
    }

    public static boolean simpleMatchesCurioSlot(LivingEntity entity, ItemStack itemStack, String slot) {
        EquipmentSlotWrapper wrapper = getCurioWrapper(entity, itemStack, slot);
        if (wrapper.isEmpty()) return false;
        Holder<EntityEquipmentSlot> eSlot = wrapper.extractApothHolder();
        return LootCategory.forItem(itemStack).getSlots().test(eSlot);
    }

    public static EquipmentSlotWrapper getVanillaWrapper(ItemStack itemStack) {
        EquipmentSlot slot = itemStack.getEquipmentSlot();
        return getVanillaWrapper(slot);
    }

    public static EquipmentSlotWrapper getCurioWrapper(@NotNull LivingEntity entity, ItemStack stack, String identifier) {
        EquipmentSlotWrapper wrapper = getWrapperByIdentifier(identifier);
        if (wrapper == null) {
            wrapper = getCurioWrapper(stack, entity);
        }
        return wrapper;
    }

    public static boolean curioSlotMatches(ItemStack itemStack, LivingEntity entity, String slot) {
        LootCategory cat = LootCategory.forItem(itemStack);
        if (cat.isNone()) return false;
        EquipmentSlotWrapper wrapper = EquipmentSlotUtil.getCurioWrapper(entity, itemStack, slot);
        if (!wrapper.isEmpty()) {
            return cat.getSlots().test(wrapper.extractApothHolder());
        }
        return false;
    }

    public static boolean vanillaSlotMatches(ItemStack itemStack, EquipmentSlot slot) {
        LootCategory cat = LootCategory.forItem(itemStack);
        if (cat.isNone()) return false;
        EquipmentSlotWrapper wrapper = EquipmentSlotUtil.getVanillaWrapper(slot);
        if (!wrapper.isEmpty()) {
            return cat.getSlots().test(wrapper.extractApothHolder());
        }
        return false;
    }

    public static EquipmentSlotWrapper getCurioWrapper(ItemStack itemStack, @NotNull LivingEntity entity) {
        LootCategory cat = LootCategory.forItem(itemStack);
        if (cat.isNone()) return EquipmentSlotWrappers.NONE;
        EntitySlotGroup slotGroup1 = cat.getSlots();
        HolderSet<EntityEquipmentSlot> holderSet = slotGroup1.slots();
        Iterator<Holder<EntityEquipmentSlot>> iterator = holderSet.iterator();
        while (iterator.hasNext()) {
            Holder<EntityEquipmentSlot> varSlot = iterator.next();
            if (varSlot.value() instanceof CurioEquipmentSlot slot) {
                boolean flag = false;
                for (ItemStack stack : slot.getStacks(entity)) {
                    if (ItemStack.isSameItemSameComponents(stack, itemStack)) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    EquipmentSlotWrapper wrapper = EquipmentSlotWrapper.byESlot(varSlot);
                    if (!wrapper.isEmpty()) return wrapper;
                    else {
                        return new EquipmentSlotWrapper(null, slot.curioType(), varSlot);
                    }
                }
            }
        }
        return EquipmentSlotWrappers.NONE;
    }

    @Nullable
    public static String getEquippedCurioIdentifier(LivingEntity entity, ItemStack stack) {
        AtomicReference<String> slotId = new AtomicReference<>();
        CuriosApi.getCuriosInventory(entity).ifPresent(handler -> {
            for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                ICurioStacksHandler slotHandler = entry.getValue();

                for (int i = 0; i < slotHandler.getSlots(); i++) {
                    ItemStack equippedStack = slotHandler.getStacks().getStackInSlot(i);
                    if (ItemStack.isSameItemSameComponents(stack, equippedStack)) {
                        slotId.set(entry.getKey());
                        break;
                    }
                }
                if (slotId.get() != null) break;
            }
        });
        return slotId.get();
    }

    public static Iterable<ItemStack> getOffHandAndArmors(Player player) {
        return Iterables.concat(Lists.newArrayList(player.getOffhandItem()), player.getArmorSlots());
    }

    public static Map<Indexed<String>, ItemStack> getAllSlotsMap(LivingEntity entity) {
        Map<Indexed<String>, ItemStack> allSlotsMap = new LinkedHashMap<>();
        for (ItemStack stack : entity.getAllSlots()) {
            EquipmentSlot slot = stack.getEquipmentSlot();
            if (slot == null) continue;
            allSlotsMap.put(Indexed.simple(0, slot.getName()), stack);
        }
        return allSlotsMap;
    }

    public static Map<Indexed<String>, ItemStack> getAllSlotsMapWithCurio(LivingEntity entity) {
        Map<Indexed<String>, ItemStack> result = getAllSlotsMap(entity);
        result.putAll(getCurioAllSlots(entity));
        return result;
    }

    public static Map<Indexed<String>, ItemStack> getCurioAllSlots(LivingEntity entity) {
        Map<Indexed<String>, ItemStack> allSlotsMap = new LinkedHashMap<>();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler != null) {
            for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                ICurioStacksHandler slotHandler = entry.getValue();
                try {
                    for (int i = 0; i < slotHandler.getSlots(); i++) {
                        ItemStack equippedStack = slotHandler.getStacks().getStackInSlot(i);
                        if (equippedStack.isEmpty()) continue;
                        allSlotsMap.put(Indexed.simple(i, entry.getKey()), equippedStack);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return allSlotsMap;
    }
}
