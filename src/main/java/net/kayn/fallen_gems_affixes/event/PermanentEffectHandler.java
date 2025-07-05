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
import net.kayn.fallen_gems_affixes.util.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID)
public class PermanentEffectHandler {
    // TODO: once the test passes, delete the LOGGER
    private static final Logger LOGGER = LogManager.getLogger();
    @SubscribeEvent
    public static void onEntityEquipmentChange(LivingEquipmentChangeEvent event) {
        // TODO: exclude some hand item change to not trigger addOrRemoveEffect
        if (!(event.getEntity() instanceof Player player)) return;
        LOGGER.info("{}", player.getClass());
        EquipmentSlot slot = event.getSlot();
        ItemStack from = event.getFrom();
        addOrRemoveEffect(player, from, slot, Operation.REMOVE);
        ItemStack to = event.getTo();
        addOrRemoveEffect(player, to, slot, Operation.ADD);
        LOGGER.info("from: {}, to: {}", from, to);
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

    public static Set<MobEffect> collectPermanentEffects(LivingEntity entity) {
        Set<MobEffect> mobEffects = new HashSet<>();
        for(ItemStack equipment : entity.getAllSlots()) {
            checkGemBonus(equipment, (bonus, rarity) -> {
                mobEffects.add(bonus.getEffect());
            });
        }
        return mobEffects;
    }

    public static void checkGemBonus(ItemStack itemStack, BonusProcessor processor) {
        LOGGER.info("into checkGemBonus");
        if (itemStack.isEmpty()) return;
        DynamicHolder<LootRarity> rarityHolder = AffixHelper.getRarity(itemStack);
        if (!rarityHolder.isBound()) return;
        LOGGER.info("into checkGemBonus inner {}, {}", rarityHolder, itemStack);
        LootCategory cat = LootCategory.forItem(itemStack);
        LootRarity rarity = rarityHolder.get();
        for (GemInstance g : SocketHelper.getGems(itemStack)) {
            LOGGER.info("into gem inner {}", g);
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
        LOGGER.info("into matches");
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

    public static void addOrRemoveEffect(LivingEntity entity, @NotNull ItemStack itemStack, EquipmentSlot cSlot, Operation operation) {
        LOGGER.info("into addOrRemoveEffect");
        for (EquipmentSlot slot: LootCategory.forItem(itemStack).getSlots()) {
            LOGGER.info("effectSlot {}, slot {}", cSlot, slot);
            if (cSlot == slot) {
                checkGemBonus(itemStack, (bonus, rarity) -> {
                    addOrRemoveEffectInner(entity, bonus.getEffect(), EquipmentSlotUtil.getVanillaWrapper(cSlot), operation, bonus.getAmplifier(rarity));
                });
                break;
            }
        }
    }

    private static void addOrRemoveEffectInner(LivingEntity entity, MobEffect effect, EquipmentSlotWrapper slotWrapper, Operation operation, int amplifier) {
        if (!(entity.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
        map.setOperator(ProtectedMobEffectMap.EffectOperator.ON_EQUIP);
        switch(operation) {
            case ADD -> {
                LOGGER.info("add effect");
                MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
                entity.addEffect(inst);
                map.addPermanentEffect(slotWrapper, effect, amplifier);
            }
            case REMOVE -> {
                LOGGER.info("remove effect");
                entity.removeEffect(effect);
                if (map.containsPermanent(effect)) {
                    entity.addEffect(map.getLastPotentialEffectInst(effect));
                } else {
                    map.tryRemovePermanentEffect(slotWrapper, effect, amplifier);
                }
            }
        }
        map.setOperator(ProtectedMobEffectMap.EffectOperator.EXTERNAL);
    }

    public void addPermanentEffect(ProtectedMobEffectMap<?> map, EquipmentSlotWrapper slot, MobEffect effect, int amplifier) {
        try {
            map.initOperation(slot, ProtectedMobEffectMap.EffectOperator.ON_HANDLER);
            map.addPermanentEffect(slot, effect, amplifier);
        } finally {
            map.finalizeOperation();
        }
    }

    public void removePermanentEffect(ProtectedMobEffectMap<?> map, EquipmentSlotWrapper slot, MobEffect effect, int amplifier) {
        try {
            map.initOperation(slot, ProtectedMobEffectMap.EffectOperator.ON_HANDLER);
            map.tryRemovePermanentEffect(slot, effect, amplifier);
        } finally {
            map.finalizeOperation();
        }
    }

    public enum Operation {
        ADD,
        REMOVE
    }
}