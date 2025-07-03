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
import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.LinkedList;
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

    private static void addOrRemoveEffect(LivingEntity entity, ItemStack itemStack, EquipmentSlot cSlot, Operation operation) {
        LOGGER.info("into addOrRemoveEffect");
        for (EquipmentSlot slot: LootCategory.forItem(itemStack).getSlots()) {
            LOGGER.info("effectSlot {}, slot {}", cSlot, slot);
            if (cSlot == slot) {
                checkGemBonus(itemStack, (bonus, rarity) -> {
                    addOrRemoveEffectInner(entity, bonus.getEffect(), operation, bonus.getAmplifier(rarity));
                });
                break;
            }
        }
    }

    private static void addOrRemoveEffectInner(LivingEntity entity, MobEffect effect, Operation operation, int amplifier) {
        if (!(entity.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
        map.setRemover(ProtectedMobEffectMap.EffectRemover.ON_EQUIP);
        var currentEffectsMap = entity.getActiveEffectsMap();
        switch(operation) {
            case ADD -> {
                LOGGER.info("add effect");
                MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
                entity.addEffect(inst);
                map.addPermanentEffect(inst);
            }
            case REMOVE -> {
                LOGGER.info("remove effect");
                entity.removeEffect(effect);
                map.removePermanentEffect(entity.getActiveEffectsMap().get(effect));
            }
        }
        map.setRemover(ProtectedMobEffectMap.EffectRemover.EXTERNAL);
    }

    public enum Operation {
        ADD,
        REMOVE
    }
}