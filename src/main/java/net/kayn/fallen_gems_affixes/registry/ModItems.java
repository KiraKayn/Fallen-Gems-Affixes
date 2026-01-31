package net.kayn.fallen_gems_affixes.registry;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.item.SigilOfAscensionItem;
import net.kayn.fallen_gems_affixes.item.SigilOfSeveranceItem;
import net.kayn.fallen_gems_affixes.item.SigilOfTransmutationItem;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FallenGemsAffixes.MOD_ID);

    public static final RegistryObject<Item> AUGMENT_ITEM = ITEMS.register("augment",
            () -> new AugmentItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC)
            ));

    public static final RegistryObject<Item> SIGIL_OF_ASCENSION = ITEMS.register("sigil_of_ascension",
            () -> new SigilOfAscensionItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)
            ));

    public static final RegistryObject<Item> SIGIL_OF_TRANSMUTATION = ITEMS.register("sigil_of_transmutation",
            () -> new SigilOfTransmutationItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)
            ));
    public static final RegistryObject<Item> SIGIL_OF_SEVERANCE = ITEMS.register("sigil_of_severance",
            () -> new SigilOfSeveranceItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)
            ));
}