package net.kayn.fallen_gems_affixes.attachment.augment;

import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("rawtypes")
public class LiveAugments implements Iterable<Map.Entry<IAugment, AugmentInstance>> {
    private final Map<IAugment, AugmentInstance> map;

    public LiveAugments(Map<IAugment, AugmentInstance> map) {
        this.map = Collections.unmodifiableMap(map);
    }

    public Set<IAugment> augments() {
        return map.keySet();
    }

    public Collection<AugmentInstance> instances() {
        return map.values();
    }

    public AugmentInstance get(IAugment aug) {
        return map.get(aug);
    }

    public boolean contains(IAugment aug) {
        return map.containsKey(aug);
    }

    public Set<Map.Entry<IAugment, AugmentInstance>> entries() {
        return map.entrySet();
    }

    @Override
    public @NotNull Iterator<Map.Entry<IAugment, AugmentInstance>> iterator() {
        return map.entrySet().iterator();
    }
}
