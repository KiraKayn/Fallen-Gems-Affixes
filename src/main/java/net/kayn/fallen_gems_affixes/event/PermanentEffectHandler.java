package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.PermanentEffectBonus;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.util.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PermanentEffectHandler implements IPermanentEffectHandler{
    private static final PermanentEffectHandler INSTANCE = new PermanentEffectHandler();
    // TODO: add a validate mechanic so there won't be invalid infinity potion effect
    private static boolean useTickEvent = false;
    private static boolean configLoaded = false;

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onModConfigEvent(ModConfigEvent event) {
        if (configLoaded) return;
        useTickEvent = ModConfig.PERMANENT_EFFECT_USE_TICK_EVENT.get();
        if (useTickEvent) {
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onTick);
        } else {
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onEntityEquipmentChange);
        }
        configLoaded = true;
    }

    private void onEntityEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        LOGGER.info("{}", player.getClass());
        EquipmentSlot slot = event.getSlot();
        ItemStack from = event.getFrom();
        onEquip(player, from, slot, Operation.REMOVE);
        ItemStack to = event.getTo();
        onEquip(player, to, slot, Operation.ADD);
    }

//    @SubscribeEvent(priority = EventPriority.HIGH)
//    public static void onMobEffectRemove(MobEffectEvent.Remove event) {
//        if (!(event.getEntity() instanceof Player player) || !(player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
//        MobEffect effect = event.getEffect();
//        LOGGER.info("effect: {}", effect);
//        for (ItemStack equipment : player.getAllSlots()) {
//            if (matches(equipment, effect)) {
//                event.setCanceled(true);
//            }
//        }
//    }

    private void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        var permanentEffects = collectPermanentEffects(player);
        if (permanentEffects.isEmpty()) return;
        permanentEffects.forEach((effect, amplifier) -> {
            player.addEffect(new MobEffectInstance(effect, amplifier));
        });
    }

    public static boolean isUseTickEvent() {
        return useTickEvent;
    }

    public static Map<MobEffect, Integer> collectPermanentEffects(LivingEntity entity) {
        Map<MobEffect, Integer> mobEffects = new HashMap<>();
        for(ItemStack equipment : entity.getAllSlots()) {
            checkGemBonus(equipment, (bonus, rarity) -> {
                mobEffects.put(bonus.getEffect(), bonus.getAmplifier(rarity));
            });
        }
        return mobEffects;
    }

    public static void checkGemBonus(ItemStack itemStack, BonusProcessor processor) {
        if (itemStack.isEmpty()) return;
        DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(itemStack);
        if (!rarityHolder.isBound()) return;
        LootCategory cat = LootCategory.forItem(itemStack);
        LootRarity rarity = rarityHolder.get();
        for (GemInstance g : SocketHelper.getGems(itemStack)) {
            if (!g.isValid()) continue;
            Gem gem = g.gem().get();
            gem.getBonus(cat, rarity)
                    .filter(b -> b instanceof PermanentEffectBonus)
                    .map(b -> (PermanentEffectBonus) b)
                    .ifPresent(bonus -> processor.accept(bonus, rarity));
        }
    }

    @FunctionalInterface
    public interface BonusProcessor {
        void accept(PermanentEffectBonus bonus, LootRarity rarity);
    }

    public static boolean matches(ItemStack itemStack, MobEffect effect) {
        AtomicBoolean result = new AtomicBoolean(false);
        checkGemBonus(itemStack, (bonus, rarity) -> {
            if (bonus.getEffect() == effect) {
                result.set(true);
            }
        });
        return result.get();
    }

//    public static void addOrRemoveEffectWithCache(LivingEntity entity, ItemStack itemStack) {
//        switch(operation) {
//            case ADD -> {
//                LOGGER.info("add effect");
//                checkGemBonus(itemStack, (bonus, rarity) -> {
//                    MobEffect effect = bonus.getEffect();
//                    int amplifier = bonus.getAmplifier(rarity);
//                    MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
//                    player.addEffect(inst);
//                });
//
//            }
//            case REMOVE -> {
//                LOGGER.info("remove effect");
//                checkGemBonus(itemStack, (bonus, rarity) -> {
//                    MobEffect effect = bonus.getEffect();
//                    int amplifier = bonus.getAmplifier(rarity);
//                    player.removeEffect(effect);
//                    if (map.containsPermanent(effect)) {
//                        player.addEffect(map.getLastPotentialEffectInst(effect));
//                    }
//                });
//            }
//        }
//    }

    private void onEquip(LivingEntity entity, @NotNull ItemStack itemStack, EquipmentSlot cSlot, Operation operation) {
        for (EquipmentSlot slot: LootCategory.forItem(itemStack).getSlots()) {
            if (cSlot == slot) {
                checkGemBonus(itemStack, (bonus, rarity) -> {
                    onEquipInner(entity, bonus.getEffect(), EquipmentSlotUtil.getVanillaWrapper(cSlot), operation, bonus.getAmplifier(rarity));
                });
                break;
            }
        }
    }

    private void onEquipInner(LivingEntity entity, MobEffect effect, EquipmentSlotWrapper slotWrapper, Operation operation, int amplifier) {
        if (!(entity.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
        map.setOperator(ProtectedMobEffectMap.EffectOperator.ON_EQUIP);
        switch(operation) {
            case ADD -> {
                MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
                entity.addEffect(inst);
            }
            case REMOVE -> {
                entity.removeEffect(effect);
            }
        }
        map.setOperator(ProtectedMobEffectMap.EffectOperator.EXTERNAL);
    }

    @Override
    public void addPermanentEffect(LivingEntity entity, EquipmentSlotWrapper slot, MobEffect effect, int amplifier) {
        if (!(entity.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
        try {
            map.initOperation(slot, ProtectedMobEffectMap.EffectOperator.ON_HANDLER);
            entity.addEffect(new MobEffectInstance(effect, amplifier));
            map.addPermanentEffect(slot, effect, amplifier);
        }
        catch (Exception e) {
            LOGGER.error("Failed to add PermanentEffect {}", effect.getDisplayName());
            e.printStackTrace();
        }
        finally {
            map.finalizeOperation();
        }
    }

    @Override
    public void removePermanentEffect(LivingEntity entity, EquipmentSlotWrapper slot, MobEffect effect, int amplifier) {
        if (!(entity.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
        try {
            map.initOperation(slot, ProtectedMobEffectMap.EffectOperator.ON_HANDLER);
            entity.removeEffect(effect);
            map.tryRemovePermanentEffect(slot, effect, amplifier);
        }
        catch (Exception e) {
            LOGGER.error("Failed to remove PermanentEffect {}", effect.getDisplayName());
            e.printStackTrace();
        }
        finally {
            map.finalizeOperation();
        }
    }

    @Override
    public void setEffectPermanent(ProtectedMobEffectMap<?> map, EquipmentSlotWrapper slot, MobEffect effect, int amplifier) {
        map.addPermanentEffect(slot, effect, amplifier);
    }

    @Override
    public void unsetEffectPermanent(ProtectedMobEffectMap<?> map, EquipmentSlotWrapper slot, MobEffect effect, int amplifier) {
        map.initOperation(slot, ProtectedMobEffectMap.EffectOperator.ON_HANDLER);
        map.tryRemovePermanentEffect(slot, effect, amplifier);
    }

    public enum Operation {
        ADD,
        REMOVE
    }
}