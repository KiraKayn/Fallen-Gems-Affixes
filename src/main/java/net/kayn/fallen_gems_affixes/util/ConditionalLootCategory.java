package net.kayn.fallen_gems_affixes.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record ConditionalLootCategory(Optional<String> modid, String cat, boolean notMode) {
    public static final Codec<ConditionalLootCategory> CODEC = RecordCodecBuilder.create((inst) -> inst
            .group(
                    Codec.STRING.optionalFieldOf("modid").forGetter(ConditionalLootCategory::modid),
                    Codec.STRING.fieldOf("category").forGetter(ConditionalLootCategory::cat),
                    Codec.BOOL.optionalFieldOf("not_mode", false).forGetter(ConditionalLootCategory::notMode))
            .apply(inst, ConditionalLootCategory::new));
    public static final Codec<Set<ConditionalLootCategory>> SET_CODEC = CODEC.listOf().xmap(HashSet::new, ArrayList::new);
    public boolean test() {
        if (modid().isEmpty()) return false;
        boolean flag = ModList.get().isLoaded(modid.get());
        return notMode != flag;
    }
}
