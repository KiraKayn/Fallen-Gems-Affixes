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
public class KeresSpellPowerPatch {

    private static final ResourceLocation BLOOD_SPELL_POWER_ID = new ResourceLocation("irons_spellbooks", "blood_spell_power");
    private static final Lazy<Attribute> BLOOD_SPELL_POWER = Lazy.of(() -> ForgeRegistries.ATTRIBUTES.getValue(BLOOD_SPELL_POWER_ID));

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player)) return;

        ItemStack held = player.getMainHandItem();
        if (!held.is(CSItems.KERES.get())) return;

        Attribute bloodSpellAttr = BLOOD_SPELL_POWER.get();
        if (bloodSpellAttr == null) return;

        AttributeInstance bloodSpellInstance = player.getAttribute(bloodSpellAttr);
        if (bloodSpellInstance == null) return;

        double bloodSpellPower = bloodSpellInstance.getValue();
        if (bloodSpellPower <= 0) return;

        float original = event.getAmount();
        float scaled = original + (original * (float) bloodSpellPower);
        event.setAmount(scaled);
    }
}