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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.plugin.PluginCandidate;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

public final class ResolutionResult {

    private final LinkedHashSet<PluginCandidate> sortedSuccesses;
    private final Collection<String> duplicateIds;
    private final Map<PluginCandidate, Collection<String>> missingDependencies;
    private final Map<PluginCandidate, Collection<Tuple<String, PluginCandidate>>> versionMismatch;
    private final Map<PluginCandidate, Collection<PluginCandidate>> cyclicDependency;
    private final Map<PluginCandidate, Collection<PluginCandidate>> cascadedFailure;

    public ResolutionResult() {
        this.sortedSuccesses = new LinkedHashSet<>();
        this.duplicateIds = new HashSet<>();
        this.missingDependencies = new HashMap<>();
        this.versionMismatch = new HashMap<>();
        this.cyclicDependency = new HashMap<>();
        this.cascadedFailure = new HashMap<>();
    }

    public Collection<PluginCandidate> sortedSuccesses() {
        return this.sortedSuccesses;
    }

    public Collection<String> duplicateIds() {
        return this.duplicateIds;
    }

    public Map<PluginCandidate, Collection<String>> missingDependencies() {
        return this.missingDependencies;
    }

    public Map<PluginCandidate, Collection<Tuple<String, PluginCandidate>>> versionMismatch() {
        return this.versionMismatch;
    }

    public Map<PluginCandidate, Collection<PluginCandidate>> cyclicDependency() {
        return this.cyclicDependency;
    }

    public Map<PluginCandidate, Collection<PluginCandidate>> cascadedFailure() {
        return this.cascadedFailure;
    }

    public void printErrorsIfAny(
            final Map<PluginCandidate, String> failedInstance,
            final Map<PluginCandidate, String> consequentialFailedInstance,
            final Logger logger) {
        final int noOfFailures = this.numberOfFailures() + failedInstance.size() + consequentialFailedInstance.size();
        if (noOfFailures == 0) {
            return;
        }

        final PrettyPrinter errorPrinter = new PrettyPrinter(120);
        errorPrinter.add("SPONGE PLUGINS FAILED TO LOAD").centre().hr()
                .addWrapped("%d plugin(s) have unfulfilled or cyclic dependencies or failed to load. Your game will continue to load without"
                                + " these plugins.",
                        noOfFailures);

        if (!this.duplicateIds.isEmpty()) {
            errorPrinter.add();
            errorPrinter.add("The following plugins IDs were duplicated - some plugins will not have been loaded:");
            for (final String id : this.duplicateIds) {
                errorPrinter.add(" * %s", id);
            }
        }

        if (!this.missingDependencies.isEmpty()) {
            errorPrinter.add();
            errorPrinter.add("The following plugins are missing dependencies:");
            for (final Map.Entry<PluginCandidate, Collection<String>> entry : this.missingDependencies.entrySet()) {
                errorPrinter.add(" * %s requires [ %s ]",
                        entry.getKey().metadata().id(),
                        String.join(", ", entry.getValue()));
            }
        }

        if (!this.versionMismatch.isEmpty()) {
            errorPrinter.add();
            errorPrinter.add("The following plugins require different version(s) of dependencies you have installed:");
            for (final Map.Entry<PluginCandidate, Collection<Tuple<String, PluginCandidate>>> entry : this.versionMismatch.entrySet()) {
                final PluginCandidate candidate = entry.getKey();
                final Collection<Tuple<String, PluginCandidate>> mismatchedDeps = entry.getValue();
                final String errorString = mismatchedDeps.stream()
                        .map(x -> String.format("%s version %s (currently version %s)",
                                x.second().metadata().id(), x.first(), x.second().metadata().version()))
                        .collect(Collectors.joining(", "));
                errorPrinter.add(" * %s requires [ %s ]",
                        candidate.metadata().id(),
                        errorString);
            }
        }

        if (!this.cyclicDependency.isEmpty()) {
            errorPrinter.add();
            errorPrinter.add("The following plugins were found to have cyclic dependencies:");
            for (final Map.Entry<PluginCandidate, Collection<PluginCandidate>> node : this.cyclicDependency.entrySet()) {
                errorPrinter.add(" * %s has dependency cycle [ ... -> %s -> ... ]",
                        node.getKey().metadata().id(),
                        node.getValue().stream().map(x -> x.metadata().id()).collect(Collectors.joining(" -> ")));
            }
        }

        if (!failedInstance.isEmpty()) {
            errorPrinter.add();
            errorPrinter.add("The following plugins threw exceptions when being created (report these to the plugin authors):");
            for (final Map.Entry<PluginCandidate, String> node : failedInstance.entrySet()) {
                errorPrinter.add(" * %s with the error message \"%s\"",
                        node.getKey().metadata().id(),
                        node.getValue());
            }
        }

        if (!this.cascadedFailure.isEmpty() || !consequentialFailedInstance.isEmpty()) {
            final Map<PluginCandidate, String> mergedFailures = new HashMap<>(consequentialFailedInstance);
            for (final Map.Entry<PluginCandidate, Collection<PluginCandidate>> entry : this.cascadedFailure.entrySet()) {
                final String error = entry.getValue().stream().map(x -> x.metadata().id()).collect(Collectors.joining(", "));
                mergedFailures.merge(entry.getKey(), error, (old, incoming) -> old + ", " + incoming);
            }

            errorPrinter.add();
            errorPrinter.add("The following plugins are not loading because they depend on plugins that will not load:");
            for (final Map.Entry<PluginCandidate, String> node : mergedFailures.entrySet()) {
                errorPrinter.add(" * %s depends on [ %s ]",
                        node.getKey().metadata().id(),
                        // nothing wrong with this plugin other than the other plugins,
                        // so we just list all the plugins that failed
                        node.getValue());
            }
        }

        errorPrinter.add().hr().addWrapped("DO NOT REPORT THIS TO SPONGE. These errors are not Sponge errors, they are plugin loading errors. Seek "
                + "support from the authors of the plugins listed above if you need help getting these plugins to load.").add();
        errorPrinter.addWrapped("Your game will continue to start without the %d plugins listed above. Other plugins will continue to load, "
                        + "however you may wish to stop your game and fix these issues. For any missing dependencies, you "
                        + "may be able to find them at https://ore.spongepowered.org/. For any plugins that have cyclic dependencies or threw "
                        + "exceptions, it is likely a bug in the plugin.", noOfFailures);

        errorPrinter.log(logger, Level.ERROR);
    }

    private int numberOfFailures() {
        return this.missingDependencies.size() + this.versionMismatch.size() + this.cyclicDependency.size() + this.cascadedFailure.size();
    }

}
