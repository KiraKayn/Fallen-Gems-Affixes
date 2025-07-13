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
    // this is a record, it may be used some time, like manually control school types in config.
    protected static final Map<Item, SchoolType> recorded_irons_items = new HashMap<>();


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
        if (!super.canApplyTo(stack, cat, rarity)) {
            return false;
        }
        if (cat == null || cat.isNone()) {
            return false;
        }
        // get the EquipmentSlot.
        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(stack);
        Item item = stack.getItem();
        Set<Attribute> modifiers1 = new HashSet<>();
        // curio compat.
        if (item instanceof ICurioItem curio) {
            Set<String> slots = CuriosApi.getCuriosHelper().getCurioTags(item);
            for (String slot1 : slots) {
                SlotContext slotContext = new SlotContext(slot1, null, -1, false, true);
                modifiers1.addAll(curio.getAttributeModifiers(slotContext, UUID.fromString("667ad88f-901d-4691-b2a2-3664e42026d3"), stack).keySet());
            }
        } else {
            modifiers1 = stack.getAttributeModifiers(slot).keySet();
        }
        for (Attribute attribute : modifiers1) {
            var attrId = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
            var path = attrId.getPath();
            if (attrId != null && path.endsWith("spell_power")) {
                if (path.length() != 11) {
                    String schoolName = attrId.getPath().replace("_spell_power", "");
                    ResourceLocation schoolResource = ResourceLocation.fromNamespaceAndPath(attrId.getNamespace(), schoolName);
                    SchoolType school1 = SchoolRegistry.getSchool(schoolResource);
                    if (school != null && school == school1) return true;
                }
            }
        }
        // if modifier is empty, it can take this affix? Or not?
        // default
        return true;
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}