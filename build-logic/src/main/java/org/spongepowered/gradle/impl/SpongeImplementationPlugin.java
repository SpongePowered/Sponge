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
package org.spongepowered.gradle.impl;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.jetbrains.gradle.ext.ProjectSettings;
import org.jetbrains.gradle.ext.TaskTriggersConfig;

/**
 * Set up the appropriate variants of a project (accessors, main, mixins, launch, applaunch)
 */
public class SpongeImplementationPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project target) {
        target.getExtensions().create("spongeImpl", SpongeImplementationExtension.class, target, target.getLogger());

        final NamedDomainObjectContainer<IntelliJRun> intellijRuns = target.getObjects().domainObjectContainer(IntelliJRun.class);
        target.getExtensions().add("intellijRuns", intellijRuns);
        final TaskProvider<GenerateIntelliJRuns> genIntelliJRuns = target.getTasks().register("genIntelliJRuns", GenerateIntelliJRuns.class, task -> {
            task.setGroup("ide");
            task.setDescription("Generates native IntelliJ Application run configurations");
            task.setRuns(intellijRuns);
            task.getOutputDirectory().set(target.getRootProject().getLayout().getProjectDirectory().dir(".idea/runConfigurations"));
        });

        // Regenerate on every IDE sync, like VanillaGradle and ModDevGradle do for their runs. The trigger needs the realized
        // task, so only register it while syncing to keep regular builds from configuring it
        target.afterEvaluate(p -> {
            if (intellijRuns.isEmpty() || !IdeHelper.isIdeaSync()) {
                return;
            }
            final Project root = p.getRootProject();
            root.getPluginManager().withPlugin("org.jetbrains.gradle.plugin.idea-ext", ideaExt -> {
                final ProjectSettings settings = ((ExtensionAware) root.getExtensions().getByType(IdeaModel.class).getProject()).getExtensions().getByType(ProjectSettings.class);
                ((ExtensionAware) settings).getExtensions().getByType(TaskTriggersConfig.class).afterSync(genIntelliJRuns.get());
            });
        });
    }
}
