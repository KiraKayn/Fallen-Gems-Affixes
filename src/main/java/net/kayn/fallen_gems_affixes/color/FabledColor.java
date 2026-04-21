package net.kayn.fallen_gems_affixes.color;

import dev.shadowsoffire.placebo.PlaceboClient;
import dev.shadowsoffire.placebo.color.GradientColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class FabledColor extends GradientColor {

    public static final int[] FABLED_GRADIENT;

    static {
        int steps = 64;
        FABLED_GRADIENT = new int[steps];
        for (int i = 0; i < steps; i++) {
            double t = (double) i / steps;

            double wave = Math.sin(t * Math.PI * 2.0) * 0.5 + 0.5;
            int r = (int) (139 + wave * (255 - 139));
            FABLED_GRADIENT[i] = (r << 16);
        }
    }

    public static final FabledColor FABLED = new FabledColor();

    private FabledColor() {
        super(FABLED_GRADIENT, "fabled", 0.35f);
    }

    @Override
    public int getValue() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return this.gradient[(int) ((PlaceboClient.getColorTicks() * speed) % this.gradient.length)];
        }
        return super.getValue();
    }
}
