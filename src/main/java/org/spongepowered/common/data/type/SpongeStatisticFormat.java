package org.spongepowered.common.data.type;

import net.minecraft.stats.IStatType;
import net.minecraft.stats.StatBase;
import org.spongepowered.api.statistic.StatisticFormat;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class SpongeStatisticFormat implements StatisticFormat {
    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.US);
    private final DecimalFormat decimalFormat = new DecimalFormat("########0.00");
    private final long maxValue = 1000000000L;

    private final String id;

    public SpongeStatisticFormat(String id) {
        this.id = id;
    }

    public SpongeStatisticFormat() {
        this("COUNT");
    }

    @Override
    public String format(long value) {
        if(this.id.equals("DISTANCE")) {
            if(value >= this.maxValue) {
                value = value / 100;
                if(value >= this.maxValue) {
                    value = value / 1000;
                    return this.decimalFormat.format(value) + " km";
                } else {
                    return this.decimalFormat.format(value) + " m";
                }
            } else {
                return this.decimalFormat.format(value) + " cm";
            }
        } else if(this.id.equals("TIME")) {
            value = value / 20;
            if(value >= this.maxValue) {
                value = value / 60;
                if(value >= this.maxValue ) {
                    value = value / 60;
                    if(value >= this.maxValue) {
                        value = value / 24;
                        return this.decimalFormat.format(value) + " d";
                    } else {
                        return this.format(value) + " h";
                    }
                } else {
                    return this.decimalFormat.format(value) + " m";
                }
            } else {
                return this.decimalFormat.format(value) + " s";
            }
        } else if(this.id.equals("FRACTIONAL")) {
            return this.decimalFormat.format(value);
        } else {
            return this.numberFormat.format(value);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.id;
    }

    public static SpongeStatisticFormat byStatType(IStatType type) {
        if(type.equals(StatBase.distanceStatType)) {
            return new SpongeStatisticFormat("DISTANCE");
        } else if(type.equals(StatBase.timeStatType)) {
            return new SpongeStatisticFormat("TIME");
        } else if(type.equals(StatBase.field_111202_k)) {
            return new SpongeStatisticFormat("FRACTIONAL");
        } else {
            return new SpongeStatisticFormat("COUNT");
        }
    }
}
