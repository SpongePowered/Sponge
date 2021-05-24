package org.spongepowered.gradle.impl;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.jetbrains.gradle.ext.IdeaExtPlugin;
import org.jetbrains.gradle.ext.ProjectSettings;
import org.jetbrains.gradle.ext.TaskTriggersConfig;

/**
 * Do templated resources, but in a way that's compatible with IDE run configs.
 */
public class TemplatedResourcesPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project target) {
        // for every source set, register a templateResources task
        // that task's output gets registered as a project task

        target.getPlugins().withType(JavaBasePlugin.class, pl -> this.withJavaBase(target));
    }

    private void withJavaBase(final Project target) {
        final SourceSetContainer sourceSets = target.getExtensions().getByType(SourceSetContainer.class);
        final TaskContainer tasks = target.getTasks();
        final ProjectLayout layout = target.getLayout();

        sourceSets.all(set -> {
            final Provider<Directory> destination = layout.getBuildDirectory().dir("generated/templateResources/" + set.getName());
            final Directory srcDir = layout.getProjectDirectory().dir("src/" + set.getName() + "/resourceTemplates");
            final TaskProvider<?> generateTask = tasks.register(set.getTaskName("template", "resources"), GenerateResourceTemplates.class, copy -> {
                copy.into(destination);
                copy.from(srcDir);
            });
            set.getResources().srcDir(generateTask.map(Task::getOutputs));
        });

        final TaskProvider<?> generateAllTask = tasks.register("allTemplateResource", t -> {
            t.dependsOn(tasks.withType(GenerateResourceTemplates.class));
        });

        this.configureIdeIntegrations(target, generateAllTask);
    }

    private void configureIdeIntegrations(final Project target, final TaskProvider<?> generateAllTask) {
        target.getPlugins().withType(EclipsePlugin.class, plug -> this.withEclipse(target.getExtensions().getByType(EclipseModel.class), generateAllTask));
        target.getPlugins().withType(IdeaExtPlugin.class, plug -> this.withIdea(target, generateAllTask));
    }

    private void withEclipse(final EclipseModel eclipse, final TaskProvider<?> generateAllTask) {
        eclipse.synchronizationTasks(generateAllTask);
    }

    private void withIdea(final Project target, final TaskProvider<?> generateAllTask) {
        if (!Boolean.getBoolean("idea.active")) {
            return;
        }

        // Apply the IDE plugin to the root project
        final Project rootProject = target.getRootProject();
        if (target != rootProject) {
            rootProject.getPlugins().apply(IdeaExtPlugin.class);
        }
        final IdeaModel model = rootProject.getExtensions().findByType(IdeaModel.class);
        if (model == null || model.getProject() == null) {
            return;
        }
        final ProjectSettings ideaExt = ((ExtensionAware) model.getProject()).getExtensions().getByType(ProjectSettings.class);

        // But actually perform the configuration with the subproject context
        final TaskTriggersConfig taskTriggers = ((ExtensionAware) ideaExt).getExtensions().getByType(TaskTriggersConfig.class);
        taskTriggers.afterSync(generateAllTask);
    }
}
