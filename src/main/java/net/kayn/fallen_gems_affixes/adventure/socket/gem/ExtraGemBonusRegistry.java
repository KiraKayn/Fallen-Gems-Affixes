package net.kayn.fallen_gems_affixes.adventure.socket.gem;

import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;

public class ExtraGemBonusRegistry extends DynamicRegistry<ExtraGemBonusRegistry.ExtraGemBonus> {

    public static final ExtraGemBonusRegistry INSTANCE = new ExtraGemBonusRegistry();

    protected Multimap<DynamicHolder<Gem>, ExtraGemBonus> extraBonuses = HashMultimap.create();

    public ExtraGemBonusRegistry() {
        super(FallenGemsAffixes.LOGGER, "extra_gem_bonuses", true, false);
    }

    @Override
    protected void beginReload() {
        super.beginReload();
        this.extraBonuses = HashMultimap.create();
        this.clearExtraGemBonuses();
    }

    @Override
    protected void onReload() {
        super.onReload();
        for (ExtraGemBonus extraBonus : this.getValues()) {
            this.extraBonuses.put(extraBonus.gem, extraBonus);
        }
        this.applyExtraGemBonuses();
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(FallenGemsAffixes.id("extra_gem_bonus"), ExtraGemBonus.CODEC);
    }

    private void applyExtraGemBonuses() {
        for (Gem gem : GemRegistry.INSTANCE.getValues()) {
            DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(gem);

            for (ExtraGemBonus extraBonus : this.extraBonuses.get(holder)) {
                for (GemBonus bonus : extraBonus.bonuses()) {
                    try {
                        ((GemBonusExtension) gem).fallen_gems_affixes$appendExtraBonus(bonus);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private void clearExtraGemBonuses() {
        for (Gem gem : GemRegistry.INSTANCE.getValues()) {
            if (gem instanceof GemBonusExtension extension) {
                extension.fallen_gems_affixes$clearExtraBonuses();
            }
        }
    }

    public record ExtraGemBonus(DynamicHolder<Gem> gem,
                                List<GemBonus> bonuses) implements CodecProvider<ExtraGemBonus> {

        public static final Codec<ExtraGemBonus> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                        GemRegistry.INSTANCE.holderCodec().fieldOf("gem").forGetter(ExtraGemBonus::gem),
                        GemBonus.CODEC.listOf().fieldOf("bonuses").forGetter(ExtraGemBonus::bonuses))
                .apply(inst, ExtraGemBonus::new));

        @Override
        public Codec<? extends ExtraGemBonus> getCodec() {
            return CODEC;
        }
    }
}