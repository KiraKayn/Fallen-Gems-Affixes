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
public class FrostboundSpellPowerPatch {

    private static final ResourceLocation ICE_SPELL_POWER_ID = new ResourceLocation("irons_spellbooks", "ice_spell_power");
    private static final Lazy<Attribute> ICE_SPELL_POWER = Lazy.of(() -> ForgeRegistries.ATTRIBUTES.getValue(ICE_SPELL_POWER_ID));

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player)) return;

        ItemStack held = player.getMainHandItem();
        if (!held.is(CSItems.FROSTBOUND.get())) return;

        Attribute iceSpellAttr = ICE_SPELL_POWER.get();
        if (iceSpellAttr == null) return;

        AttributeInstance iceSpellInstance = player.getAttribute(iceSpellAttr);
        if (iceSpellInstance == null) return;

        double iceSpellPower = iceSpellInstance.getValue();
        if (iceSpellPower <= 0) return;

        float original = event.getAmount();
        float scaled = original + (original * (float) iceSpellPower);
        event.setAmount(scaled);
    }
}