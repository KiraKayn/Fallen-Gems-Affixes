package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.PermanentEffectBonus;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.util.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.common.inventory.CurioSlot;
import top.theillusivec4.curios.common.inventory.container.CuriosContainer;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID)
public class PermanentEffectHandler implements IPermanentEffectHandler {
    public static final PermanentEffectHandler INSTANCE = new PermanentEffectHandler();
    private static Map<UUID, ProtectedMobEffectMap<LivingEntity>> tickEventProtectedMapWrapper = null;

    private static boolean useTickEvent = false;
    private static boolean configLoaded = false;
    private static boolean curiosLoaded;

    public static final BiConsumer<LivingEntity, ProtectedMobEffectMap<LivingEntity>> tickFunctionCurio;
    public static final TriConsumer<LivingEntity, ItemStack, Object> onTakeCurio;
    public static final Consumer<Object> onInitializeCurioContents;
    public static final BiConsumer<LivingEntity, Map<Holder<MobEffect>, Integer>> onCollectCurioEffects;

    static {
        if (ModList.get().isLoaded("curios")) {
            curiosLoaded = true;

            tickFunctionCurio = (entity, cached) -> {
                ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
                if (handler != null) {
                    for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                        String identifier = entry.getKey();
                        ICurioStacksHandler slotHandler = entry.getValue();
                        try {
                            for (int i = 0; i < slotHandler.getSlots(); i++) {
                                ItemStack equippedStack = slotHandler.getStacks().getStackInSlot(i);
                                if (equippedStack.isEmpty()) continue;
                                EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getCurioWrapper(entity, equippedStack, identifier);
                                if (slotWrapper == EquipmentSlotWrappers.NONE) continue;
                                cached.initOperation(slotWrapper);
                                Set<Holder<MobEffect>> effects = cached.getEffectsFromCache(slotWrapper);
                                if (effects == null) continue;
                                checkGemBonus(equippedStack, (bonus, rarity) -> {
                                    Holder<MobEffect> effect = bonus.getEffect();
                                    if (!entity.getActiveEffectsMap().containsKey(effect) && effects.contains(effect)) {
                                        entity.addEffect(new MobEffectInstance(effect, -1, bonus.getAmplifier(rarity)));
                                        cached.addPermanentEffect(slotWrapper, effect, bonus.getAmplifier(rarity), true);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            cached.finalizeOperation();
                        }
                    }
                } else return;
            };

            onTakeCurio = (entity, pStack, object) -> {
                if (!(object instanceof CurioSlot curioSlot)) return;
                String identifier = curioSlot.getIdentifier();
                EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getCurioWrapper(entity, pStack, identifier);
                if (slotWrapper == EquipmentSlotWrappers.NONE) return;
                if (!(entity instanceof LocalPlayer player)) return;
                if (!(player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
                if (pStack.isEmpty()) return;
                try {
                    map.initOperation(slotWrapper);
                    checkGemBonus(pStack, (bonus, rarity) -> {
                        Holder<MobEffect> effect = bonus.getEffect();
                        player.removeEffectNoUpdate(effect);
                        if (map.containsPermanent(effect)) {
                            player.forceAddEffect(map.getLastPotentialEffectInst(effect), null);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    map.finalizeOperation();
                }
            };

            onInitializeCurioContents = (object) -> {
                if (!(object instanceof CuriosContainer container)) return;
                Player player = container.player;
                if (!(player.getActiveEffectsMap() instanceof ProtectedMobEffectMap<?> map)) return;
                List<Slot> slots = container.slots;
                for (int i = 46; i < slots.size(); i++) {
                    Slot slot = slots.get(i);
                    ItemStack stack = slot.getItem();
                    if (stack.isEmpty()) continue;
                    if (slots.get(i) instanceof CurioSlot curioSlot) {
                        EquipmentSlotWrapper slotWrapper = EquipmentSlotUtil.getCurioWrapper(player, stack, curioSlot.getIdentifier());
                        if (slotWrapper == EquipmentSlotWrappers.NONE) continue;
                        try {
                            map.initOperation(slotWrapper, ProtectedMobEffectMap.EffectOperator.ON_INIT);

                            if (map.getLastEffectsProvider() != stack && EquipmentSlotUtil.simpleMatchesCurioSlot(player, stack, curioSlot.getIdentifier())) {
                                checkGemBonus(stack, (bonus, rarity) -> {
                                    Holder<MobEffect> effect = bonus.getEffect();
                                    int amplifier = bonus.getAmplifier(rarity);
                                    MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
                                    player.forceAddEffect(inst, null);
                                    map.setLastEffectsProvider(stack);
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            map.finalizeOperation();
                        }
                    }
                }
            };

            onCollectCurioEffects = (entity, mobEffects) -> {
                ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
                if (handler != null) {
                    for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                        ICurioStacksHandler slotHandler = entry.getValue();
                        for (int i = 0; i < slotHandler.getSlots(); i++) {
                            ItemStack equippedStack = slotHandler.getStacks().getStackInSlot(i);
                            if (equippedStack.isEmpty()) continue;
                            if (EquipmentSlotUtil.simpleMatchesCurioSlot(entity, equippedStack, entry.getKey())) {
                                checkGemBonus(equippedStack, (bonus, rarity) -> {
                                    mobEffects.put(bonus.getEffect(), bonus.getAmplifier(rarity));
                                });
                            }
                        }
                    }
                } else return;
            };
        } else {
            tickFunctionCurio = (a, b) -> {};
            onTakeCurio = (a, b, c) -> {};
            onInitializeCurioContents = (a) -> {};
            onCollectCurioEffects = (a, b) -> {};
        }
    }

    public void bootstrap() {
        if (ModList.get().isLoaded("curios")) {
            Consumer<CurioChangeEvent> onEntityCurioChange = event -> {
                if (!(event.getEntity() instanceof Player player)) return;
                String slot = event.getIdentifier();
                ItemStack from = event.getFrom();
                onCurioEquip(player, from, slot, Operation.REMOVE);
                ItemStack to = event.getTo();
                onCurioEquip(player, to, slot, Operation.ADD);
            };
            NeoForge.EVENT_BUS.addListener(onEntityCurioChange);
        }
    }

    private static final Logger LOGGER = LogManager.getLogger();

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void apply(A a, B b, C c);
    }

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
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onPlayerLogIn);
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onPlayerLogout);
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onEntityEquipmentChange);
        } else {
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onPlayerLogout);
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, INSTANCE::onEntityEquipmentChange);
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

    private void onCurioEquip(LivingEntity entity, ItemStack itemStack, String slot, Operation operation) {
        if (EquipmentSlotUtil.simpleMatchesCurioSlot(entity, itemStack, slot)) {
            if (useTickEvent) {
                checkGemBonus(itemStack, (bonus, rarity) -> {
                    onEquipByTick(entity, bonus.getEffect(), EquipmentSlotUtil.getCurioWrapper(slot), operation, bonus.getAmplifier(rarity));
                });
                return;
            }
            checkGemBonus(itemStack, (bonus, rarity) -> {
                onEquipDefault(entity, bonus.getEffect(), EquipmentSlotUtil.getCurioWrapper(slot), operation, bonus.getAmplifier(rarity));
            });
            return;
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

    public static boolean isCuriosLoaded() {
        return curiosLoaded;
    }

    public static ProtectedMobEffectMap<LivingEntity> getTickEventProtectedMapWrapper(LivingEntity entity) {
        return tickEventProtectedMapWrapper.get(entity.getUUID());
    }

    public static Map<Holder<MobEffect>, Integer> collectPermanentEffects(LivingEntity entity) {
        Map<Holder<MobEffect>, Integer> mobEffects = new HashMap<>();
        int index = 0;
        for (ItemStack equipment : entity.getAllSlots()) {
            EquipmentSlot slot1 = EquipmentSlotUtil.slotFromAllSlotsIndex(index++);
            if (slot1 == null) continue;
            if (EquipmentSlotUtil.simpleMatchesSlot(equipment, slot1)) {
                checkGemBonus(equipment, (bonus, rarity) -> {
                    mobEffects.put(bonus.getEffect(), bonus.getAmplifier(rarity));
                });
            }
        }
        if (curiosLoaded) {
            onCollectCurioEffects.accept(entity, mobEffects);
        }
        return mobEffects;
    }

    public static void checkGemBonus(ItemStack itemStack, BonusProcessor processor) {
        if (itemStack.isEmpty()) return;
        LootCategory cat = LootCategory.forItem(itemStack);
        for (GemInstance g : SocketHelper.getGems(itemStack)) {
            if (!g.isValid()) continue;
            Purity purity = g.purity();
            Gem gem = g.gem().get();
            gem.getBonus(cat, purity)
                    .filter(b -> b instanceof PermanentEffectBonus)
                    .map(b -> (PermanentEffectBonus) b)
                    .ifPresent(bonus -> processor.accept(bonus, purity));
        }
    }

    @FunctionalInterface
    public interface BonusProcessor {
        void accept(PermanentEffectBonus bonus, Purity purity);
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
        if (EquipmentSlotUtil.simpleMatchesSlot(itemStack, cSlot)) {
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

    /**
     * Main logic when an Equipment Change Event triggers with Tick Event config.
     */
    private void onEquipByTick(LivingEntity entity, Holder<MobEffect> effect, EquipmentSlotWrapper slotWrapper, Operation operation, int amplifier) {
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
    private void onEquipDefault(LivingEntity entity, Holder<MobEffect> effect, EquipmentSlotWrapper slotWrapper, Operation operation, int amplifier) {
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
    public void addPermanentEffect(LivingEntity entity, EquipmentSlotWrapper slot, Holder<MobEffect> effect, int amplifier, boolean altCondition) {
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
            LOGGER.error("Failed to add PermanentEffect {}", effect.value().getDisplayName());
            e.printStackTrace();
        } finally {
            map.finalizeOperation();
        }
    }

    /**
     * A public method to remove PermanentEffect to an entity.
     */
    @Override
    public void removePermanentEffect(LivingEntity entity, EquipmentSlotWrapper slot, Holder<MobEffect> effect, int amplifier, boolean altCondition) {
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
            LOGGER.error("Failed to remove PermanentEffect {}", effect.value().getDisplayName());
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
                for (Holder<MobEffect> effect : Set.copyOf(cachedEffects)) {
                    player.removeEffectNoUpdate(effect);
                    if (map.containsPermanent(effect)) {
                        player.forceAddEffect(map.getLastPotentialEffectInst(effect), null);
                    }
                }
            }
            if (EquipmentSlotUtil.matchesSlot(itemStack, slotWrapper)) {
                checkGemBonus(itemStack, (bonus, rarity) -> {
                    Holder<MobEffect> effect = bonus.getEffect();
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
    public void setEffectPermanent(ProtectedMobEffectMap<?> map, EquipmentSlotWrapper slot, Holder<MobEffect> effect, int amplifier, boolean altCondition) {
        map.addPermanentEffect(slot, effect, amplifier, altCondition);
    }

    @Override
    public void unsetEffectPermanent(ProtectedMobEffectMap<?> map, EquipmentSlotWrapper slot, Holder<MobEffect> effect, int amplifier, boolean altCondition) {
        map.tryRemovePermanentEffect(slot, effect, amplifier, altCondition);
    }

    public enum Operation {
        ADD,
        REMOVE
    }
}