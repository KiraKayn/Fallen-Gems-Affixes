package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import net.kayn.fallen_gems_affixes.util.BossUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = "fallen_gems_affixes")
public class BossSlayerHandler {

    private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(
            BuiltInRegistries.ENTITY_TYPE.key(),
            ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "boss_slayer")
    );

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (!BossUtil.isBoss(target, BOSS_TAG)) return;

        ItemStack weapon = attacker.getMainHandItem();
        LootCategory category = LootCategory.forItem(weapon);

        SocketHelper.getGems(weapon).forEach(gemInstance -> {
            if (!gemInstance.isValid()) return;

            Purity purity = gemInstance.purity();
            gemInstance.gem().get().getBonus(category, purity).ifPresent(bonus -> {
                if (bonus instanceof BossSlayerBonus slayer && slayer.supports(purity)) {
                    double mod = slayer.values.get(purity);
                    event.setNewDamage((float) (event.getOriginalDamage() * (1.0 + mod)));
                }
            });
        });
    }
}