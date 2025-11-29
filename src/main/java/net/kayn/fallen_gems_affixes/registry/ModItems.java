package net.kayn.fallen_gems_affixes.registry;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.item.augments.DeathsDefianceItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FallenGemsAffixes.MOD_ID);

    public static final RegistryObject<Item> SOULBOUND_AUGMENT_ITEM = ITEMS.register("deaths_defiance",
            () -> new DeathsDefianceItem(new Item.Properties()
                    .stacksTo(16)
                    .rarity(Rarity.RARE)
            ));
}