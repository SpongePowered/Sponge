package org.spongepowered.common.data.processor.common;

import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.entity.SpongeEntityConstants;
import org.spongepowered.common.entity.SpongeHorseColor;
import org.spongepowered.common.entity.SpongeHorseStyle;

public class HorseUtils {

    public static int getInternalVariant(SpongeHorseColor color, SpongeHorseStyle style) {
        return color.getBitMask() | style.getBitMask();
    }

    public static HorseColor getHorseColor(EntityHorse horse) {
        return SpongeEntityConstants.HORSE_COLOR_IDMAP.get(horse.getHorseVariant() & 255);
    }

    public static HorseStyle getHorseStyle(EntityHorse horse) {
        return SpongeEntityConstants.HORSE_STYLE_IDMAP.get((horse.getHorseVariant() & 65280) >> 8);
    }

    public static HorseVariant getHorseVariant(int type) {
        return SpongeEntityConstants.HORSE_VARIANT_IDMAP.get(type);
    }

}
