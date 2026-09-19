package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.network.casting.SyncTargetingDataPacket;
import net.kayn.fallen_gems_affixes.adventure.affix.*;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.SpellEchoHandler;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.SpellEffectBonus;
import net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.util.TriConsumer;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static net.kayn.fallen_gems_affixes.adventure.affix.SpellCastAffix.isCurrentlyTriggering;

public class SpellEventHandler {

    private static final Map<UUID, Map<String, Integer>> ACTIVE_CAST_LEVELS = new ConcurrentHashMap<>();
    public static final Map<UUID, UUID> LAST_SPELL_DAMAGE_TARGET = new ConcurrentHashMap<>();

    public static final BiConsumer<LivingEntity, LivingEntity> triggerCurio;
    public static final TriConsumer<ItemStack, LivingEntity, LivingEntity> triggerAffixes;

    static {
        if (!ModList.get().isLoaded("irons_apothic")) {
            triggerAffixes = (stack, caster, target) -> {
                AffixHelper.streamAffixes(stack).forEach(inst -> {
                    if (inst.affix().get() instanceof SpellEffectAffix affix) {
                        switch (affix.target) {
                            case SPELL_DAMAGE_SELF, SPELL_HEAL_SELF -> affix.applyEffect(caster, inst.getRarity(), inst.level());
                            case SPELL_DAMAGE_TARGET, SPELL_HEAL_TARGET -> affix.applyEffect(target, inst.getRarity(), inst.level());
                        }
                    }
                });
            };
        } else {
            triggerAffixes = (stack, caster, target) -> {};
        }

        if (ModList.get().isLoaded("curios")) {
            triggerCurio = (caster, target) -> {
                ICuriosItemHandler handler = CuriosApi.getCuriosInventory(caster).orElse(null);
                if (handler == null) return;
                for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                    ICurioStacksHandler slotHandler = entry.getValue();
                    try {
                        for (int i = 0; i < slotHandler.getSlots(); i++) {
                            ItemStack equippedStack = slotHandler.getStacks().getStackInSlot(i);
                            if (!EquipmentSlotUtil.simpleMatchesCurioSlot(caster, equippedStack, entry.getKey())) continue;
                            applyAllEffects(equippedStack, caster, target);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        } else {
            triggerCurio = (a, b) -> {};
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSpellCastCaptureLevel(SpellOnCastEvent event) {
        Player caster = event.getEntity();
        ACTIVE_CAST_LEVELS
                .computeIfAbsent(caster.getUUID(), u -> new ConcurrentHashMap<>())
                .put(event.getSpellId(), event.getSpellLevel());

        if (ACTIVE_CAST_LEVELS.size() % 128 == 0) {
            ServerLevel serverLevel = (ServerLevel) caster.level();
            ACTIVE_CAST_LEVELS.keySet().removeIf(uuid -> {
                Entity ent = serverLevel.getEntity(uuid);
                return ent == null || ent.isRemoved() || !ent.isAlive();
            });
        }
    }

    public static void updateTargetData(LivingEntity caster, Entity entityHit, MagicData playerMagicData,
                                        AbstractSpell spell, Predicate<LivingEntity> filter) {
        if (playerMagicData.getAdditionalCastData() != null) return;

        String id = spell.getSpellId();
        if (id.equals("irons_spellbooks:teleport") || id.equals("irons_spellbooks:blood_step")) return;

        LivingEntity livingTarget = null;
        if (entityHit instanceof LivingEntity livingEntity && filter.test(livingEntity)) {
            livingTarget = livingEntity;
        } else if (entityHit instanceof PartEntity<?> partEntity &&
                partEntity.getParent() instanceof LivingEntity livingParent &&
                filter.test(livingParent)) {
            livingTarget = livingParent;
        }

        if (livingTarget != null) {
            playerMagicData.setAdditionalCastData(new TargetEntityCastData(livingTarget));
            if (caster instanceof ServerPlayer serverPlayer && spell.getCastType() != CastType.INSTANT) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncTargetingDataPacket(livingTarget, spell));
            }
        } else if (caster instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable("ui.irons_spellbooks.cast_error_target").withStyle(ChatFormatting.RED),
                    true);
        }
    }

    private static LivingEntity getSpellTarget(Optional<SpellCastAffix.TargetType> targetType,
                                               LivingEntity caster, LivingEntity contextTarget) {
        return targetType.filter(type -> type == SpellCastAffix.TargetType.SELF)
                .map(type -> caster).orElse(contextTarget);
    }

    @SubscribeEvent
    public static void onSpellCast(SpellOnCastEvent event) {
        LivingEntity caster = event.getEntity();
        if (caster.level().isClientSide()) return;
        if (isCurrentlyTriggering(caster)) return;
        if (event.getCastSource() == CastSource.COMMAND) return;

        AbstractSpell castedSpell = SpellRegistry.getSpell(ResourceLocation.parse(event.getSpellId()));

        for (ItemStack stack : caster.getAllSlots()) {
            AffixHelper.streamAffixes(stack).forEach(inst -> {
                if (inst.affix().get() instanceof CooldownResetAffix affix) {
                    affix.onSpellCast(caster, event.getSpellId(), inst.getRarity(), inst.level());
                } else if (inst.affix().get() instanceof ChainCastAffix affix) {
                    boolean isTargetMode = affix.target.filter(t -> t == SpellCastAffix.TargetType.TARGET).isPresent();
                    if (!isTargetMode) {
                        affix.onSpellCast(caster, event.getSpellId(), caster, inst.getRarity());
                    }
                }
            });
        }

        if (castedSpell != null) {
            SpellEchoHandler.handle(caster, castedSpell, event.getSpellLevel(), caster);
        }
    }

    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;

        SpellDamageSource source = event.getSpellDamageSource();
        Entity entity = source.getEntity();
        if (!(entity instanceof LivingEntity caster)) return;

        if (!SpellCastAffix.isCurrentlyTriggering(caster)) {
            LAST_SPELL_DAMAGE_TARGET.put(caster.getUUID(), target.getUUID());
        }

        for (ItemStack stack : caster.getAllSlots()) {
            applyAllEffects(stack, caster, target);
        }
        triggerCurio.accept(caster, target);

        if (SpellCastAffix.isCurrentlyTriggering(caster)) return;

        final String castSpellId = source.spell().getSpellId();
        final int spellLevel = ACTIVE_CAST_LEVELS
                .getOrDefault(caster.getUUID(), Collections.emptyMap())
                .getOrDefault(castSpellId, 1);

        for (ItemStack stack : caster.getAllSlots()) {
            if (stack.isEmpty()) continue;

            AffixHelper.streamAffixes(stack).forEach(inst -> {
                if (!inst.isValid()) return;
                var affix = inst.affix().get();
                var rarity = inst.getRarity();
                float affixLevel = inst.level();

                if (affix instanceof SpellEffectAffix effectAffix) {
                    if (effectAffix.target == SpellEffectAffix.Target.SPELL_DAMAGE_TARGET) {
                        effectAffix.applyEffect(target, rarity, affixLevel);
                    } else if (effectAffix.target == SpellEffectAffix.Target.SPELL_DAMAGE_SELF) {
                        effectAffix.applyEffect(caster, rarity, affixLevel);
                    }
                } else if (affix instanceof SpellCastAffix castAffix && castAffix.trigger == SpellCastAffix.TriggerType.SPELL_DAMAGE) {
                    LivingEntity actualTarget = getSpellTarget(castAffix.target, caster, target);
                    castAffix.triggerSpell(caster, actualTarget, rarity, (int) affixLevel);
                } else if (affix instanceof ChainCastAffix chainCastAffix) {
                    boolean isTargetMode = chainCastAffix.target.filter(t -> t == SpellCastAffix.TargetType.TARGET).isPresent();
                    if (isTargetMode) chainCastAffix.onSpellCast(caster, castSpellId, target, rarity);
                } else if (affix instanceof SpellFocusAffix focusAffix) {
                    float mult = focusAffix.onSpellDamage(caster, target, castSpellId, rarity, affixLevel);
                    event.setAmount(event.getAmount() * mult);
                } else if (affix instanceof ManaDamageAffix manaAffix && caster instanceof Player player) {
                    float mult = manaAffix.getDamageMultiplier(player, rarity, affixLevel);
                    event.setAmount(event.getAmount() * mult);
                }
            });

            checkGemBonus(stack, (gem, bonus) -> {
                if (bonus.target == SpellEffectAffix.Target.SPELL_DAMAGE_TARGET) {
                    bonus.applyEffect(gem, target);
                } else if (bonus.target == SpellEffectAffix.Target.SPELL_DAMAGE_SELF) {
                    bonus.applyEffect(gem, caster);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onSpellHeal(SpellHealEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity caster = event.getEntity();
        LivingEntity healTarget = event.getTargetEntity();

        for (ItemStack stack : caster.getAllSlots()) {
            applyAllEffects(stack, caster, healTarget);
        }
        triggerCurio.accept(caster, healTarget);

        if (isCurrentlyTriggering(caster)) return;

        for (ItemStack stack : caster.getAllSlots()) {
            AffixHelper.streamAffixes(stack).forEach(inst -> {
                if (inst.affix().get() instanceof SpellEffectAffix affix) {
                    if (affix.target == SpellEffectAffix.Target.SPELL_HEAL_TARGET) {
                        affix.applyEffect(healTarget, inst.getRarity(), inst.level());
                    } else if (affix.target == SpellEffectAffix.Target.SPELL_HEAL_SELF) {
                        affix.applyEffect(caster, inst.getRarity(), inst.level());
                    }
                } else if (inst.affix().get() instanceof SpellCastAffix affix
                        && affix.trigger == SpellCastAffix.TriggerType.SPELL_HEAL) {
                    LivingEntity actualTarget = getSpellTarget(affix.target, caster, healTarget);
                    affix.triggerSpell(caster, actualTarget, inst.getRarity(), (int) inst.level());
                }
            });

            checkGemBonus(stack, (gem, bonus) -> {
                if (bonus.target == SpellEffectAffix.Target.SPELL_HEAL_TARGET) bonus.applyEffect(gem, healTarget);
                else if (bonus.target == SpellEffectAffix.Target.SPELL_HEAL_SELF) bonus.applyEffect(gem, caster);
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onChangeMana_Cost(ChangeManaEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getNewMana() >= event.getOldMana()) return;

        Player player = event.getEntity();
        MagicData magicData = event.getMagicData();
        SpellData castingSpell = magicData.getCastingSpell();
        if (castingSpell == null || castingSpell.getSpell() == null) return;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        float totalReduction = 0f;
        for (var inst : AffixHelper.getAffixes(mainHand).values()) {
            if (!inst.isValid()) continue;
            if (!(inst.affix().get() instanceof ManaCostAffix affix)) continue;
            totalReduction += affix.getReductionPercent(inst.getRarity(), inst.level());
        }
        if (totalReduction <= 0f) return;

        totalReduction = Math.min(totalReduction, 0.9f);
        float manaCost = event.getOldMana() - event.getNewMana();
        float reducedCost = manaCost * (1f - totalReduction);
        event.setNewMana(event.getOldMana() - reducedCost);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChangeMana_Return(ChangeManaEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getNewMana() >= event.getOldMana()) return;

        Player player = event.getEntity();
        MagicData magicData = event.getMagicData();
        SpellData castingSpell = magicData.getCastingSpell();
        if (castingSpell == null || castingSpell.getSpell() == null) return;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;
        float actualCost = event.getOldMana() - event.getNewMana();
        if (actualCost <= 0f) return;

        float totalReturn = 0f;
        for (var inst : AffixHelper.getAffixes(mainHand).values()) {
            if (!inst.isValid()) continue;
            if (!(inst.affix().get() instanceof ManaReturnAffix affix)) continue;
            float chance = affix.getChance(inst.getRarity(), inst.level());
            if (player.getRandom().nextFloat() < chance) {
                totalReturn += actualCost * affix.getReturnPercent(inst.getRarity(), inst.level());
            }
        }
        if (totalReturn <= 0f) return;

        event.setNewMana(Math.min(event.getNewMana() + totalReturn, event.getOldMana()));
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ACTIVE_CAST_LEVELS.clear();
        LAST_SPELL_DAMAGE_TARGET.clear();
        SpellEchoHandler.LAST_ECHO_TICK.clear();
        DelayedTaskScheduler.clear();
        SpellCastAffix.ACTIVE_TRIGGERS.clear();
    }

    public static void applyAllEffects(ItemStack stack, LivingEntity caster, LivingEntity target) {
        triggerAffixes.accept(stack, caster, target);
        checkGemBonus(stack, (gem, bonus) -> {
            switch (bonus.target) {
                case SPELL_DAMAGE_SELF, SPELL_HEAL_SELF -> bonus.applyEffect(gem, caster);
                case SPELL_DAMAGE_TARGET, SPELL_HEAL_TARGET -> bonus.applyEffect(gem, target);
            }
        });
    }

    public static void checkGemBonus(ItemStack itemStack, BiConsumer<GemInstance, SpellEffectBonus> processor) {
        if (itemStack.isEmpty()) return;
        LootCategory cat = LootCategory.forItem(itemStack);
        for (GemInstance g : SocketHelper.getGems(itemStack)) {
            if (!g.isValid()) continue;
            Gem gem = g.gem().get();
            gem.getBonus(cat, g.purity())
                    .filter(b -> b instanceof SpellEffectBonus)
                    .map(b -> (SpellEffectBonus) b)
                    .ifPresent(bonus -> processor.accept(g, bonus));
        }
    }
}