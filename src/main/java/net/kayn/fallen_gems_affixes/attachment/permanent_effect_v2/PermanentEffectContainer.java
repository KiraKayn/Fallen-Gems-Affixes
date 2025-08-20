package net.kayn.fallen_gems_affixes.attachment.permanent_effect_v2;

import com.mojang.datafixers.util.Pair;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static net.kayn.fallen_gems_affixes.util.CodecUtil.HOLDER_MOB_EFFECT_CODEC;

public class PermanentEffectContainer implements INBTSerializable<CompoundTag> {
    Map<Holder<MobEffect>, List<Integer>> dataMap = new HashMap<>();

    public Set<Holder<MobEffect>> getEffects() {
        return this.dataMap.keySet();
    }

    public void addEffect(Holder<MobEffect> effect, int amplifier) {
        this.dataMap.compute(effect, (a, b) -> {
            if (b == null) {
                b = new ArrayList<>();
                b.add(amplifier);
            } else {
                b.add(amplifier);
                b.sort(Comparator.naturalOrder());
            }
            return b;
        });
    }

    public int tryRemoveEffect(Holder<MobEffect> effect, int amplifier) {
        List<Integer> levels = this.dataMap.get(effect);
        if (levels != null) {
            if (levels.size() == 1) {
                this.removeEffect(effect);
            } else {
                levels.remove(Integer.valueOf(amplifier));
            }
            return amplifier;
        }
        return -1;
    }

    public void removeEffect(Holder<MobEffect> effect) {
        this.dataMap.remove(effect);
    }

    public void clearEffects() {
        this.dataMap.clear();
    }

    public Iterator<Map.Entry<Holder<MobEffect>, List<Integer>>> getIterator() {
        return this.dataMap.entrySet().iterator();
    }

    public void forEachEffect(BiConsumer<Holder<MobEffect>, List<Integer>> consumer) {
        this.dataMap.forEach(consumer);
    }

    public boolean containsEffect(Holder<MobEffect> effect) {
        return this.dataMap.containsKey(effect);
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        ListTag listTag = new ListTag();
        for (Map.Entry<Holder<MobEffect>, List<Integer>> data : this.dataMap.entrySet()) {
            if (data == null) continue;
            Optional<Tag> effectTag = HOLDER_MOB_EFFECT_CODEC.encodeStart(NbtOps.INSTANCE, data.getKey())
                    .resultOrPartial(error -> FallenGemsAffixes.LOGGER.error("Error encoding effect: {}", error));
            if (effectTag.isPresent()) {
                CompoundTag tag = new CompoundTag();
                tag.put("Effect", effectTag.get());
                tag.putIntArray("Levels", data.getValue());
                listTag.add(tag);
            }
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Effects", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        this.dataMap.clear();
        ListTag listTag = nbt.getList("Effects", Tag.TAG_COMPOUND);
        for (Tag tag : listTag) {
            CompoundTag tag1 = (CompoundTag) tag;
            Optional<Pair<Holder<MobEffect>, Tag>> data = HOLDER_MOB_EFFECT_CODEC.decode(NbtOps.INSTANCE, tag1.get("Effect"))
                    .resultOrPartial(error -> FallenGemsAffixes.LOGGER.error("Error decoding tag: {}", error));
            data.ifPresent(pair -> this.dataMap.put(pair.getFirst(), Arrays.stream(tag1.getIntArray("Levels")).boxed().collect(Collectors.toList())));
        }
    }
}
