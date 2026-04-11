package net.kayn.fallen_gems_affixes.adventure.socket.gem.storage;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class GemCaseAnimationState {

    private static final int SWITCH_INTERVAL_MIN = 80;
    private static final int SWITCH_INTERVAL_MAX = 240;
    private static final int ANIMATION_DURATION = 40;

    private final RandomSource random;
    private final int[] slotPositions;
    private int ticksUntilNextSwitch;
    private int animationTicks;
    private int swappingIndex1 = -1;
    private int swappingIndex2 = -1;
    private boolean isAnimating = false;

    public GemCaseAnimationState(RandomSource random) {
        this.random = random;
        this.slotPositions = new int[16];
        for (int i = 0; i < 16; i++) this.slotPositions[i] = i;
        this.shuffleInitial();
        this.ticksUntilNextSwitch = this.getRandomSwitchInterval();
    }

    private void shuffleInitial() {
        for (int i = 0; i < 16; i++) {
            int j = this.random.nextInt(16);
            int temp = this.slotPositions[i];
            this.slotPositions[i] = this.slotPositions[j];
            this.slotPositions[j] = temp;
        }
    }

    public void tick(int activeGemCount, boolean isPlayerNearby) {
        if (activeGemCount < 4) return;
        if (this.isAnimating) {
            this.animationTicks++;
            if (this.animationTicks >= ANIMATION_DURATION) {
                this.completeSwap();
                this.isAnimating = false;
                this.ticksUntilNextSwitch = this.getRandomSwitchInterval();
            }
        } else if (isPlayerNearby) {
            this.ticksUntilNextSwitch--;
            if (this.ticksUntilNextSwitch <= 0) this.startRandomSwap(activeGemCount);
        }
    }

    private void startRandomSwap(int activeGemCount) {
        this.swappingIndex1 = this.random.nextInt(activeGemCount);
        this.swappingIndex2 = this.random.nextInt(activeGemCount);
        if (this.swappingIndex1 == this.swappingIndex2)
            this.swappingIndex2 = (this.swappingIndex2 + 1) % activeGemCount;
        this.isAnimating = true;
        this.animationTicks = 0;
    }

    private void completeSwap() {
        if (this.swappingIndex1 >= 0 && this.swappingIndex2 >= 0) {
            int temp = this.slotPositions[this.swappingIndex1];
            this.slotPositions[this.swappingIndex1] = this.slotPositions[this.swappingIndex2];
            this.slotPositions[this.swappingIndex2] = temp;
        }
    }

    public PositionInfo getPosition(int gemIndex, float partialTicks) {
        int baseSlot = this.slotPositions[Math.min(gemIndex, 15)];
        if (!this.isAnimating || (gemIndex != this.swappingIndex1 && gemIndex != this.swappingIndex2))
            return new PositionInfo(baseSlot, 0, 0);

        float progress = Mth.clamp((this.animationTicks + partialTicks) / (float) ANIMATION_DURATION, 0f, 1f);
        progress = progress * progress * (3f - 2f * progress);

        int targetSlot = (gemIndex == this.swappingIndex1) ? this.slotPositions[this.swappingIndex2] : this.slotPositions[this.swappingIndex1];

        float offsetX = ((targetSlot % 4) - (baseSlot % 4)) * progress;
        float offsetZ = ((targetSlot / 4) - (baseSlot / 4)) * progress;
        return new PositionInfo(baseSlot, offsetX, offsetZ);
    }

    private int getRandomSwitchInterval() {
        return SWITCH_INTERVAL_MIN + this.random.nextInt(SWITCH_INTERVAL_MAX - SWITCH_INTERVAL_MIN);
    }

    public record PositionInfo(int baseSlot, float offsetX, float offsetZ) {
    }
}
