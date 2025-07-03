package net.kayn.fallen_gems_affixes.util;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.collectPermanentEffects;

public class ProtectedMobEffectMap<E extends Entity> extends HashMap<MobEffect, MobEffectInstance> {
    private final E owner;
    private final Set<MobEffect> currentPermanentEffects = new HashSet<>();
    private final Map<MobEffect, EffectInstanceBucket> fallback = new HashMap<>();
//    private final Map<MobEffect, MobEffectInstance> currentPermanentEffectsMap = new HashMap<>();
    private EffectRemover remover = EffectRemover.EXTERNAL;

    // TODO: once the test passes, delete the LOGGER
    private static final Logger LOGGER = LogManager.getLogger();

    public ProtectedMobEffectMap(E owner) {
        this.owner = owner;
    }

    @Override
    public MobEffectInstance remove(Object key) {
        LOGGER.info("into remove {}", remover);
        LOGGER.info("class : {}", this.owner.getClass());
        if (!(this.owner instanceof Player)) {
            LOGGER.info("into player check {}", this.owner);
            return super.remove(key);
        };
        this.currentPermanentEffects.forEach(e -> LOGGER.info("current {}", e));
        LOGGER.info("Object {}", key);
        if (remover == EffectRemover.EXTERNAL && this.currentPermanentEffects.contains((MobEffect) key)) {
            LOGGER.info("into remove inner {}", key);
            return null;
        }
        return super.remove(key);
    }

    @Override
    public void clear() {
        LOGGER.info("into clear {}", remover);
        if (!(this.owner instanceof Player)) return;
        if (this.remover == EffectRemover.EXTERNAL) {
            super.keySet().retainAll(collectPermanentEffects((LivingEntity) this.owner));
        }
        else {
            super.clear();
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        LOGGER.info("into remove2 {}", remover);
        if (!(this.owner instanceof Player) || remover == EffectRemover.EXTERNAL && this.currentPermanentEffects.contains((MobEffect) key)) {
            return false;
        }
        return super.remove(key, value);
    }

    @Override
    public @NotNull Collection<MobEffectInstance> values() {
        Collection<MobEffectInstance> original = super.values();
        return new AbstractCollection<>() {
            @Override
            public @NotNull Iterator<MobEffectInstance> iterator() {
                Iterator<MobEffectInstance> originalIterator = original.iterator();
                return new Iterator<>() {
                    MobEffectInstance current;

                    @Override
                    public boolean hasNext() {
                        return originalIterator.hasNext();
                    }

                    @Override
                    public MobEffectInstance next() {
                        current = originalIterator.next();
                        return current;
                    }

                    @Override
                    public void remove() {
//                        LOGGER.info("Intercepted iterator.remove() for effect: {}", current);
//                        LOGGER.info("{}, {}", current, remover);
                        LOGGER.info("into iterator remove {}", remover);
                        if (remover == EffectRemover.EXTERNAL && currentPermanentEffects.contains(current.getEffect())) {
//                            currentPermanentEffects.forEach(LOGGER::info);
                            return;
                        }
                        originalIterator.remove();
                    }
                };
            }

            @Override
            public int size() {
                return original.size();
            }
        };
    }

    public boolean containsPermanent(MobEffect effect) {
        return currentPermanentEffects.contains(effect);
    }

    public enum EffectRemover {
        ON_EQUIP,
        ON_REMOVE,
        EXTERNAL
    }

    public void setRemover(EffectRemover remover) {
        this.remover = remover;
    }

    public Boolean isExternalRemover() {
        return this.remover == EffectRemover.EXTERNAL;
    }

    private void resetRemover() {
        this.remover = EffectRemover.EXTERNAL;
    }

    public void addPermanentEffect(MobEffect effect, int amplifier) {
        if (effect == null) return;
        currentPermanentEffects.add(effect);
        EffectInstanceBucket bucket = fallback.get(effect);
        if (bucket == null) {
            bucket = new EffectInstanceBucket(effect);
            fallback.put(effect, bucket);
        }
        bucket.add(amplifier);
    }

    public void tryRemovePermanentEffect(MobEffect effect, int amplifier) {
        if (effect == null) return;
        var potentialEffects = fallback.get(effect);
        potentialEffects.remove(amplifier);
        if (potentialEffects.size() == 0) {
            currentPermanentEffects.remove(effect);
        }
    }

    public MobEffectInstance getLastPotentialEffectInst(MobEffect effect) {
        return new MobEffectInstance(effect, -1, fallback.get(effect).getLast());
    }
}