package org.spongepowered.common.mixin.core.data.types;

import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBase;
import org.spongepowered.api.statistic.StatisticFormat;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IStatType.class)
@Implements(@Interface(iface = StatisticFormat.class, prefix = "format$"))
public abstract class MixinIStatType implements StatisticFormat {

    public String format$format(long value) {
        return null;
    }

    public String format$getId() {
        if (this == StatBase.distanceStatType) {
            return "DISTANCE";
        } else if (this == StatBase.field_111202_k) {
            return "FRACTIONAL";
        } else if (this == StatBase.timeStatType) {
            return "TIME";
        } else {
            return "COUNT";
        }
    }

    public String format$getName() {
        if (this == StatBase.distanceStatType) {
            return "DISTANCE";
        } else if (this == StatBase.field_111202_k) {
            return "FRACTIONAL";
        } else if (this == StatBase.timeStatType) {
            return "TIME";
        } else {
            return "COUNT";
        }
    }
}
