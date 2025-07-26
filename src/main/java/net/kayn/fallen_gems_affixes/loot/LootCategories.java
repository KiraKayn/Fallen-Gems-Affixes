package net.kayn.fallen_gems_affixes.loot;

import com.google.common.base.Predicates;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
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
    public static final LootCategory STAFFS = LootCategoryUtil.registerLootCategoryOrFalse("staffs",
            ALObjects.EquipmentSlotGroups.MAINHAND,
            Check::staffCheck,
            1200,
            true
    );
    public static void bootstrap() {}

    public static class Check {
        public static boolean staffCheck(ItemStack i) {
            return i.getItem() instanceof CastingItem;
        }
    }
}