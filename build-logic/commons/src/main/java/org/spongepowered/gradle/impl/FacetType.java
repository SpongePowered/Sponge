package org.spongepowered.gradle.impl;

import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

public interface FacetType extends Named {

    Attribute<FacetType> FACET_TYPE_ATTRIBUTE = Attribute.of("org.spongepowered.facet", FacetType.class);

    // Early init on the system classloader
    String APPLAUNCH = "applaunch"; // Standalone
    // Early init on the TCL
    String LAUNCH = "launch"; // Can see game + main?

    // Mixins (not visible as classes)
    String MIXINS = "mixins"; // Can see main + accessors + launch + applaunch + game

    // Accessors
    String ACCESSORS = "accessors"; // Can see main + launch + applaunch + game

    // The main content
    String MAIN = "main"; // Can see applaunch, launch, accessors, game

}
