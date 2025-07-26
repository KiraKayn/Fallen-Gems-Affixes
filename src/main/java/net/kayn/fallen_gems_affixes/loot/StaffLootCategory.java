package net.kayn.fallen_gems_affixes.loot;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import io.redspace.ironsspellbooks.item.CastingItem;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class StaffLootCategory {

    private static final DeferredRegister<LootCategory> LOOT_CATEGORIES = DeferredRegister.create(Apoth.BuiltInRegs.LOOT_CATEGORY.key(), FallenGemsAffixes.MOD_ID);

    public static final LootCategory STAFF = new LootCategory(
            s -> s.getItem() instanceof CastingItem,
            ALObjects.EquipmentSlotGroups.MAINHAND,
            900);

    static {
        LOOT_CATEGORIES.register("staffs", () -> STAFF);
    }

    public static void register(IEventBus modEventBus) {
        LOOT_CATEGORIES.register(modEventBus);
    }

    public static boolean isStaff(ItemStack stack) {
        return LootCategory.forItem(stack) == STAFF;
    }
}