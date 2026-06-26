/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.vanilla.launch.plugin.resolver;

import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.metadata.model.PluginDependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class DependencyResolver {

    public static ResolutionResult resolveAndSortCandidates(final Collection<PluginCandidate> candidates,
            final Logger logger) {
        final Map<String, Node> nodes = new HashMap<>();
        final ResolutionResult resolutionResult = new ResolutionResult();
        for (final PluginCandidate candidate : candidates) {
            final String id = candidate.metadata().id();
            // If we already have an entry, this is now a duplicate ID situation.
            if (nodes.containsKey(id)) {
                resolutionResult.duplicateIds().add(id);
            } else {
                nodes.put(id, new Node(candidate));
            }
        }

        for (final Map.Entry<String, Node> entry : nodes.entrySet()) {
            // Attach deps, invalid deps will appear at this point.
            final Node node = entry.getValue();
            for (final PluginDependency pd : node.candidate.metadata().dependencies()) {
                final boolean isOptional = pd.optional();
                final Node dep = nodes.get(pd.id());

                if (dep == null) {
                    if (isOptional) {
                        continue; // just move on to the next dep
                    }
                    node.invalid = true;
                    final String failure;
                    if (pd.version() != null) {
                        failure = String.format("%s version %s", pd.id(), pd.version());
                    } else {
                        failure = pd.id();
                    }
                    resolutionResult.missingDependencies().computeIfAbsent(entry.getValue().candidate, k -> new ArrayList<>()).add(failure);
                    node.checked = true; // no need to process this further
                    continue;
                }

                if (!DependencyResolver.checkVersion(pd.version(), dep.candidate.metadata().version())) {
                    if (isOptional) {
                        continue; // just move on to the next dep
                    }
                    resolutionResult.versionMismatch().computeIfAbsent(entry.getValue().candidate, k -> new ArrayList<>()).add(Tuple.of(pd.version().toString(), dep.candidate));
                    node.invalid = true;
                    node.checked = true; // no need to process this further.
                }

                if (pd.loadOrder() == PluginDependency.LoadOrder.BEFORE) {
                    if (!pd.optional()) {
                        // Because of what we're about to do, we need to just make sure that
                        // if the "before" dep fails within here, then we still throw it out.
                        // Note, we can only do this for sorting, once sorted, if this loads
                        // but the before dep doesn't, well, it's on the plugin to solve, not
                        // us.
                        node.beforeRequiredDependency.add(node);
                    }
                    // don't bomb out the dep if this doesn't load - so set it to be optional.
                    // however, we otherwise treat it as an AFTER dep on the target dep
                    DependencyResolver.setDependency(dep, node, true);
                } else {
                    DependencyResolver.setDependency(node, dep, pd.optional());
                }
            }
        }

        // Check for invalid deps
        DependencyResolver.checkCyclic(nodes.values(), resolutionResult);
        for (final Node node : nodes.values()) {
            DependencyResolver.calculateSecondaryFailures(node, resolutionResult);
        }

        // Now to sort them.
        final List<Node> original = nodes.values().stream().filter(x -> !x.invalid).collect(Collectors.toCollection(ArrayList::new));
        final List<Node> toLoad = new ArrayList<>(original);
        final LinkedHashSet<Node> sorted = new LinkedHashSet<>();
        toLoad.stream().filter(x -> x.dependencies.isEmpty() && x.optionalDependencies.isEmpty()).forEach(sorted::add);
        toLoad.removeIf(sorted::contains);
        int size = toLoad.size();
        boolean excludeOptionals = false;
        while (!toLoad.isEmpty()) {
            boolean containsOptionalDeps = false;
            for (final Node node : toLoad) {
                if (sorted.containsAll(node.dependencies) && DependencyResolver.checkOptionalDependencies(excludeOptionals, sorted, node)) {
                    final boolean hasOptionalDeps = !node.optionalDependencies.isEmpty();
                    containsOptionalDeps |= hasOptionalDeps;
                    sorted.add(node);
                    if (excludeOptionals && hasOptionalDeps) {
                        logger.warn("Plugin {} will be loaded before its optional dependencies: [ {} ]",
                                node.candidate.metadata().id(),
                                node.optionalDependencies.stream().map(x -> x.candidate.metadata().id()).collect(Collectors.joining(", ")));
                    }
                }
            }
            toLoad.removeIf(sorted::contains);
            if (toLoad.size() == size) {
                // If we have excluded optionals then we need to re-do this cycle
                // without them.
                if (excludeOptionals || !containsOptionalDeps) {
                    // We have a problem
                    throw new IllegalStateException(String.format("Dependency resolver could not resolve order of all plugins.\n\n"
                            + "Attempted to sort %d plugins: [ %s ]\n"
                            + "Could not sort %d plugins: [ %s ]",
                            original.size(),
                            original.stream().map(x -> x.candidate.metadata().id()).collect(Collectors.joining(", ")),
                            toLoad.size(),
                            toLoad.stream().map(x -> x.candidate.metadata().id()).collect(Collectors.joining(", "))));
                }
                logger.warn("Failed to resolve plugin load order due to failed dependency resolution, attempting to resolve order ignoring optional"
                        + " dependencies.");
                excludeOptionals = true;
            } else {
                size = toLoad.size();
                excludeOptionals = false;
            }
        }

        final Collection<PluginCandidate> sortedSuccesses = resolutionResult.sortedSuccesses();
        for (final Node x : sorted) {
            sortedSuccesses.add(x.candidate);
        }
        return resolutionResult;
    }

    private static boolean checkOptionalDependencies(
            final boolean excludeOptionals, final Collection<Node> sorted, final Node node) {
        if (excludeOptionals) {
            // We need to make sure we filter out any deps that have "before" requirements - so we load those with all required deps met.
            return node.optionalDependencies.stream().flatMap(x -> x.beforeRequiredDependency.stream()).distinct().allMatch(sorted::contains);
        }
        return sorted.containsAll(node.optionalDependencies);
    }

    private static void setDependency(final Node before, final Node after, final boolean optional) {
        if (optional) {
            before.optionalDependencies.add(after);
        } else {
            before.dependencies.add(after);
        }
    }

    private static boolean checkVersion(final @Nullable VersionRange requestedVersion, final ArtifactVersion dependencyVersion) {
        if (requestedVersion == null || !requestedVersion.hasRestrictions()) {
            // we don't care which version
            return true;
        }
        // Maven Artifact version resolution has a bug(?) where VersionRange#containsVersion()
        // returns false if there are no restrictions when logically it should be true because
        // theoretically all versions are included. Except in our case, the recommended version
        // might be populated, yet no restrictions are in the VersionRange object, because we
        // want a specific version, which should be a restriction.
        //
        // Further, VersionRange#hasRestrictions() returns true even if VersionRange#getRestrictions()
        // is empty as it accounts for if there is a recommended version. Thus, we have to do the check
        // on the recommended version first... which might be null, hence the Objects.equals check.
        return Objects.equals(requestedVersion.getRecommendedVersion(), dependencyVersion) || requestedVersion.containsVersion(dependencyVersion);
    }

    private static void checkCyclic(final Collection<Node> nodes, final ResolutionResult resolutionResult) {
        for (final Node node : nodes) {
            if (!node.checked) {
                final LinkedHashSet<Node> nodeSet = new LinkedHashSet<>();
                nodeSet.add(node);
                DependencyResolver.checkCyclic(node, resolutionResult, nodeSet);
            }
        }
    }

    private static void checkCyclic(final Node node, final ResolutionResult resolutionResult,
            final LinkedHashSet<Node> dependencyPath) {
        if (node.invalid) {
            return;
        }

        // We're doing depth first.
        for (final Node dependency : node.dependencies) {
            // We've already done this. Consequential failures will be handled later.
            if (dependency.checked) {
                continue;
            }

            if (!dependencyPath.add(dependency)) {
                // This is a cyclic dep, so we need to break out.
                dependency.checked = true;
                node.invalid = true;
                // We create the dependency path for printing later.
                boolean append = false;
                final List<PluginCandidate> candidatePath = new LinkedList<>();
                for (final Node depInCycle : dependencyPath) {
                    append |= depInCycle == dependency;
                    // all candidates from here are in the loop.
                    if (append) {
                        candidatePath.add(depInCycle.candidate);
                        depInCycle.invalid = true;
                    }
                }

                // We'll only care about the one.
                for (final PluginCandidate dep : candidatePath) {
                    resolutionResult.cyclicDependency().put(dep, candidatePath);
                }
            } else {
                DependencyResolver.checkCyclic(dependency, resolutionResult, dependencyPath);
                // this should be at the bottom of this list so remove it again.
                dependencyPath.remove(dependency);
            }
        }
    }

    private static boolean calculateSecondaryFailures(final Node node, final ResolutionResult resolutionResult) {
        if (node.secondaryChecked) {
            return node.invalid;
        }

        node.secondaryChecked = true;
        if (node.invalid) {
            return true;
        } else if (node.dependencies.isEmpty() && node.beforeRequiredDependency.isEmpty()) {
            return false;
        }

        for (final Node depNode : node.dependencies) {
            if (DependencyResolver.calculateSecondaryFailures(depNode, resolutionResult)) {
                node.invalid = true;
                resolutionResult.cascadedFailure().computeIfAbsent(node.candidate, k -> new HashSet<>()).add(depNode.candidate);
            }
        }

        for (final Node depNode : node.beforeRequiredDependency) {
            if (DependencyResolver.calculateSecondaryFailures(depNode, resolutionResult)) {
                node.invalid = true;
                resolutionResult.cascadedFailure().computeIfAbsent(node.candidate, k -> new HashSet<>()).add(depNode.candidate);
            }
        }
        return node.invalid;
    }

    static class Node {

        final PluginCandidate candidate;
        final Set<Node> beforeRequiredDependency = new HashSet<>();
        final Set<Node> dependencies = new HashSet<>();
        final Set<Node> optionalDependencies = new HashSet<>();
        boolean invalid = false;
        boolean checked = false;
        boolean secondaryChecked = false;

        public Node(final PluginCandidate candidate) {
            this.candidate = candidate;
        }

    }

}
