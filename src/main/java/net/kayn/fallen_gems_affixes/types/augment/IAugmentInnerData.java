package net.kayn.fallen_gems_affixes.types.augment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.util.INBTSerializable;

public interface IAugmentInnerData extends INBTSerializable<CompoundTag> {
    IAugmentInnerData EMPTY = new IAugmentInnerData() {
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

    MutableComponent combineText();
}
