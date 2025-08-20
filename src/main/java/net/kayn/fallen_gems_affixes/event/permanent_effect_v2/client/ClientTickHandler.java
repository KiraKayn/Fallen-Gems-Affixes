package net.kayn.fallen_gems_affixes.event.permanent_effect_v2.client;

import net.kayn.fallen_gems_affixes.Fallen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientTickHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isPaused()) {
            LocalPlayer player = mc.player;
            if (player != null) {
                var cap = player.getCapability(Fallen.Capabilities.PE_CAP);
                if (cap != null) {
                    cap.getContainer().forEachEffect((e, l) -> {
                        if (!player.hasEffect(e)) {
                            cap.addEffectSilent(e, l.getLast());
                        }
                    });
                }
            }
        }
    }
}
