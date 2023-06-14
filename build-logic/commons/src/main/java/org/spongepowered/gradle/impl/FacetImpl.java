package org.spongepowered.gradle.impl;

import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;

import javax.inject.Inject;

abstract class FacetImpl implements Facet {
    private final Project owner;
    private final SourceSet set;

    private final Property<Boolean> inherited;

    @Inject
    public FacetImpl(final ObjectFactory objects, final SourceSet set, final Project owner) {
        this.owner = owner;
        this.set = set;
        this.inherited = objects.property(Boolean.class)
            .convention(true);
    }

    @Override
    public SourceSet getSourceSet() {
        return this.set;
    }

    @Override
    public Property<Boolean> isInherited() {
        return this.inherited;
    }

    @Override
    public void withSourcesJar() {
        final String name = this.set.getSourcesJarTaskName();
        if (this.owner.getTasks().getNames().contains(name)) {
            this.owner.getTasks().named(name, Jar.class).configure(task -> {

            });
        }
    }

    @Override
    public void read(final Facet facet) {

    }

    @Override
    public void read(final Provider<? extends Facet> facet) {

    }
}
