package net.kayn.fallen_gems_affixes.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.apothic_attributes.compat.CurioEquipmentSlot;
import dev.shadowsoffire.apothic_attributes.modifiers.EntityEquipmentSlot;
import dev.shadowsoffire.apothic_attributes.modifiers.EntitySlotGroup;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosTags;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.swing.text.html.Option;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class EquipmentSlotUtil {
    public static EquipmentSlotWrapper getVanillaWrapper(@NotNull EquipmentSlot slot) {
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
        switch (slot) {
            case EquipmentSlot.MAINHAND -> {
                return EquipmentSlotWrappers.MAIN_HAND;
            }
            case EquipmentSlot.OFFHAND -> {
                return EquipmentSlotWrappers.OFF_HAND;
            }
            case EquipmentSlot.FEET -> {
                return EquipmentSlotWrappers.FEET;
            }
            case EquipmentSlot.CHEST -> {
                return EquipmentSlotWrappers.CHEST;
            }
            case EquipmentSlot.HEAD -> {
                return EquipmentSlotWrappers.HEAD;
            }
            default -> {return EquipmentSlotWrappers.NONE;}
        }
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

    public static boolean matchesSlot(ItemStack itemStack, @Nullable EquipmentSlot slot) {
        EntitySlotGroup slotGroup1 = LootCategory.forItem(itemStack).getSlots();
        if (CuriosApi.getCurio(itemStack).isPresent()) {
            HolderSet<EntityEquipmentSlot> holderSet = slotGroup1.slots();
            Set<EntityEquipmentSlot> slotSet = holderSet.stream().map(Holder::value).collect(Collectors.toSet());
            Map<String, ISlotType> curioSlotMap = CuriosApi.getItemStackSlots(itemStack, false);
            for (String slot0 : curioSlotMap.keySet()) {
                for (EntityEquipmentSlot slot1 : slotSet) {
                    if (!(slot1 instanceof CurioEquipmentSlot curioSlot)) continue;
                    if (curioSlot.curioType().equals(slot0)) return true;
                }
            }
        }
        Holder<EntityEquipmentSlot> slot2;
        switch (slot) {
            case EquipmentSlot.MAINHAND -> slot2 = ALObjects.EquipmentSlots.MAINHAND;
            case EquipmentSlot.OFFHAND -> slot2 = ALObjects.EquipmentSlots.OFFHAND;
            case EquipmentSlot.FEET -> slot2 = ALObjects.EquipmentSlots.FEET;
            case EquipmentSlot.CHEST -> slot2 = ALObjects.EquipmentSlots.CHEST;
            case EquipmentSlot.HEAD -> slot2 = ALObjects.EquipmentSlots.HEAD;
            default -> throw new MatchException((String)null, (Throwable)null);
        };
        return slotGroup1.test(slot2);
    }

    public static boolean simpleMatchesSlot(ItemStack itemStack, EquipmentSlot slot) {
        EquipmentSlotWrapper wrapper = getVanillaWrapper(slot);
        Holder<EntityEquipmentSlot> eSlot = wrapper.extractHolder();
        return LootCategory.forItem(itemStack).getSlots().test(eSlot);
    }

    public static Set<EquipmentSlotWrapper> collectWrapper(ItemStack itemStack) {
        EntitySlotGroup slotGroup1 = LootCategory.forItem(itemStack).getSlots();
        HolderSet<EntityEquipmentSlot> holderSet = slotGroup1.slots();
        return holderSet.stream()
                .map(a -> {
                    EquipmentSlotWrapper wrapper = EquipmentSlotWrapper.byESlot(a);
                    if (wrapper != null) return wrapper;
                    return new EquipmentSlotWrapper(null, a.getRegisteredName(), a);
                }).collect(Collectors.toSet());
    }

    public static Iterable<ItemStack> getOffHandAndArmors(Player player){
        return Iterables.concat(Lists.newArrayList(player.getOffhandItem()), player.getArmorSlots());
    }
}
