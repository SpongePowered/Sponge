package org.spongepowered.gradle.impl;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.artifacts.Dependency;

import java.util.Objects;

/**
 * An interface on the project.
 */
public interface FacetExtension {

    NamedDomainObjectContainer<Facet> getFacets();

    default void facets(final Action<? super NamedDomainObjectContainer<Facet>> configure) {
        Objects.requireNonNull(configure, "configure").execute(this.getFacets());
    }

    /**
     * Create a copy of the specified dependency to depend on it as its
     * faceted variant, not its combined variant.
     *
     * @param dependency the dependency notation
     */
    Dependency facetsOf(final Object dependency);

}
