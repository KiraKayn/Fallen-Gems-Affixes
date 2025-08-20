package net.kayn.fallen_gems_affixes.network;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class ClientlikeClearPermanentEffectPacket implements CustomPacketPayload{
    public static final String version = "1.0";
    public static final CustomPacketPayload.Type<ClientlikeClearPermanentEffectPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "ccl_pe"));

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientlikeClearPermanentEffectPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull ClientlikeClearPermanentEffectPacket value) {}

        @Override
        public @NotNull ClientlikeClearPermanentEffectPacket decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new ClientlikeClearPermanentEffectPacket();
        }
    };

    public static void handleClient(ClientlikeClearPermanentEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            var a = player.getCapability(Fallen.Capabilities.PE_CAP);
            if (a != null) {
                a.getContainer().clearEffects();
            }
        });
    }
}
