package net.kayn.fallen_gems_affixes.datagen;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.init.loot.GemLootModifier;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.List;

public class GemGlobalLootModifierProvider extends GlobalLootModifierProvider {

    public GemGlobalLootModifierProvider(PackOutput output) {
        super(output, FallenGemsAffixes.MOD_ID);
    }

    @Override
    protected void start() {
        add("infernal_overlord_modifier",
                new GemLootModifier(
                        new LootItemCondition[0],
                        List.of(
                                new GemLootModifier.GemEntry("fallen_gems_affixes:boss_gems/infernal_overlord", "apotheosis:epic", 1.0F),
                                new GemLootModifier.GemEntry("fallen_gems_affixes:boss_gems/infernal_overlord", "apotheosis:mythic", 0.5F),
                                new GemLootModifier.GemEntry("fallen_gems_affixes:boss_gems/infernal_overlord", "apotheosis:ancient", 0.25F)
                        )
                )
        );
    }

    public static void gather(GatherDataEvent event) {
        if (event.includeServer()) {
            event.getGenerator().addProvider(true, new GemGlobalLootModifierProvider(event.getGenerator().getPackOutput()));
        }
    }
}