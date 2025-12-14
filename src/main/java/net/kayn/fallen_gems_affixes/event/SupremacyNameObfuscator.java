package net.kayn.fallen_gems_affixes.event;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = FallenGemsAffixes.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class SupremacyNameObfuscator {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.hasTag()) return;

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean("fallen_gems_affixes:fabled")) return;

        ensureObfuscatedName(stack, tag);
    }

    private static void ensureObfuscatedName(ItemStack stack, CompoundTag tag) {
        if (!tag.contains("affix_data")) return;

        CompoundTag affixData = tag.getCompound("affix_data");
        if (!affixData.contains("name")) return;

        Component affixName = Component.Serializer.fromJson(affixData.getString("name"));
        if (affixName == null) return;

        String rawText = affixName.getString();
        if (rawText.isEmpty()) return;

        String obfuscatedJson =
                "{\"text\":\"\",\"extra\":[{\"text\":\"" +
                        rawText.replace("\\", "\\\\").replace("\"", "\\\"") +
                        "\",\"color\":\"dark_red\",\"obfuscated\":true}]}";
        affixData.putString("name", obfuscatedJson);
        tag.put("affix_data", affixData);

        CompoundTag display = tag.contains("display")
                ? tag.getCompound("display")
                : new CompoundTag();

        display.putString("Name", obfuscatedJson);
        tag.put("display", display);
    }
}