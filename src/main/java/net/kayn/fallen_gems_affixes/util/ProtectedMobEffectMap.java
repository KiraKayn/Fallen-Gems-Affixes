package net.kayn.fallen_gems_affixes.util;

import net.kayn.fallen_gems_affixes.event.PermanentEffectHandler;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.collectPermanentEffects;

public class ProtectedMobEffectMap<E extends LivingEntity> extends HashMap<MobEffect, MobEffectInstance>{
    private final E owner;
    private final Map<MobEffect, EffectInstanceBucket> fallback = new HashMap<>();
    private final Map<EquipmentSlotWrapper, Set<MobEffect>> cachedEffectsBySlot = new HashMap<>();
    private final Set<MobEffect> currentPermanentEffects = new HashSet<>();
    private EffectOperator operator = EffectOperator.EXTERNAL;
    private static final ThreadLocal<ItemStack> lastEffectsProvider = ThreadLocal.withInitial(() -> ItemStack.EMPTY);
    private static final ThreadLocal<EquipmentSlotWrapper> currentSlot = ThreadLocal.withInitial(() -> EquipmentSlotWrappers.NONE);

    public ProtectedMobEffectMap(E owner) {
        this.owner = owner;
    }

    @Override
    public MobEffectInstance put(MobEffect effect, MobEffectInstance effectInst) {
        if (this.owner instanceof Player && operator != EffectOperator.EXTERNAL) {
            addPermanentEffect(currentSlot.get(), effect, effectInst.getAmplifier(), false);
        }
        return super.put(effect, effectInst);
    }

    @Override
    public MobEffectInstance remove(Object key) {
        if (!(this.owner instanceof Player)) {
            return super.remove(key);
        }
        if (operator == EffectOperator.EXTERNAL) {
             if(this.currentPermanentEffects.contains((MobEffect) key)) {
                 return null;
             }
        }
        else {
            MobEffectInstance effectInst = super.remove(key);
            if (effectInst != null) {
                tryRemovePermanentEffect(currentSlot.get(), (MobEffect) key, effectInst.getAmplifier(), false);
            }
            return effectInst;
        }
        return super.remove(key);
    }

    @Override
    public void clear() {
        if (!(this.owner instanceof Player)) return;
        if (this.operator == EffectOperator.EXTERNAL) {
            super.keySet().retainAll(collectPermanentEffects((LivingEntity) this.owner).keySet());
        }
        else {
            super.clear();
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        if (!(this.owner instanceof Player)) {
            return super.remove(key, value);
        }
        if (operator == EffectOperator.EXTERNAL) {
            if(this.currentPermanentEffects.contains((MobEffect) key)) {
                return false;
            }
        }
        else {
            if (value != null) {
                tryRemovePermanentEffect(currentSlot.get(), (MobEffect) key,((MobEffectInstance) value).getAmplifier(), false);
            }
            return true;
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
                        if (operator == EffectOperator.EXTERNAL) {
                            if(currentPermanentEffects.contains(current.getEffect())) {
                                return;
                            }
                        }
                        else {
                            if (current != null) {
                                tryRemovePermanentEffect(currentSlot.get(), current.getEffect(), current.getAmplifier(), false);
                            }
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

    public Set<MobEffect> getEffectsFromCache(EquipmentSlotWrapper slot) {
        return cachedEffectsBySlot.get(slot);
    }

    public enum EffectOperator {
        ON_EQUIP,
        ON_HANDLER,
        ON_INIT,
        EXTERNAL
    }

    public void setOperator(EffectOperator operator) {
        this.operator = operator;
    }

    public void setCurrentSlot(EquipmentSlotWrapper slot) {
        currentSlot.set(slot);
    }

    public Boolean isExternalRemover() {
        return this.operator == EffectOperator.EXTERNAL;
    }

    private void resetRemover() {
        this.operator = EffectOperator.EXTERNAL;
    }

    public void addPermanentEffect(EquipmentSlotWrapper slot, MobEffect effect, int amplifier, boolean isAltCondition) {
        if (effect == null) return;
        currentPermanentEffects.add(effect);
        tryUpdateCachedMobEffectsForSlot(slot, effect, PermanentEffectHandler.Operation.ADD, isAltCondition);
        EffectInstanceBucket potentialEffects = fallback.get(effect);
        if (potentialEffects == null) {
            potentialEffects = new EffectInstanceBucket(effect);
            fallback.put(effect, potentialEffects);
        }
        potentialEffects.add(amplifier);
    }

    private void updateCachedMobEffectsForSlot(EquipmentSlotWrapper slot, MobEffect effect, PermanentEffectHandler.Operation operation) {
        Set<MobEffect> effects = cachedEffectsBySlot.get(slot);
        if(effects == null) {
            effects = new HashSet<>();
            cachedEffectsBySlot.put(slot, effects);
        }
        switch (operation) {
            case REMOVE -> {
                effects.remove(effect);
            }
            case ADD -> {
                effects.add(effect);
            }
        }
    }

    public void tryRemovePermanentEffect(EquipmentSlotWrapper slot, MobEffect effect, int amplifier, boolean altCondition) {
        if (effect == null) return;
        var potentialEffects = fallback.get(effect);
        if (potentialEffects == null) return;
        potentialEffects.remove(amplifier);
        if (potentialEffects.size() == 0) {
            currentPermanentEffects.remove(effect);
            tryUpdateCachedMobEffectsForSlot(slot, effect, PermanentEffectHandler.Operation.REMOVE, altCondition);
        }
    }

    private void tryUpdateCachedMobEffectsForSlot(EquipmentSlotWrapper slot, MobEffect effect, PermanentEffectHandler.Operation operation, boolean isAltCondition) {
        if (owner.level().isClientSide || isAltCondition) {
            updateCachedMobEffectsForSlot(slot, effect, operation);
        }
    }

    public MobEffectInstance getLastPotentialEffectInst(MobEffect effect) {
        return new MobEffectInstance(effect, -1, fallback.get(effect).getLast());
    }

    public void initOperation(EquipmentSlotWrapper slotWrapper, EffectOperator pOperator, ItemStack effectsProvide) {
        operator = pOperator;
        lastEffectsProvider.set(effectsProvide);
        currentSlot.set(slotWrapper);
    }

    public void initOperation(EquipmentSlotWrapper slotWrapper, EffectOperator pOperator) {
        operator = pOperator;
        currentSlot.set(slotWrapper);
    }

    public void initOperation(EquipmentSlotWrapper slotWrapper) {
        operator = EffectOperator.ON_EQUIP;
        currentSlot.set(slotWrapper);
    }

    public void finalizeOperation() {
        operator = EffectOperator.EXTERNAL;
        currentSlot.set(EquipmentSlotWrappers.NONE);
    }

    public ItemStack getLastEffectsProvider() {
        return lastEffectsProvider.get();
    }

    public void setLastEffectsProvider(ItemStack itemStack) {
        lastEffectsProvider.set(itemStack);
    }
}