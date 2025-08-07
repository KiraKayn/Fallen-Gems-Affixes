package net.kayn.fallen_gems_affixes.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.apothic_attributes.compat.CurioEquipmentSlot;
import dev.shadowsoffire.apothic_attributes.modifiers.EntityEquipmentSlot;
import dev.shadowsoffire.apothic_attributes.modifiers.EntitySlotGroup;
import net.kayn.fallen_gems_affixes.adventure.affix.SpellEffectAffix;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
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
import java.util.stream.Collectors;

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
                default -> null;
            };
        }
        return EquipmentSlotWrappers.NONE;
    }

    public static EquipmentSlotWrapper getOrCreateWrapper(ItemStack itemStack, @Nullable EquipmentSlot slot) {
        EntitySlotGroup slotGroup1 = LootCategory.forItem(itemStack).getSlots();
        HolderSet<EntityEquipmentSlot> holderSet = slotGroup1.slots();
        Set<Holder<EntityEquipmentSlot>> slotSet = holderSet.stream().collect(Collectors.toSet());
        if (CuriosApi.getCurio(itemStack).isPresent()) {
            Map<String, ISlotType> curioSlotMap = CuriosApi.getItemStackSlots(itemStack, false);
            for (String slot0 : curioSlotMap.keySet()) {
                for (Holder<EntityEquipmentSlot> slot1 : slotSet) {
                    if (!(slot1.value() instanceof CurioEquipmentSlot curioSlot)) continue;
                    if (curioSlot.curioType().equals(slot0)) {
                        EquipmentSlotWrapper wrapper = EquipmentSlotWrapper.getAll().get(slot0);
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
            case 39 -> EquipmentSlot.FEET;
            case 38 -> EquipmentSlot.LEGS;
            case 37 -> EquipmentSlot.CHEST;
            case 36 -> EquipmentSlot.HEAD;
            default -> null;
        };
    }

    public static boolean matchesSlot(ItemStack itemStack, EquipmentSlotWrapper slotWrapper) {
        LootCategory category = LootCategory.forItem(itemStack);
        if (category.isNone()) return false;
        return category.getSlots().test(slotWrapper.extractHolder());
    }

    public static boolean simpleMatchesSlot(ItemStack itemStack, EquipmentSlot slot) {
        EquipmentSlotWrapper wrapper = getVanillaWrapper(slot);
        Holder<EntityEquipmentSlot> eSlot = wrapper.extractHolder();
        return LootCategory.forItem(itemStack).getSlots().test(eSlot);
    }

    public static boolean simpleMatchesSlot(ItemStack itemStack, EquipmentSlotWrapper slotWrapper) {
        Holder<EntityEquipmentSlot> eSlot = slotWrapper.extractHolder();
        return LootCategory.forItem(itemStack).getSlots().test(eSlot);
    }

    public static boolean simpleMatchesCurioSlot(LivingEntity entity, ItemStack itemStack, String slot) {
        EquipmentSlotWrapper wrapper = getCurioWrapper(entity, itemStack, slot);
        if (wrapper == EquipmentSlotWrappers.NONE) return false;
        Holder<EntityEquipmentSlot> eSlot = wrapper.extractHolder();
        return LootCategory.forItem(itemStack).getSlots().test(eSlot);
    }

    public static EquipmentSlotWrapper getVanillaWrapper(ItemStack itemStack) {
        EquipmentSlot slot = itemStack.getEquipmentSlot();
        return getVanillaWrapper(slot);
    }

    @Nullable
    public static EquipmentSlotWrapper getCurioWrapper(String identifier) {
        return EquipmentSlotWrapper.getAll().get(identifier);
    }

    public static EquipmentSlotWrapper getCurioWrapper(LivingEntity entity, ItemStack stack, String identifier) {
        EquipmentSlotWrapper wrapper = EquipmentSlotWrapper.getAll().get(identifier);
        if (wrapper == null) {
            wrapper = getCurioWrapper(stack, entity);
        }
        return wrapper;
    }

    public static EquipmentSlotWrapper getCurioWrapper(ItemStack itemStack, LivingEntity entity) {
        EntitySlotGroup slotGroup1 = LootCategory.forItem(itemStack).getSlots();
        HolderSet<EntityEquipmentSlot> holderSet = slotGroup1.slots();
        Iterator<Holder<EntityEquipmentSlot>> iterator = holderSet.iterator();
        while(iterator.hasNext()) {
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
                    if (wrapper != null) return wrapper;
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

    public static Iterable<ItemStack> getAllSlots(LivingEntity entity) {
        NonNullList<ItemStack> allSlots = NonNullList.create();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler != null) {
            for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                ICurioStacksHandler slotHandler = entry.getValue();
                try {
                    for (int i = 0; i < slotHandler.getSlots(); i++) {
                        ItemStack equippedStack = slotHandler.getStacks().getStackInSlot(i);
                        allSlots.add(equippedStack);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return Iterables.concat(allSlots, entity.getAllSlots());
    }
}
