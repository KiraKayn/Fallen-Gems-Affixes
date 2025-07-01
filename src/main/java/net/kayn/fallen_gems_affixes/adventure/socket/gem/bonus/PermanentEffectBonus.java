package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class PermanentEffectBonus extends GemBonus {
    protected final MobEffect effect;
    protected final Map<LootRarity, Integer> values;
    protected static final Codec<PermanentEffectBonus> CODEC = null;

    public PermanentEffectBonus(ResourceLocation id, GemClass gemClass, MobEffect effect, Map<LootRarity, Integer> values, Codec<PermanentEffectBonus> codec) {
        super(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "multi_effect"), gemClass);
        this.effect = effect;
        this.values = values;
    }

    @Override
    public GemBonus validate() {
        Preconditions.checkNotNull(this.effect, "Null mob effect");
        return this;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return this.values.containsKey(rarity);
    }

    @Override
    public int getNumberOfUUIDs() {
        return 0;
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack itemStack, LootRarity lootRarity) {
        MobEffectInstance inst = new MobEffectInstance(this.effect, Integer.MAX_VALUE, this.values.get(lootRarity));
        MutableComponent comp = Component.translatable("armed").withStyle(ChatFormatting.YELLOW);
        Component infinity = Component.translatable("infinity");
        comp = comp.append(" ").append(infinity);
//        if (this.stackOnReapply) {
//            comp = comp.append(" ").append(Component.translatable("affix.apotheosis.stacking"));
//        }
        return comp;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    public MobEffect getEffect() {
        return this.effect;
    }

    public int getAmplifier(LootRarity rarity) {
        return this.values.get(rarity);
    }
}