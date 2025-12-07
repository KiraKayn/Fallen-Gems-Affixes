package net.kayn.fallen_gems_affixes.augment;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AugmentRegistry {
    private static final Map<ResourceLocation, IAugment> REGISTRY = new HashMap<>();
    public static final Map<ResourceLocation, IAugment> BY_ID = Collections.unmodifiableMap(REGISTRY);

    // Register an augment
    public static IAugment register(IAugment augment) {
       REGISTRY.put(augment.getId(), augment);
       return augment;
    }

    // Get an augment by ID
    public static IAugment get(ResourceLocation id) {
        return REGISTRY.get(id);
    }
}