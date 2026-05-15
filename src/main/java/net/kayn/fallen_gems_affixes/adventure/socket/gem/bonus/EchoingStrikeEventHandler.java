package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class EchoingStrikeEventHandler {

    public static final Map<UUID, Long> LAST_ECHO_TICK = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> IS_LOCK = new ConcurrentHashMap<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        if (IS_LOCK.getOrDefault(player.getUUID(), false)) return;

        LivingEntity target = event.getEntity();
        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return;

        SocketedGems socketedGems = SocketHelper.getGems(weapon);

        for (GemInstance gi : socketedGems.gems()) {
            if (!gi.isValid()) continue;

            gi.gem().get().getBonus(gi.cat(), gi.rarity().get()).ifPresent(bonus -> {
                if (bonus instanceof EchoingStrikeBonus echoBonus) {
                    LootRarity rarity = gi.rarity().get();

                    if (echoBonus.supports(rarity)) {
                        long now = player.level().getGameTime();
                        long lastEcho = LAST_ECHO_TICK.getOrDefault(player.getUUID(), -1000L);
                        int delayTicks = echoBonus.getDelayTicks(rarity);

                        if (now - lastEcho < delayTicks) return;

                        float damageMultiplier = echoBonus.getDamageMultiplier(rarity);
                        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        float echoDamage = baseDamage * damageMultiplier;

                        LAST_ECHO_TICK.put(player.getUUID(), now);

                        DelayedTaskScheduler.schedule(player.level(), delayTicks, () -> {
                            if (!target.isAlive() || !player.isAlive()) return;

                            IS_LOCK.put(player.getUUID(), true);
                            try {
                                target.invulnerableTime = 0;

                                player.swing(InteractionHand.MAIN_HAND, true);

                                if (player instanceof ServerPlayer sp) {
                                    sp.connection.send(new ClientboundLevelEventPacket(2001, target.blockPosition(), 0, false));
                                }

                                target.hurt(player.damageSources().playerAttack(player), echoDamage);
                                LAST_ECHO_TICK.put(player.getUUID(), player.level().getGameTime());
                            } finally {
                                IS_LOCK.put(player.getUUID(), false);
                            }
                        });
                    }
                }
            });

            break;
        }
    }
}