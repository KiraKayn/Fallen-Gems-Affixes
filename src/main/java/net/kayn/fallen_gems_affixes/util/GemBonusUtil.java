package net.kayn.fallen_gems_affixes.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;

import java.util.Set;
import java.util.function.Function;

public class GemBonusUtil {
    public static final Codec<GemBonus> CONDITIONAL_CAT_CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    ConditionalLootCategory.SET_CODEC.optionalFieldOf("con_cat", Set.of()).forGetter(bonus -> Set.of()),
                    GemBonus.CODEC.fieldOf("bonus").forGetter(Function.identity()))
            .apply(inst, (conCategories, bonus) -> {
                Set<LootCategory> categories = bonus.getGemClass().types();
                if (conCategories != null && !conCategories.isEmpty()) {
                    conCategories.forEach(cat -> {
                        if (cat.test()) {
                            categories.add(LootCategory.byId(cat.cat()));
                        }
                    });
                }
                return bonus;
            }));
}
