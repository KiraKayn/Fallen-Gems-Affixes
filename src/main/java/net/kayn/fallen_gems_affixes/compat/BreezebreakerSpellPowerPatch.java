package net.kayn.fallen_gems_affixes.compat;

import com.aqutheseal.celestisynth.common.registry.CSItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class BreezebreakerSpellPowerPatch {

    private static final ResourceLocation EVOCATION_SPELL_POWER_ID = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "evocation_spell_power");
    private static final Lazy<Attribute> EVOCATION_SPELL_POWER = Lazy.of(() -> ForgeRegistries.ATTRIBUTES.getValue(EVOCATION_SPELL_POWER_ID));

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player)) return;

        ItemStack held = player.getMainHandItem();
        if (!held.is(CSItems.BREEZEBREAKER.get())) return;

        Attribute evocationSpellAttr = EVOCATION_SPELL_POWER.get();
        if (evocationSpellAttr == null) return;

        AttributeInstance evocationSpellInstance = player.getAttribute(evocationSpellAttr);
        if (evocationSpellInstance == null) return;

        double evocationSpellPower = evocationSpellInstance.getValue();
        if (evocationSpellPower <= 0) return;

        float original = event.getAmount();
        float scaled = original + (original * (float) evocationSpellPower);
        event.setAmount(scaled);
    }
}