package net.kayn.fallen_gems_affixes.loot;

import com.google.common.base.Predicates;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import io.redspace.ironsspellbooks.item.CastingItem;
import net.kayn.fallen_gems_affixes.util.LootCategoryUtil;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class LootCategories {
    // we can't define predicate in this loot category, because it's sub-category of melee weapon, we don't want to overlap it.
    public static final LootCategory HEAVY_WEAPON = LootCategoryUtil.registerLootCategoryOrFalse("heavy_weapon",
            ALObjects.EquipmentSlotGroups.MAINHAND,
            Predicates.alwaysFalse(),
            1,
            false
    );

    public static final LootCategory STAFFS = LootCategoryUtil.registerLootCategoryOrFalse("staffs",
            ALObjects.EquipmentSlotGroups.MAINHAND,
            Predicates.alwaysFalse(),
            1200,
            false
    );

//    public static final LootCategory LIGHT_WEAPON = LootCategoryUtil.registerLootCategoryOrFalse("light_weapon",
//            ALObjects.EquipmentSlotGroups.MAINHAND,
//            Predicates.alwaysFalse(),
//            1,
//            false
//    );

    public static void bootstrap() {}

    public static class Check {
        public static boolean heavyWeaponCheck(ItemStack i) {
            Item ii = i.getItem();
            if (ii instanceof AxeItem || ii instanceof MaceItem) return true;
            ItemAttributeModifiers modifiers = i.getAttributeModifiers();
            boolean flag1 = false;
            boolean flag2 = false;
            for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                Holder<Attribute> attribute = entry.attribute();
                if (attribute == Attributes.ATTACK_SPEED && entry.modifier().amount() <= 1.2) {
                    flag1 = true;
                    continue;
                }
                if (attribute == Attributes.ATTACK_DAMAGE && entry.modifier().amount() >= 9) {
                    flag2 = true;
                }
            }
            if (flag1 && flag2) return true;
            return false;
        }

        public static boolean staffCheck(ItemStack i) {
            return i.getItem() instanceof CastingItem;
        }

//        public static boolean lightWeaponCheck(ItemStack i) {
//            if (i.canPerformAction(ItemAbilities.SWORD_DIG)
//                    || i.getAttributeModifiers().modifiers().stream().anyMatch(e->e.attribute() == Attributes.ATTACK_DAMAGE && e.modifier().amount() > 0
//                    || i.getItem() instanceof TridentItem)) return true;
//            return false;
//        }
    }
}