package net.kayn.fallen_gems_affixes.attachment.augment.mod_events;

import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event fires when augment is going to apply.
 *
 */
@Cancelable
public class ApplyAugmentEvent extends Event {
    private final ItemStack stack;
    private final AugmentInstance instance;
    public ApplyAugmentEvent(ItemStack stack, AugmentInstance instance) {
        this.stack = stack;
        this.instance = instance;
    }

    public ItemStack getStack() {
        return stack;
    }

    public AugmentInstance getInstance() {
        return instance;
    }
}
