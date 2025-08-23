package net.kayn.fallen_gems_affixes.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record ConditionalType(Optional<String> modid, String type, boolean notMode) {
    public static final Codec<ConditionalType> CODEC = RecordCodecBuilder.create((inst) -> inst
            .group(
                    Codec.STRING.optionalFieldOf("modid").forGetter(ConditionalType::modid),
                    Codec.STRING.fieldOf("type").forGetter(ConditionalType::type),
                    Codec.BOOL.optionalFieldOf("notMode", false).forGetter(ConditionalType::notMode))
            .apply(inst, ConditionalType::new));
    public static final Codec<Set<ConditionalType>> SET_CODEC = CODEC.listOf().xmap(HashSet::new, ArrayList::new);
    public boolean test() {
        if (modid().isEmpty()) return false;
        boolean flag = ModList.get().isLoaded(modid.get());
        return notMode != flag;
    }
}
