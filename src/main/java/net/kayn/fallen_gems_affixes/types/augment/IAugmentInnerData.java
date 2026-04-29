package net.kayn.fallen_gems_affixes.types.augment;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.util.INBTSerializable;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ICodecProvider;

public interface IAugmentInnerData extends INBTSerializable<CompoundTag>, ICodecProvider<IAugmentInnerData> {
    IAugmentInnerData EMPTY = new IAugmentInnerData() {
        @Override
        public Codec<? extends IAugmentInnerData> getCodec() {
            return null;
        }

        @Override
        public void enable() {

        }

        @Override
        public void disable() {

        }

        @Override
        public boolean isFunctional() {
            return false;
        }

        @Override
        public IAugmentInnerData copy() {
            return this;
        }

        @Override
        public MutableComponent combineText() {
            return Component.empty();
        };

        @Override
        public CompoundTag serializeNBT() {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {

        }
    };
    void enable();
    void disable();
    boolean isFunctional();
    IAugmentInnerData copy();

    MutableComponent combineText();
}
