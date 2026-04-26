package net.kayn.fallen_gems_affixes.attachment.augment.mod_events;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event fires when assemble augment recipe.
 * 
 */
@Cancelable
public class AssembleAugmentRecipeEvent extends Event {
    private final Container cont;
    private final Level level;
    public AssembleAugmentRecipeEvent(Container cont, Level level) {
        this.cont = cont;
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    public Container getContainer() {
        return cont;
    }

    public ItemStack getAugmentItem() {
        return cont.getItem(2);
    }

    public ItemStack getItem() {
        return cont.getItem(1);
    }

    public ItemStack getIResult() {
        return cont.getItem(3);
    }
}
