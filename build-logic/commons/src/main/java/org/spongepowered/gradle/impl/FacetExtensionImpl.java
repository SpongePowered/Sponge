package org.spongepowered.gradle.impl;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import javax.inject.Inject;

public class FacetExtensionImpl implements FacetExtension {
    private final SourceSetContainer sourceSets;
    private final DependencyHandler dependencies;

    private final NamedDomainObjectContainer<Facet> facets;

    @Inject
    public FacetExtensionImpl(final ObjectFactory objects, final SourceSetContainer sourceSets, final Project project) {
        this.sourceSets = sourceSets;
        this.dependencies = project.getDependencies();

        this.facets = objects.domainObjectContainer(Facet.class, new NamedDomainObjectFactory<Facet>() {
            @Override
            public Facet create(final String name) {
                // Add source set
                final SourceSet facetSet = sourceSets.maybeCreate(name);
                // Add dependency configuration

                // Add outgoing configuration
                return objects.newInstance(FacetImpl.class, facetSet, project);
            }
        });
    }

    @Override
    public NamedDomainObjectContainer<Facet> getFacets() {
        return this.facets;
    }

    @Override
    public Dependency facetsOf(final Object dependency) {
        final Dependency initial;
        if (dependency instanceof ModuleDependency) {
            initial = ((Dependency) dependency).copy();
        } else {
            initial = this.dependencies.create(dependency);
        }

        if (!(initial instanceof ModuleDependency)) {
            // todo: error?
            return initial;
        }

        final ModuleDependency mod = (ModuleDependency) initial;
        return mod.capabilities(caps -> {
            caps.requireCapabilities(mod.getGroup() + ":" + mod.getName() + "-faceted:" + mod.getVersion());
        });
    }
}
