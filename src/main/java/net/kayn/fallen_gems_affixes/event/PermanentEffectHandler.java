package net.kayn.fallen_gems_affixes.event;

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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PermanentEffectHandler implements IPermanentEffectHandler {
    private static final PermanentEffectHandler INSTANCE = new PermanentEffectHandler();
    private static Map<UUID, ProtectedMobEffectMap<?>> tickEventProtectedMapWrapper = null;

    private static boolean useTickEvent = false;
    private static boolean configLoaded = false;

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Main logic on mod loading, set everything as intended.
     */
    @SubscribeEvent
    public static void onModConfigEvent(ModConfigEvent event) {
        if (configLoaded) return;
        useTickEvent = ModConfig.PERMANENT_EFFECT_USE_TICK_EVENT.get();
        if (useTickEvent) {
            if (tickEventProtectedMapWrapper == null) {
                tickEventProtectedMapWrapper = new HashMap<>();
            }
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onPlayerLogIn);
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onPlayerLogout);
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onTick);
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onEntityEquipmentChange);
        } else {
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onPlayerLogout);
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onEntityEquipmentChange);
        }
        configLoaded = true;
    }

    public static void createSoftProtectedMapFor(LivingEntity entity) {
        UUID uuid = entity.getUUID();
        if (tickEventProtectedMapWrapper.get(uuid) != null) return;
        tickEventProtectedMapWrapper.put(uuid, new ProtectedMobEffectMap<>(entity));
    }

    /**
     * Main logic in both implementations, refreshing Permanent Effect when player's equipment changes.
     */
    private void onEntityEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        EquipmentSlot slot = event.getSlot();
        ItemStack from = event.getFrom();
        onEquip(player, from, slot, Operation.REMOVE);
        ItemStack to = event.getTo();
        onEquip(player, to, slot, Operation.ADD);
    }

    /**
     * Main logic when use Tick Event to manage Permanent Effect.
     */
    private void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        var cached = tickEventProtectedMapWrapper.get(player.getUUID());
        try {
            for (ItemStack equipment : player.getAllSlots()) {
                for (EquipmentSlot slot : LootCategory.forItem(equipment).getSlots()) {
                    EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getVanillaWrapper(slot);
                    cached.initOperation(slotWrapper);
                    Set<MobEffect> effects = cached.getEffectsFromCache(slotWrapper);
                    if (effects == null) continue;
                    checkGemBonus(equipment, (bonus, rarity) -> {
                        MobEffect effect = bonus.getEffect();
                        if (!player.getActiveEffectsMap().containsKey(effect) && effects.contains(effect)) {
                            player.addEffect(new MobEffectInstance(effect, -1, bonus.getAmplifier(rarity)));
                            cached.addPermanentEffect(slotWrapper, effect, bonus.getAmplifier(rarity), true);
                        }
                    });
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cached.finalizeOperation();
        }
    }

    /**
     * Initialize the cached effects when player logged in.
     * This should be bounded with Tick Event.
     */
    private void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        tickEventProtectedMapWrapper.put(player.getUUID(), new ProtectedMobEffectMap<>(player));
    }

    /**
     * Remove cached permanent effects map when player leave the world.
     * This should be bounded with both Tick Event and Default.
     */
    private void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (useTickEvent) {
            tickEventProtectedMapWrapper.remove(player.getUUID());
        } else {
            if (!(player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
            try {
                var allPermanentEffects = collectPermanentEffects(player).keySet();
                map.initOperation(EquipmentSlotWrappers.NONE, ProtectedMobEffectMap.EffectOperator.ON_HANDLER);
                allPermanentEffects.forEach(player::removeEffect);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                map.finalizeOperation();
            }
        }
    }

    public static boolean isUseTickEvent() {
        return useTickEvent;
    }

    public ProtectedMobEffectMap<?> getTickEventProtectedMapWrapper(LivingEntity entity) {
        return tickEventProtectedMapWrapper.get(entity.getUUID());
    }

    public static Map<MobEffect, Integer> collectPermanentEffects(LivingEntity entity) {
        Map<MobEffect, Integer> mobEffects = new HashMap<>();
        int index = 0;
        for (ItemStack equipment : entity.getAllSlots()) {
            EquipmentSlot slot1 = EquipmentSlotUtil.slotFromAllSlotsIndex(index++);
            if (slot1 == null) continue;
            for (EquipmentSlot slot : LootCategory.forItem(equipment).getSlots()) {
                if (slot1 == slot) {
                    checkGemBonus(equipment, (bonus, rarity) -> {
                        mobEffects.put(bonus.getEffect(), bonus.getAmplifier(rarity));
                    });
                    break;
                }
            }
        }
        return mobEffects;
    }

    public static void checkGemBonus(ItemStack itemStack, BonusProcessor processor) {
        if (itemStack.isEmpty()) return;
        LootCategory cat = LootCategory.forItem(itemStack);
        for (GemInstance g : SocketHelper.getGems(itemStack)) {
            if (!g.isValid()) continue;
            DynamicHolder<LootRarity> rarityHolder = g.rarity();
            if (!rarityHolder.isBound()) continue;
            LootRarity rarity = rarityHolder.get();
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

    /**
     * Main logic when an Equipment Change Event triggers.
     */
    private void onEquip(LivingEntity entity, @NotNull ItemStack itemStack, EquipmentSlot cSlot, Operation operation) {
        for (EquipmentSlot slot : LootCategory.forItem(itemStack).getSlots()) {
            if (cSlot == slot) {
                if (useTickEvent) {
                    checkGemBonus(itemStack, (bonus, rarity) -> {
                        onEquipByTick(entity, bonus.getEffect(), EquipmentSlotUtil.getVanillaWrapper(cSlot), operation, bonus.getAmplifier(rarity));
                    });
                    return;
                }
                checkGemBonus(itemStack, (bonus, rarity) -> {
                    onEquipDefault(entity, bonus.getEffect(), EquipmentSlotUtil.getVanillaWrapper(cSlot), operation, bonus.getAmplifier(rarity));
                });
                return;
            }
        }
    }

    /**
     * Main logic when an Equipment Change Event triggers with Tick Event config.
     */
    private void onEquipByTick(LivingEntity entity, MobEffect effect, EquipmentSlotWrapper slotWrapper, Operation operation, int amplifier) {
        var map = tickEventProtectedMapWrapper.get(entity.getUUID());
        map.setOperator(ProtectedMobEffectMap.EffectOperator.ON_EQUIP);
        switch (operation) {
            case ADD -> {
                MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
                entity.addEffect(inst);
                map.addPermanentEffect(slotWrapper, effect, amplifier, true);
            }
            case REMOVE -> {
                entity.removeEffect(effect);
                map.tryRemovePermanentEffect(slotWrapper, effect, amplifier, true);
                if (map.containsPermanent(effect)) {
                    entity.addEffect(map.getLastPotentialEffectInst(effect));
                }
            }
        }
        map.setOperator(ProtectedMobEffectMap.EffectOperator.EXTERNAL);
    }

    /**
     * Main logic when an Equipment Change Event triggers with default config.
     */
    private void onEquipDefault(LivingEntity entity, MobEffect effect, EquipmentSlotWrapper slotWrapper, Operation operation, int amplifier) {
        if (!(entity.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
        map.setOperator(ProtectedMobEffectMap.EffectOperator.ON_EQUIP);
        switch (operation) {
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

    /**
     * A public method to add PermanentEffect to an entity.
     */
    @Override
    public void addPermanentEffect(LivingEntity entity, EquipmentSlotWrapper slot, MobEffect effect, int amplifier, boolean altCondition) {
        var map1 = entity.getActiveEffectsMap();
        if (useTickEvent) {
            map1 = tickEventProtectedMapWrapper.get(entity.getUUID());
        }
        if (!(map1 instanceof ProtectedMobEffectMap<?> map)) return;
        try {
            map.initOperation(slot, ProtectedMobEffectMap.EffectOperator.ON_HANDLER);
            entity.addEffect(new MobEffectInstance(effect, amplifier));
            map.addPermanentEffect(slot, effect, amplifier, altCondition);
        } catch (Exception e) {
            LOGGER.error("Failed to add PermanentEffect {}", effect.getDisplayName());
            e.printStackTrace();
        } finally {
            map.finalizeOperation();
        }
    }

    /**
     * A public method to remove PermanentEffect to an entity.
     */
    @Override
    public void removePermanentEffect(LivingEntity entity, EquipmentSlotWrapper slot, MobEffect effect, int amplifier, boolean altCondition) {
        var map1 = entity.getActiveEffectsMap();
        if (useTickEvent) {
            map1 = tickEventProtectedMapWrapper.get(entity.getUUID());
        }
        if (!(map1 instanceof ProtectedMobEffectMap<?> map)) return;
        try {
            map.initOperation(slot, ProtectedMobEffectMap.EffectOperator.ON_HANDLER);
            entity.removeEffect(effect);
            map.tryRemovePermanentEffect(slot, effect, amplifier, altCondition);
        } catch (Exception e) {
            LOGGER.error("Failed to remove PermanentEffect {}", effect.getDisplayName());
            e.printStackTrace();
        } finally {
            map.finalizeOperation();
        }
    }

    /**
     * This triggers when main hand slot changes.
     * This method is clientside only.
     * This method is for Default setting.
     */
    public static void onHotBarSelectedChange(Player player) {
        if (!(player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
        ItemStack itemStack = player.getMainHandItem();
        effectOperationBySlot(map, player, EquipmentSlotWrappers.MAIN_HAND, itemStack);
    }

    /**
     * Client only.
     */
    public static void effectOperationBySlot(ProtectedMobEffectMap<?> map, Player player, EquipmentSlotWrapper slotWrapper, ItemStack itemStack) {
        try {
            var cachedEffects = map.getEffectsFromCache(slotWrapper);
            map.initOperation(slotWrapper);
            if (cachedEffects != null) {
                // To not trigger concurrent exception
                for (MobEffect effect : Set.copyOf(cachedEffects)) {
                    player.removeEffectNoUpdate(effect);
                    if (map.containsPermanent(effect)) {
                        player.forceAddEffect(map.getLastPotentialEffectInst(effect), null);
                    }
                }
            }
            if (EquipmentSlotUtil.matchesSlot(itemStack, slotWrapper.getSlot())) {
                checkGemBonus(itemStack, (bonus, rarity) -> {
                    MobEffect effect = bonus.getEffect();
                    int amplifier = bonus.getAmplifier(rarity);
                    MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
                    player.forceAddEffect(inst, null);
                    map.setLastEffectsProvider(itemStack);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            map.finalizeOperation();
        }
    }

    @Override
    public void setEffectPermanent(ProtectedMobEffectMap<?> map, EquipmentSlotWrapper slot, MobEffect effect, int amplifier, boolean altCondition) {
        map.addPermanentEffect(slot, effect, amplifier, altCondition);
    }

    @Override
    public void unsetEffectPermanent(ProtectedMobEffectMap<?> map, EquipmentSlotWrapper slot, MobEffect effect, int amplifier, boolean altCondition) {
        map.tryRemovePermanentEffect(slot, effect, amplifier, altCondition);
    }

    public enum Operation {
        ADD,
        REMOVE
    }
}