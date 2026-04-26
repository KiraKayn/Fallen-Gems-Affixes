package net.kayn.fallen_gems_affixes.attachment.augment;

import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("rawtypes")
public class LiveAugments implements Iterable<Map.Entry<IAugment, AugmentInstance>> {
    private final Map<IAugment, AugmentInstance> map;
    public static final LiveAugments EMPTY = new LiveAugments(Collections.emptyMap());

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

    public IAugmentInnerData getData(IAugment aug) {
        AugmentInstance ins = get(aug);
        return ins == null ? null : ins.getData();
    }

    public boolean contains(IAugment aug) {
        return map.containsKey(aug);
    }

    public Set<Map.Entry<IAugment, AugmentInstance>> entries() {
        return map.entrySet();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public @NotNull Iterator<Map.Entry<IAugment, AugmentInstance>> iterator() {
        return map.entrySet().iterator();
    }

    public Map<IAugment, AugmentInstance> toMap() {
        return new HashMap<>(map);
    }
}
