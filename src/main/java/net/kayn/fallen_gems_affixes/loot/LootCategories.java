package net.kayn.fallen_gems_affixes.loot;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.kayn.fallen_gems_affixes.util.LootCategoryUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

import static net.kayn.fallen_gems_affixes.Fallen.R;

public class LootCategories {
    public static class ISSLootCategories {
        public static final LootCategory STAFFS = LootCategoryUtil.registerLootCategoryOrFalse("staffs",
                ALObjects.EquipmentSlotGroups.MAINHAND,
                Check::staffCheck,
                1200,
                true
        );
        public static final LootCategory SPELLBOOK = LootCategoryUtil.registerCurioCategoryOrFalse("spellbook",
                Check::spellBookCheck,
                1200,
                true
        );
        public static void bootstrap() {};
    }

    public static void bootstrap(IEventBus bus) {
        if (ModList.get().isLoaded("irons_spellbooks") && !ModList.get().isLoaded("irons_apothic")) {
            ISSLootCategories.bootstrap();
        }
        bus.register(R);
    }

    public static class Check {
        public static boolean staffCheck(ItemStack i) {
            return i.getItem() instanceof CastingItem;
        }
        public static boolean spellBookCheck(ItemStack i) {
            return i.getItem() instanceof SpellBook;
        }
    }
}