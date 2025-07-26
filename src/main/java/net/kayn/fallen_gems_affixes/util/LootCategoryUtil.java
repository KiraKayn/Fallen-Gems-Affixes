package net.kayn.fallen_gems_affixes.util;

import com.google.common.base.Predicates;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.apothic_attributes.compat.CurioEquipmentSlot;
import dev.shadowsoffire.apothic_attributes.modifiers.EntityEquipmentSlot;
import dev.shadowsoffire.apothic_attributes.modifiers.EntitySlotGroup;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;
import java.util.function.Predicate;

import static net.kayn.fallen_gems_affixes.FallenGemsAffixes.R;

public class LootCategoryUtil {

    public static CurioEquipmentSlot registerCurioSlot(String slot) {
        CurioEquipmentSlot curioEquipmentSlot = new CurioEquipmentSlot(slot);
        R.customDH(
            slot,
            ALObjects.BuiltInRegs.ENTITY_EQUIPMENT_SLOT.key(),
            () -> curioEquipmentSlot
        );
        return curioEquipmentSlot;
    }

    public static EntitySlotGroup registerSlotGroup(String path, List<Holder<EntityEquipmentSlot>> slots) {
        EntitySlotGroup entitySlotGroup = new EntitySlotGroup(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, path), HolderSet.direct(slots));
        R.customDH(
                path,
                ALObjects.BuiltInRegs.ENTITY_SLOT_GROUP.key(),
                () -> entitySlotGroup
        );
        return entitySlotGroup;
    }

    public static LootCategory registerLootCategory(String path, EntitySlotGroup group, Predicate<ItemStack> validator, int priority) {
        LootCategory lootCategory = new LootCategory(validator, group, priority);
        R.customDH(
                path,
                Apoth.BuiltInRegs.LOOT_CATEGORY.key(),
                () -> lootCategory
            );
        return lootCategory;
    }

    public static LootCategory registerLootCategoryOrFalse(String path, EntitySlotGroup group, Predicate<ItemStack> validator, int priority, boolean flag) {
        if (validator != null && flag) {
            return registerLootCategory(path, group, validator, priority);
        } else {
            return registerLootCategory(path, group, Predicates.alwaysFalse(), priority);
        }
    }
}
