package org.spongepowered.common.util;

import java.util.function.Supplier;

public class Functional {


    public static final <T> Supplier<? extends T> throwNull() {
        return () -> {
            throw new IllegalStateException("Builder has undefined value set");
        };
    }


}
