package org.spongepowered.gradle.impl;

import org.gradle.StartParameter;
import org.gradle.TaskExecutionRequest;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.DefaultTaskExecutionRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class IdeaIntegration {

    public static boolean isIdea() {
        return Boolean.getBoolean("idea.active");
    }

    public static boolean isIdeaSync() {
        return Boolean.getBoolean("idea.sync.active");
    }

    /**
     * Executes a task when Idea performs a project synchronization.
     *
     * @param project project of the task
     * @param supplier supplier that may provide a task
     */
    public static void addSynchronizationTask(final Project project, final Supplier<Optional<TaskProvider<?>>> supplier) {
        if (!IdeaIntegration.isIdeaSync()) {
            return;
        }

        project.afterEvaluate(p -> supplier.get().ifPresent(task -> {
            final StartParameter startParameter = project.getGradle().getStartParameter();
            final List<TaskExecutionRequest> taskRequests = new ArrayList<>(startParameter.getTaskRequests());

            taskRequests.add(new DefaultTaskExecutionRequest(Collections.singletonList((project == project.getRootProject() ? "" : project.getPath()) + ":" + task.getName())));
            startParameter.setTaskRequests(taskRequests);
        }));
    }
}
