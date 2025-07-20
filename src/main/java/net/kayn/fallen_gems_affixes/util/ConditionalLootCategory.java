package net.kayn.fallen_gems_affixes.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraftforge.fml.ModList;

import java.util.*;
import java.util.stream.Collectors;

public record ConditionalLootCategory(Optional<String> modid, String cat) {
    public static final Codec<ConditionalLootCategory> CODEC = RecordCodecBuilder.create((inst) -> inst
            .group(
                    Codec.STRING.optionalFieldOf("modid").forGetter(ConditionalLootCategory::modid),
                    Codec.STRING.fieldOf("category").forGetter(ConditionalLootCategory::cat))
            .apply(inst, ConditionalLootCategory::new));
    public static final Codec<Set<ConditionalLootCategory>> SET_CODEC = CODEC.listOf().xmap(HashSet::new, ArrayList::new);
    public boolean test() {
        if (modid().isEmpty()) return false;
        return ModList.get().isLoaded(modid.get());
    }
}
