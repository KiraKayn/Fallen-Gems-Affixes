package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.*;

public class AdaptiveSpellPowerAffix extends AttributeAffix {

    public static final Codec<AdaptiveSpellPowerAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(a -> a.attribute),
                    PlaceboCodecs.enumCodec(AttributeModifier.Operation.class).fieldOf("operation").forGetter(a -> a.operation),
                    GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types),
                    ResourceLocation.CODEC.fieldOf("school").forGetter(a -> a.schoolId))
            .apply(inst, AdaptiveSpellPowerAffix::new));

    protected final ResourceLocation schoolId;
    protected final SchoolType school;

    // Prevent infinite recursion
    private static final ThreadLocal<Boolean> callGuard = ThreadLocal.withInitial(() -> false);

    public AdaptiveSpellPowerAffix(Attribute attr, AttributeModifier.Operation op, Map<LootRarity, StepFunction> values, Set<LootCategory> types, ResourceLocation schoolId) {
        super(attr, op, values, types);
        this.schoolId = schoolId;
        this.school = SchoolRegistry.getSchool(schoolId);
        if (school == null) {
            FallenGemsAffixes.LOGGER.error("A Null SchoolType is created, {}", this.schoolId);
        }
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (callGuard.get()) return false;
        callGuard.set(true);
        try {
            if (!super.canApplyTo(stack, cat, rarity)) return false;
            if (cat == null || cat.isNone()) return false;

            EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(stack);
            Item item = stack.getItem();
            Set<Attribute> foundAttributes = new HashSet<>();

            // Curios compatibility
            if (item instanceof ICurioItem curio) {
                Set<String> slots = CuriosApi.getCuriosHelper().getCurioTags(item);
                for (String slotId : slots) {
                    SlotContext context = new SlotContext(slotId, null, -1, false, true);
                    foundAttributes.addAll(curio.getAttributeModifiers(context, UUID.randomUUID(), stack).keySet());
                }
            } else {
                foundAttributes = stack.getAttributeModifiers(slot).keySet();
            }

            for (Attribute attribute : foundAttributes) {
                ResourceLocation attrId = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
                if (attrId != null) {
                    String path = attrId.getPath();
                    if (path.endsWith("_spell_power") && path.length() > "_spell_power".length()) {
                        String schoolName = path.replace("_spell_power", "");
                        ResourceLocation schoolResource = new ResourceLocation(attrId.getNamespace(), schoolName);
                        SchoolType matchedSchool = SchoolRegistry.getSchool(schoolResource);
                        if (school != null && school.equals(matchedSchool)) {
                            return true;
                        }
                    }
                }
            }
            return !ModConfig.STRICT_SCHOOL_MATCH.get();
        } finally {
            callGuard.set(false);
        }
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}