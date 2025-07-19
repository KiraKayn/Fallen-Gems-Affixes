package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod.EventBusSubscriber(modid = "fallen_gems_affixes")
public class BossSlayerHandler {

    private static final Set<UUID> TAGGED_BOSSES = new HashSet<>();

    private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(
            ForgeRegistries.ENTITY_TYPES.getRegistryKey(),
            new ResourceLocation("fallen_gems_affixes", "boss_slayer")
    );

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity living && living.getType().is(BOSS_TAG)) {
            TAGGED_BOSSES.add(living.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (!TAGGED_BOSSES.contains(target.getUUID())) return;

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        ItemStack weapon = attacker.getMainHandItem();
        if (!(weapon.getItem() instanceof GemItem)) return;

        GemInstance gemInstance = GemInstance.unsocketed(weapon);
        if (!gemInstance.isValidUnsocketed()) return;

        LootRarity rarity = gemInstance.rarity().get();
        Gem gem = gemInstance.gem().get();

        List<GemBonus> bonuses = gem.getBonuses();
        for (GemBonus bonus : bonuses) {
            if (bonus instanceof BossSlayerBonus slayer && slayer.supports(rarity)) {
                double mod = slayer.values.get(rarity).get(0);
                event.setAmount(event.getAmount() * (1.0F + (float) mod));
                break;
            }
        }
    }
}