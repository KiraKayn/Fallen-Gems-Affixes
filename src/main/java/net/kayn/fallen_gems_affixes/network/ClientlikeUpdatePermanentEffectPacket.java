package net.kayn.fallen_gems_affixes.network;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ClientlikeUpdatePermanentEffectPacket(Holder<MobEffect> effect, int amplifier, boolean remove) implements CustomPacketPayload {
    public static final String version = "1.0";
    public static final Type<ClientlikeUpdatePermanentEffectPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "cup_pe"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientlikeUpdatePermanentEffectPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, ClientlikeUpdatePermanentEffectPacket value) {
            MobEffect.STREAM_CODEC.encode(buf, value.effect);
            ByteBufCodecs.VAR_INT.encode(buf, value.amplifier);
            ByteBufCodecs.BOOL.encode(buf, value.remove);
        }

        @Override
        public @NotNull ClientlikeUpdatePermanentEffectPacket decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new ClientlikeUpdatePermanentEffectPacket(MobEffect.STREAM_CODEC.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.BOOL.decode(buf));
        }
    };

    public static void handleClient(ClientlikeUpdatePermanentEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            var a = player.getCapability(Fallen.Capabilities.PE_CAP);
            if (a != null) {
                if (packet.remove) {
                    a.removeEffect(packet.effect, packet.amplifier);
                }
                else {
                    a.addEffect(packet.effect, packet.amplifier);
                }
            }
        });
    }
}