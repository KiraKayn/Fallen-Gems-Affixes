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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
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

    public static final Map<Item, Set<SchoolType>> recordedIronsItems = new HashMap<>();

    @SubscribeEvent
    public static void loadingIronsItemsFromConfig(ModConfigEvent event) {
        List<String> raw = ModConfig.IRONS_ITEMS_MAP.get();
        raw.forEach((entry) -> {
            String[] split = entry.trim().split("\\|", 0);
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0]));
            if (item == null) return;
            Set<SchoolType> schoolTypes = new HashSet<>();
            for (int i = 1; i < split.length; i++) {
                SchoolType school = SchoolRegistry.getSchool(new ResourceLocation(split[i]));
                if (school != null) {
                    schoolTypes.add(school);
                }
            }
            if (!schoolTypes.isEmpty()) {
                recordedIronsItems.put(item, schoolTypes);
            }
        });
    }

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
        // Validation part.
        if (cat.isNone()) return false;
        if (!this.modifiers.containsKey(rarity)) return false;
        if (this.types.isEmpty()) return true;
        Item item = stack.getItem();

        // This should be checked at first.
        Set<SchoolType> schoolTypes = recordedIronsItems.get(item);
        if (schoolTypes != null && schoolTypes.contains(this.school)) return true;
        if (!this.types.contains(cat)) return false;

        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(stack);
        Set<Attribute> foundAttributes = new HashSet<>();

        // Curios compatibility
        if (item instanceof ICurioItem curio) {
            Set<String> slots = CuriosApi.getCuriosHelper().getCurioTags(item);
            for (String slotId : slots) {
                SlotContext context = new SlotContext(slotId, null, -1, false, true);
                foundAttributes.addAll(curio.getAttributeModifiers(context, UUID.randomUUID(), stack).keySet());
            }
        } else {
            foundAttributes = item.getAttributeModifiers(slot, stack).keySet();
        }

        for (Attribute attribute : foundAttributes) {
            ResourceLocation attrId = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
            if (attrId != null) {
                String path = attrId.getPath();
                if (path.endsWith("spell_power") && path.length() != 11) {
                    String schoolName = path.replace("_spell_power", "");
                    ResourceLocation schoolResource = new ResourceLocation(attrId.getNamespace(), schoolName);
                    SchoolType matchedSchool = SchoolRegistry.getSchool(schoolResource);
                    if (matchedSchool != null && school == matchedSchool) {
                        return true;
                    }
                }
            }
        }
        return !ModConfig.STRICT_SCHOOL_MATCH.get();
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}