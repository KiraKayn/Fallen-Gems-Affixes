package net.kayn.fallen_gems_affixes.attachment.rarity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import net.minecraft.resources.ResourceLocation;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.AFallenRarity;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ICodecProvider;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ILocalRarity;

import javax.annotation.Nullable;

public class FallenRarity extends AFallenRarity<ResourceLocation, ILocalRarity> implements ILocalRarity, ICodecProvider<FallenRarity> {
    public static final Codec<FallenRarity> CODEC = RecordCodecBuilder.create(ins -> ins.group(
            LootRarity.LOAD_CODEC.optionalFieldOf("apoth_rarity").forGetter(f -> java.util.Optional.ofNullable(
                    (LootRarity) (Object) f.rarity)),
            ResourceLocation.CODEC.fieldOf("id").forGetter(f -> f.classifier)
    ).apply(ins, (r, i) -> {
        ILocalRarity locR = null;
        if (r.isPresent()) {
            locR = (ILocalRarity) (Object) r.get();
        }
        return new FallenRarity(i, locR);
    }));

    public FallenRarity(ResourceLocation classifier, ILocalRarity rarity) {
        super(classifier, rarity);
    }

    @Override
    public Codec<FallenRarity> getCodec() {
        return CODEC;
    }

    @Override
    public ResourceLocation fallen_lib$getId() {
        return getClassifier();
    }

    @Nullable
    public LootRarity getLootRarity() {
        ILocalRarity r = this.getRarity();
        return r == null ? null : (LootRarity) (Object) r;
    }
}