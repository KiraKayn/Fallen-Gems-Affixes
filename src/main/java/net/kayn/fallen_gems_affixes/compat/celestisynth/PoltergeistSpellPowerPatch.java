package net.kayn.fallen_gems_affixes.compat.celestisynth;

import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class PoltergeistSpellPowerPatch {

    private static final boolean HAS_CELESTISYNTH = ModList.get().isLoaded("celestisynth");
    private static final boolean HAS_IRONS = ModList.get().isLoaded("irons_spellbooks");

    private static final ResourceLocation POLTERGEIST_ID = ResourceLocation.fromNamespaceAndPath("celestisynth", "poltergeist");
    private static final ResourceLocation ELDRITCH_SPELL_POWER_ID = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "eldritch_spell_power");

    private static Holder<Attribute> eldritchSpellPower = null;
    private static boolean lookedUp = false;

    private static Holder<Attribute> getEldritchSpellPower() {
        if (!lookedUp) {
            lookedUp = true;
            if (HAS_IRONS) eldritchSpellPower = BuiltInRegistries.ATTRIBUTE.getHolder(ELDRITCH_SPELL_POWER_ID).orElse(null);
        }
        return eldritchSpellPower;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!ModConfig.ENABLE_SPELL_POWER_PATCH.get()) return;
        if (!HAS_CELESTISYNTH || !HAS_IRONS) return;

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player)) return;

        ItemStack held = player.getMainHandItem();
        if (!held.is(BuiltInRegistries.ITEM.get(POLTERGEIST_ID))) return;

        Holder<Attribute> attr = getEldritchSpellPower();
        if (attr == null) return;

        AttributeInstance instance = player.getAttribute(attr);
        if (instance == null) return;

        double value = instance.getValue();
        if (value <= 0) return;

        event.setAmount(event.getAmount() + (event.getAmount() * (float) value));
    }
}