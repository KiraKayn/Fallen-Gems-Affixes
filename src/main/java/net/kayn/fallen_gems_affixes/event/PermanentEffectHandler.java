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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID)
public class PermanentEffectHandler {
    protected static Boolean flag = false;
    private static final Logger LOGGER = LogManager.getLogger();
    @SubscribeEvent
    public static void onEntityEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack from = event.getFrom();
        addOrRemoveEffect(player, from, Operation.REMOVE);
        ItemStack to = event.getTo();
        addOrRemoveEffect(player, to, Operation.ADD);
        LOGGER.info("from: {}, to: {}", from, to);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onMobEffectRemove(MobEffectEvent.Remove event) {
        if (!(event.getEntity() instanceof Player player) || flag) return;
        MobEffect effect = event.getEffect();
        LOGGER.info("effect: {}", effect);
        for (ItemStack equipment : player.getAllSlots()) {
            if (matches(player, equipment, effect)) {
                event.setCanceled(true);
            }
        }
    }

    private static void checkGemBonus(Player player, ItemStack itemStack, BonusProcessor processor) {
        LOGGER.info("into checkGemBonus");
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
    private interface BonusProcessor {
        void accept(PermanentEffectBonus bonus, LootRarity rarity);
    }

    private static boolean matches(Player player, ItemStack itemStack, MobEffect effect) {
        LOGGER.info("into matches");
        AtomicBoolean result = new AtomicBoolean(false);
        checkGemBonus(player, itemStack, (bonus, rarity) -> {
            if (bonus.getEffect() == effect) {
                result.set(true);
            }
        });
        return result.get();
    }

    private static void addOrRemoveEffect(Player player, ItemStack itemStack, Operation operation) {
        LOGGER.info("into addOrRemoveEffect");
        checkGemBonus(player, itemStack, (bonus, rarity) -> {
            addOrRemoveEffectInner(player, bonus.getEffect(), operation, bonus.getAmplifier(rarity));
        });
    }

    private static void addOrRemoveEffectInner(Player player, MobEffect effect, Operation operation, int amplifier) {
        flag = true;
        switch(operation) {
            case ADD -> {
                LOGGER.info("add effect");
                player.addEffect(new MobEffectInstance(effect, Integer.MAX_VALUE, amplifier));
                flag = false;
            }
            case REMOVE -> {
                LOGGER.info("remove effect");
                player.removeEffect(effect);
                flag = false;
            }
        }
    }

    public enum Operation {
        ADD,
        REMOVE
    }
}