package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;
import java.util.Set;

public class FortifyAffix extends Affix /* implements EntityAffixBehavior */ {

    public static final ResourceLocation FORTIFY_ID =
            ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "fortify_armor");
    public static final ResourceLocation ENTITY_FORTIFY_ID =
            ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "entity_fortify_armor");

    public static final String FORTIFY_NAME = "fga:fortify_armor";
    public static final String ENTITY_FORTIFY_NAME = "fga:entity_fortify_armor";

    public static final Codec<FortifyAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    affixDef(),
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, FortifyAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory>             types;

    public FortifyAffix(AffixDefinition def, Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(def);
        this.values = values;
        this.types  = types;
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        event.getEntity().getPersistentData().remove(FORTIFY_NAME);
    }

    // player tick
    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide()) return;
        if (entity.tickCount % 5 != 0) return;

        Vec3 lastPosition = entity.position();
        DelayedTaskScheduler.schedule(entity.level(), 1, () -> {
            if (!entity.isAlive()) return;
            AttributeInstance armorAttr = entity.getAttribute(Attributes.ARMOR);
            if (armorAttr == null) return;
            armorAttr.removeModifier(FORTIFY_ID);

            CompoundTag tag = entity.getPersistentData();
            float cachedBonus;
            if (!tag.contains(FORTIFY_NAME)) {
                cachedBonus = computeBonus(entity);
                tag.putFloat(FORTIFY_NAME, cachedBonus);
            } else {
                cachedBonus = tag.getFloat(FORTIFY_NAME);
            }
            if (cachedBonus <= 0f) return;

            if (entity.position().distanceToSqr(lastPosition) < 0.001) {
                double armorWithoutBonus = armorAttr.getValue();
                armorAttr.addTransientModifier(new AttributeModifier(
                        FORTIFY_ID,
                        armorWithoutBonus * cachedBonus,
                        AttributeModifier.Operation.ADD_VALUE));
            }
        });
    }

    private static float computeBonus(LivingEntity entity) {
        float total = 0f;
        for (ItemStack stack : entity.getAllSlots()) {
            for (AffixInstance inst : AffixHelper.getAffixes(stack).values()) {
                if (!inst.isValid()) continue;
                if (!(inst.affix().get() instanceof FortifyAffix affix)) continue;
                total += affix.values.get(inst.getRarity()).get(inst.level());
            }
        }
        return total;
    }

    // TODO: re-enable when EntityAffixBehavior is ported
    // @Override
    // public void tickEntityAffix(LivingEntity entity, LootRarity rarity, float level) {
    //     Vec3 lastPos = entity.position();
    //     DelayedTaskScheduler.schedule(entity.level(), 1, () -> {
    //         if (!entity.isAlive()) return;
    //         AttributeInstance armorAttr = entity.getAttribute(Attributes.ARMOR);
    //         if (armorAttr == null) return;
    //         armorAttr.removeModifier(ENTITY_FORTIFY_ID);
    //         float bonus = values.get(rarity).get(level);
    //         if (bonus <= 0f) return;
    //         if (entity.position().distanceToSqr(lastPos) < 0.001) {
    //             double base = armorAttr.getValue();
    //             armorAttr.addTransientModifier(new AttributeModifier(
    //                     ENTITY_FORTIFY_ID, base * bonus,
    //                     AttributeModifier.Operation.ADD_VALUE));
    //         }
    //     });
    // }
    //
    // @Override
    // public int tickInterval() { return 5; }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable(
                "affix.fallen_gems_affixes.fortify.desc",
                Affix.fmt(values.get(inst.getRarity()).get(inst.level()) * 100f)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() { return CODEC; }
}