package org.spongepowered.gradle.impl;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;

import java.util.Objects;

/**
 * A component of the project.
 */
public interface Facet extends Named {

    /**
     * Get the source set backing this facet.
     *
     * @return the source set backing this facets
     */
    SourceSet getSourceSet();

    /**
     * Act on the facet's source set.
     */
    default void sourceSet(final Action<? super SourceSet> setAction) {
        Objects.requireNonNull(setAction, "setAction").execute(this.getSourceSet());
    }

    /**
     * Get whether this facet is exposed to child projects
     * @return the child
     */
    Property<Boolean> isInherited();

    /**
     * Generate a sources jar for this facet
     */
    void withSourcesJar();

    // TODO: do we publish these relationships somehow?
    void read(final Facet facet);

    void read(final Provider<? extends Facet> facet);

}
