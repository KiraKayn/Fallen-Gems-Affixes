package net.kayn.fallen_gems_affixes.attachment.permanent_effect_v2;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.mixin.accessor.LivingEntityAccessor;
import net.kayn.fallen_gems_affixes.mixin.accessor.ServerPlayerAccessor;
import net.kayn.fallen_gems_affixes.network.ClientlikeUpdatePermanentEffectPacket;
import net.kayn.fallen_gems_affixes.types.IEffectHandler;
import net.kayn.fallen_gems_affixes.types.IVanillaLikeEffectHandler;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;

import javax.annotation.Nullable;
import java.util.Set;

public class VanillaLikeEffectHandler implements IVanillaLikeEffectHandler, IEffectHandler {
    LivingEntity entity;

    public VanillaLikeEffectHandler(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void refreshDirtyAttributes() {
        Set<AttributeInstance> set = this.entity.getAttributes().getAttributesToUpdate();

        for(AttributeInstance attributeinstance : set) {
            this.onAttributeUpdated(attributeinstance.getAttribute());
        }

        set.clear();
    }

    @Override
    public void onEffectUpdated(MobEffectInstance effectInstance, boolean forced, @Nullable Entity source) {
        ((LivingEntityAccessor)this.entity).setEffectsDirty(true);
        if (forced && !this.entity.level().isClientSide) {
            MobEffect mobeffect = effectInstance.getEffect().value();
            mobeffect.removeAttributeModifiers(this.entity.getAttributes());
            mobeffect.addAttributeModifiers(this.entity.getAttributes(), effectInstance.getAmplifier());
            this.refreshDirtyAttributes();
        }
    }

    @Override
    public void onAttributeUpdated(Holder<Attribute> attribute) {
        if (attribute.is(Attributes.MAX_HEALTH)) {
            float f = this.entity.getMaxHealth();
            if (this.entity.getHealth() > f) {
                this.entity.setHealth(f);
            }
        } else if (attribute.is(Attributes.MAX_ABSORPTION)) {
            float f1 = this.entity.getMaxAbsorption();
            if (this.entity.getAbsorptionAmount() > f1) {
                this.entity.setAbsorptionAmount(f1);
            }
        }
    }

    @Override
    public MobEffectInstance addEffectRet(MobEffectInstance effectInstance) {
        return this.addEffectRet(effectInstance, null);
    }

    @Override
    public void addEffectSilent(MobEffectInstance effectInstance) {
        MobEffectInstance mobeffectinstance = this.entity.getActiveEffectsMap().get(effectInstance.getEffect());
        if (mobeffectinstance == null) {
            this.entity.getActiveEffectsMap().put(effectInstance.getEffect(), effectInstance);
            if (!this.entity.level().isClientSide) {
                effectInstance.getEffect().value().addAttributeModifiers(this.entity.getAttributes(), effectInstance.getAmplifier());
            }
        } else if (mobeffectinstance.update(effectInstance)) {
            if (!this.entity.level().isClientSide) {
                MobEffect mobeffect = effectInstance.getEffect().value();
                mobeffect.removeAttributeModifiers(this.entity.getAttributes());
                mobeffect.addAttributeModifiers(this.entity.getAttributes(), effectInstance.getAmplifier());
                this.refreshDirtyAttributes();
            }
        }
    }

    public MobEffectInstance addEffectRet(MobEffectInstance effectInstance, LivingEntity source) {
        MobEffectInstance mobeffectinstance = this.entity.getActiveEffectsMap().get(effectInstance.getEffect());
        if (mobeffectinstance == null) {
            this.entity.getActiveEffectsMap().put(effectInstance.getEffect(), effectInstance);
            this.onEffectAdded(effectInstance, source);
            return effectInstance;
        } else if (mobeffectinstance.update(effectInstance)) {
            this.onEffectUpdated(mobeffectinstance, true, source);
            if (this.entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientlikeUpdatePermanentEffectPacket(effectInstance.getEffect(), effectInstance.getAmplifier(), false));
            }
            return mobeffectinstance;
        } else {
            // must sync to client.
            ((LivingEntityAccessor)this.entity).setEffectsDirty(true);
            if (this.entity instanceof ServerPlayer serverPlayer) {
                FallenGemsAffixes.LOGGER.warn("Added nothing, sync client");
                serverPlayer.connection.send(new ClientlikeUpdatePermanentEffectPacket(effectInstance.getEffect(), effectInstance.getAmplifier(), false));
            }
            return null;
        }
    }

