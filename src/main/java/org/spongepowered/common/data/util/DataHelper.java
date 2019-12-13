package org.spongepowered.common.data.util;

import net.minecraft.state.IntegerProperty;

public class DataHelper {

    // TODO: Cache min/max in IntegerProperty for faster access?

    public static int min(IntegerProperty property) {
        //noinspection OptionalGetWithoutIsPresent
        return property.getAllowedValues().stream().mapToInt(i -> i).min().getAsInt();
    }

    public static int max(IntegerProperty property) {
        //noinspection OptionalGetWithoutIsPresent
        return property.getAllowedValues().stream().mapToInt(i -> i).max().getAsInt();
    }
}
