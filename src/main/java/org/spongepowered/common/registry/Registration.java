package org.spongepowered.common.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Registration {

    Phase value();

    enum Phase {
        PRE_INIT,
        INIT,
        POST_INIT,
        ;
    }

}
