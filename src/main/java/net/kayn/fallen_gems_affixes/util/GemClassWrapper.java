package net.kayn.fallen_gems_affixes.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import net.minecraftforge.fml.ModList;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.Set;

public class GemClassWrapper {
    public static final Codec<GemClassWrapper> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    Codec.STRING.fieldOf("key").forGetter(a -> a.key),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.categories),
                    ConditionalLootCategory.SET_CODEC.optionalFieldOf("con_cat", Set.of()).forGetter(a -> a.conCategories))
            .apply(inst, GemClassWrapper::new));
    private final String key;
    private final Set<LootCategory> categories;
    private final Set<ConditionalLootCategory> conCategories;
    public final GemClass instance;
    public GemClassWrapper(String key, Set<LootCategory> categories, Set<ConditionalLootCategory> conCategories) {
        this.key = key;
        this.categories = categories;
        this.conCategories = conCategories;
        if (conCategories != null && !conCategories.isEmpty()) {
            conCategories.forEach(cat -> {
                if (cat.test()) {
                    categories.add(LootCategory.byId(cat.cat()));
                }
            });
        }
        this.instance = new GemClass(key, categories);
    }
}
