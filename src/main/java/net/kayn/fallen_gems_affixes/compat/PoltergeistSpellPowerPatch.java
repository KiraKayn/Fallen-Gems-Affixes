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
public class PoltergeistSpellPowerPatch {

    private static final ResourceLocation ELDRITCH_SPELL_POWER_ID = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "eldritch_spell_power");
    private static final Lazy<Attribute> ELDRITCH_SPELL_POWER = Lazy.of(() -> ForgeRegistries.ATTRIBUTES.getValue(ELDRITCH_SPELL_POWER_ID));

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player)) return;

        ItemStack held = player.getMainHandItem();
        if (!held.is(CSItems.POLTERGEIST.get())) return;

        Attribute eldritchSpellAttr = ELDRITCH_SPELL_POWER.get();
        if (eldritchSpellAttr == null) return;

        AttributeInstance eldritchSpellInstance = player.getAttribute(eldritchSpellAttr);
        if (eldritchSpellInstance == null) return;

        double eldritchSpellPower = eldritchSpellInstance.getValue();
        if (eldritchSpellPower <= 0) return;

        float original = event.getAmount();
        float scaled = original + (original * (float) eldritchSpellPower);
        event.setAmount(scaled);
    }
}