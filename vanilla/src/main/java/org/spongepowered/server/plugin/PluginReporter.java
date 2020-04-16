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
package org.spongepowered.server.plugin;

import static com.google.common.base.Preconditions.checkArgument;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import org.spongepowered.server.launch.plugin.PluginCandidate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class PluginReporter {

    private static final String NEW_DETAILS_LINE = "\n\t\t";
    private static final String SEPARATOR = ": ";

    private PluginReporter() {
    }

    static String formatRequirements(Map<String, String> requirements) {
        StringBuilder builder = new StringBuilder();
        formatRequirements(builder, requirements);
        return builder.toString();
    }

    private static void formatRequirements(StringBuilder builder, Map<String, String> requirements) {
        checkArgument(!requirements.isEmpty(), "Requirements cannot be empty");

        boolean first = true;
        for (Map.Entry<String, String> entry : requirements.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }

            // Append plugin ID
            builder.append(entry.getKey());

            final String version = entry.getValue();
            if (version != null) {
                builder.append(" (Version ").append(version).append(')');
            }
        }
    }

    static RuntimeException crash(Throwable e, Collection<PluginCandidate> candidates) {
        CrashReport crash = CrashReport.makeCrashReport(e, "Loading Sponge plugins");
        CrashReportCategory category = crash.makeCategory("Plugins being loaded");

        StringBuilder pluginsBuilder = new StringBuilder();
        StringBuilder requirementsBuilder = new StringBuilder();
        StringBuilder dependenciesBuilder = new StringBuilder();

        for (PluginCandidate candidate : candidates) {
            pluginsBuilder.append(NEW_DETAILS_LINE).append(candidate);

            if (candidate.dependenciesCollected()) {
                Set<PluginCandidate> requirements = candidate.getRequirements();
                Map<String, String> missingRequirements = candidate.getMissingRequirements();

                if (!requirements.isEmpty() || !missingRequirements.isEmpty()) {
                    requirementsBuilder.append(NEW_DETAILS_LINE).append(candidate.getId()).append(SEPARATOR);

                    if (!requirements.isEmpty()) {
                        Map<String, String> versioned = new HashMap<>();
                        for (PluginCandidate requirement : requirements) {
                            versioned.put(requirement.getId(), candidate.getVersion(requirement.getId()));
                        }

                        formatRequirements(requirementsBuilder, versioned);
                        if (!missingRequirements.isEmpty()) {
                            requirementsBuilder.append(", ");
                        }
                    }

                    if (!missingRequirements.isEmpty()) {
                        requirementsBuilder.append("missing: ");
                        formatRequirements(requirementsBuilder, missingRequirements);
                    }
                }

                if (!candidate.getDependencies().isEmpty()) {
                    dependenciesBuilder.append(NEW_DETAILS_LINE).append(candidate.getId()).append(SEPARATOR).append(candidate.getDependencies());
                }
            }
        }

        category.addDetail("Plugins", pluginsBuilder);
        if (requirementsBuilder.length() > 0) {
            category.addDetail("Requirements", requirementsBuilder);
        }
        if (dependenciesBuilder.length() > 0) {
            category.addDetail("Dependencies", dependenciesBuilder);
        }

        throw new ReportedException(crash);
    }

}
