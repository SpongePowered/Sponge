package org.spongepowered.common.registry;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the registry being a direct override of a different
 * registry. Usually when a registry is planned for "vanilla" environments
 * but cannot know the underlying implementation and as such requires
 * additional minor changes. This will prevent the other registry from
 * being used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OverrideRegistration {

    Class<?> value();

}
