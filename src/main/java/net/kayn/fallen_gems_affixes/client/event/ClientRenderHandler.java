//package net.kayn.fallen_gems_affixes.client.event;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.player.LocalPlayer;
//import net.minecraft.world.effect.MobEffectInstance;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.event.TickEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//
//import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.checkGemBonus;
//
//@Mod.EventBusSubscriber(value = Dist.CLIENT)
//public class ClientRenderHandler {
//    @SubscribeEvent
//    public static void onClientTick(TickEvent.ClientTickEvent event) {
//        if (event.phase == TickEvent.Phase.END) {
//            LocalPlayer player = Minecraft.getInstance().player;
//            if (player != null && player.tickCount % 100 == 0 && player.level().isClientSide) {
//                for(ItemStack equipment : player.getAllSlots()) {
//                    checkGemBonus(equipment, (bonus, rarity) -> {
//                        if (!player.hasEffect(bonus.getEffect())) {
//                            player.addEffect(new MobEffectInstance(bonus.getEffect(), -1, bonus.getAmplifier(rarity)));
//                        }
//                    });
//                }
//            }
//        }
//    }
//}
