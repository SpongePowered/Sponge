package org.spongepowered.gradle.impl;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Set up the appropriate variants of a project (accessors, main, mixins, launch, applaunch)
 */
public class SpongeImplementationPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project target) {
        target.getExtensions().create("spongeImpl", SpongeImplementationExtension.class, target, target.getLogger());
    }
}