    @Override
    public void onEffectAdded(MobEffectInstance effectInstance, LivingEntity source) {
        ((LivingEntityAccessor)this.entity).setEffectsDirty(true);
        if (!this.entity.level().isClientSide) {
            effectInstance.getEffect().value().addAttributeModifiers(this.entity.getAttributes(), effectInstance.getAmplifier());
            if (this.entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientlikeUpdatePermanentEffectPacket(effectInstance.getEffect(), effectInstance.getAmplifier(), false));
            }
        }
    }

    @Override
    public boolean addEffect(MobEffectInstance effectInstance, LivingEntity source) {
        return this.addEffectRet(effectInstance, source) != null;
    }


    @Override
    public boolean removeEffect(Holder<MobEffect> effect) {
        return this.removeEffectRet(effect) != null;
    }

    public boolean removeEffect(Holder<MobEffect> effect, int amplifier) {
        return this.removeEffectRet(effect, amplifier) != null;
    }

    @Override
    public MobEffectInstance removeEffectRet(Holder<MobEffect> effect) {
        MobEffectInstance mobeffectinstance = this.entity.getActiveEffectsMap().remove(effect);
        if (mobeffectinstance != null) {
            mobeffectinstance.getEffect().value().removeAttributeModifiers(this.entity.getAttributes());
            this.onEffectRemoved(mobeffectinstance);
            this.refreshDirtyAttributes();
            return mobeffectinstance;
        } else {
            return null;
        }
    }

    @Override
    public void removeEffectNoSync(Holder<MobEffect> effect) {
        MobEffectInstance mobeffectinstance = this.entity.getActiveEffectsMap().remove(effect);
        if (mobeffectinstance != null) {
            mobeffectinstance.getEffect().value().removeAttributeModifiers(this.entity.getAttributes());
            ((LivingEntityAccessor)this.entity).setEffectsDirty(true);
            if (this.entity instanceof ServerPlayer player) {
                if (effect.is(MobEffects.LEVITATION)) {
                    ((ServerPlayerAccessor)player).setLevitationStartPos(null);
                }
            }
            this.refreshDirtyAttributes();
        } else {
            ((LivingEntityAccessor)this.entity).setEffectsDirty(true);
        }
    }

    public MobEffectInstance removeEffectRet(Holder<MobEffect> effect, int amplifier) {
        MobEffectInstance mobeffectinstance = this.entity.getActiveEffectsMap().remove(effect);
        if (mobeffectinstance != null) {
            mobeffectinstance.getEffect().value().removeAttributeModifiers(this.entity.getAttributes());
            this.onEffectRemoved(mobeffectinstance);
            this.refreshDirtyAttributes();
            return mobeffectinstance;
        } else {
            // must sync to client.
            ((LivingEntityAccessor)this.entity).setEffectsDirty(true);
            if (this.entity instanceof ServerPlayer player) {
                FallenGemsAffixes.LOGGER.warn("Removed nothing, sync client");
                player.connection.send(new ClientlikeUpdatePermanentEffectPacket(effect, amplifier, true));
            }
            return null;
        }
    }

    @Override
    public void onEffectRemoved(MobEffectInstance effectInstance) {
        ((LivingEntityAccessor)this.entity).setEffectsDirty(true);
        if (this.entity instanceof ServerPlayer player) {
            player.connection.send(new ClientlikeUpdatePermanentEffectPacket(effectInstance.getEffect(), effectInstance.getAmplifier(), true));
            if (effectInstance.getEffect().is(MobEffects.LEVITATION)) {
                ((ServerPlayerAccessor)player).setLevitationStartPos(null);
            }
        }
    }

    public void onEffectRemoved(Holder<MobEffect> effect, int amplifier) {
        ((LivingEntityAccessor)this.entity).setEffectsDirty(true);
        if (this.entity instanceof ServerPlayer player) {
            player.connection.send(new ClientlikeUpdatePermanentEffectPacket(effect, amplifier, true));
            if (effect.is(MobEffects.LEVITATION)) {
                ((ServerPlayerAccessor)player).setLevitationStartPos(null);
            }
        }
    }
}