package net.kayn.fallen_gems_affixes.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import net.kayn.fallen_gems_affixes.mixin.GemClassMixin;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class GemBonusUtil {
    public static final Codec<GemBonus> CONDITIONAL_CAT_CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    ConditionalLootCategory.SET_CODEC.optionalFieldOf("con_cat", Set.of()).forGetter(bonus -> Set.of()),
                    GemBonus.CODEC.fieldOf("bonus").forGetter(Function.identity()))
            .apply(inst, (conCategories, bonus) -> {
                GemClass gemClass = bonus.getGemClass();
                HolderSet<LootCategory> categories = gemClass.types();
                if (conCategories != null && !conCategories.isEmpty()) {
                    conCategories.forEach(cat -> {
                        if (cat.test()) {
                            var reg = Apoth.BuiltInRegs.LOOT_CATEGORY;
                            LootCategory category = reg.get(ResourceLocation.tryParse(cat.cat()));
                            if (category != null) {
                                var optNewCategories = categories.unwrap().right();
                                if (optNewCategories.isPresent()) {
                                    List<Holder<LootCategory>> newCategories = new ArrayList<>(optNewCategories.get());
                                    newCategories.add(reg.wrapAsHolder(category));
                                    ((GemClassMixin)(Object) gemClass).setTypes(HolderSet.direct(newCategories));
                                }
                            }
                        }
                    });
                }
                return bonus;
            }));
}
