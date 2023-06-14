package org.spongepowered.gradle.impl;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSetContainer;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

public class FacetedPlugin implements Plugin<Project> {

    public static final String PARENT_CONFIGURATION = "parent";

    private static final String EXTENSION_NAME = "faceted";

    @Override
    public void apply(final @Nonnull Project target) {
        target.getPlugins().apply(JavaBasePlugin.class);
        final FacetExtension ext = this.registerExtension(target);

        this.registerParentConfiguration(target, ext);

        this.configurePublication(ext, target);

        // Set up links between source sets
    }

    private FacetExtension registerExtension(final Project project) {
        return project.getExtensions().create(FacetExtension.class, EXTENSION_NAME, FacetExtensionImpl.class, project.getExtensions().getByType(SourceSetContainer.class), project.getDependencies());
    }

    private NamedDomainObjectProvider<Configuration> registerParentConfiguration(final Project project, final FacetExtension extension) {
        final NamedDomainObjectProvider<Configuration> parent = project.getConfigurations().register(FacetedPlugin.PARENT_CONFIGURATION);

        // Request the faceted variant of parent projects
        parent.configure(conf -> {
            conf.withDependencies(deps -> {
                final Set<Dependency> oldDeps = new HashSet<>(deps);
                deps.clear();
                for (final Dependency dep : oldDeps) {
                    deps.add(extension.facetsOf(dep));
                }
            });
        });

        return parent;
    }

    private void configurePublication(final FacetExtension facets, final Project target) {
        // add outgoing api elements for each config
        // (does it even make sense to have a separate runtime?)
        // make sure the outgoing config has the specific -faceted capability
        // create a jar task
    }
}
