package net.kayn.fallen_gems_affixes.item;

import net.kayn.fallen_gems_affixes.adventure.socket.SocketTierManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SigilOfElevationItem extends Item {

    public SigilOfElevationItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.fallen_gems_affixes.sigil_of_elevation.desc")
                .withStyle(ChatFormatting.GRAY));

        if (level != null && SocketTierManager.INSTANCE.isEnabled()) {
            int max = SocketTierManager.INSTANCE.getMaxOrdinal();
            if (max >= 0) {
                String maxKey = net.kayn.fallen_gems_affixes.adventure.socket.TieredSocketHelper
                        .getEmptySocketTranslationKey(max);
                tooltip.add(Component.translatable("item.fallen_gems_affixes.sigil_of_elevation.cap_hint",
                                Component.translatable(maxKey))
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
