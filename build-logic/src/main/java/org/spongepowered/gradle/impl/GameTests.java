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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.internal.tasks.testing.filter.DefaultTestFilter;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.plugins.ide.idea.model.IdeaModule;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension;
import org.gradle.testing.jacoco.tasks.JacocoReport;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Minecraft gametests, run through JUnit so they report and cover like the platforms' unit tests. The common
 * gametests live in the root project; each platform adds its own and runs both from a {@code gameTest} task.
 */
final class GameTests {

    static final String SOURCE_SET = "gametest";
    private static final String TASK = "gameTest";
    private static final String REPORT = "jacocoTestReport";

    private GameTests() {
    }

    // Gametests run through JUnit, so IntelliJ shows them as test sources
    static void markTestSources(final Project project) {
        project.getPlugins().withType(JavaPlugin.class, java -> GameTests.sourceSets(project)
            .matching(set -> GameTests.SOURCE_SET.equals(set.getName()))
            .all(set -> {
                project.getPluginManager().apply(IdeaPlugin.class);
                final IdeaModule module = project.getExtensions().getByType(IdeaModel.class).getModule();
                module.getTestSources().from(project.provider(() -> set.getJava().getSrcDirs()));
                module.getTestResources().from(project.provider(() -> set.getResources().getSrcDirs()));
            }));
    }

    /** A platform's own gametests and gametest mixins; the common gametests come along through the parent's */
    static SourceSet sourceSet(final Project project, final SpongeImplementationExtension impl) {
        final SourceSetContainer sets = GameTests.sourceSets(project);
        final SourceSetContainer common = GameTests.sourceSets(GameTests.parent(project));
        final ConfigurationContainer configurations = project.getConfigurations();
        return sets.create(GameTests.SOURCE_SET, set -> {
            impl.addDependencyToImplementation(common.getByName(GameTests.SOURCE_SET), set);
            impl.addDependencyToImplementation(sets.getByName(SourceSet.MAIN_SOURCE_SET_NAME), set);
            impl.addDependencyToImplementation(common.getByName("mixins"), set);
            impl.addDependencyToImplementation(sets.getByName("mixins"), set);

            configurations.named(set.getImplementationConfigurationName(),
                c -> c.extendsFrom(configurations.getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)));
            configurations.named(set.getRuntimeOnlyConfigurationName(),
                c -> c.extendsFrom(configurations.getByName(JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME)));
            // The common compile-only dependencies, JUnit's @Testable among them, which IntelliJ needs to offer run icons
            final Configuration commonCompileOnly = GameTests.parent(project).getConfigurations()
                .getByName(common.getByName(GameTests.SOURCE_SET).getCompileOnlyConfigurationName());
            configurations.named(set.getCompileOnlyConfigurationName(),
                c -> c.getDependencies().addAllLater(project.provider(commonCompileOnly::getDependencies)));
        });
    }

    static void register(final Project project, final Action<? super GameTestSettings> configure) {
        final GameTestSettings settings = project.getObjects().newInstance(GameTestSettings.class);
        configure.execute(settings);

        final SourceSetContainer sets = GameTests.sourceSets(project);
        final SourceSet gametest = sets.getByName(GameTests.SOURCE_SET);
        final SourceSet common = GameTests.sourceSets(GameTests.parent(project)).getByName(GameTests.SOURCE_SET);
        final TaskProvider<Test> task = project.getTasks().register(GameTests.TASK, Test.class, test -> {
            test.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
            test.setDescription("Runs the common and " + project.getName() + " gametests through JUnit");
            test.useJUnitPlatform();
            test.setMaxHeapSize("4G");
            test.setTestClassesDirs(common.getOutput().getClassesDirs().plus(gametest.getOutput().getClassesDirs()));
            test.exclude("**/mixin/**"); // mixin classes must never be loaded directly
            // The platform test boot launches the game; JUnit itself comes from the test runtime
            test.setClasspath(gametest.getRuntimeClasspath().plus(sets.getByName(SourceSet.TEST_SOURCE_SET_NAME).getRuntimeClasspath()));
            test.getJvmArgumentProviders().add(new BootArguments(settings));
            test.setWorkingDir(project.getLayout().getBuildDirectory().dir("gametest-run"));
            test.getTimeout().set(Duration.ofMinutes(15));
            test.doFirst(new ResetWorkingDirectory());
            test.dependsOn(settings.dependencies().toArray());

            final JacocoTaskExtension jacoco = test.getExtensions().findByType(JacocoTaskExtension.class);
            if (jacoco != null) {
                jacoco.setExcludeClassLoaders(List.of(settings.getGameClassLoader().get()));
                test.finalizedBy(GameTests.REPORT);
            }
        });

        project.getPlugins().withId("jacoco", jacoco -> project.getTasks().named(GameTests.REPORT, JacocoReport.class, report -> {
            // Combines whichever of test and gameTest have run
            report.getExecutionData().setFrom(project.fileTree(project.getLayout().getBuildDirectory().dir("jacoco")).include("*.exec"));
            // JaCoCo only adds pages, so packages no longer reported would otherwise linger
            report.doFirst(new DeleteHtmlReport());
            report.mustRunAfter(project.getTasks().named(JavaPlugin.TEST_TASK_NAME), task);
        }));

        GameTests.registerCommonTask(project, settings, common, task);
    }

    // IntelliJ runs a test through a Test task of the project that owns its sources, else with its own launcher,
    // which can't boot the game. This one covers the common gametests for one platform, so IntelliJ asks which
    // platform to run on, but runs nothing itself: it hands its test selection to the platform's gameTest.
    private static void registerCommonTask(final Project project, final GameTestSettings settings, final SourceSet common,
        final TaskProvider<Test> platform
    ) {
        final Project parent = GameTests.parent(project);
        parent.getTasks().register(GameTests.TASK + settings.getPlatformName().get(), Test.class, forward -> {
            forward.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
            forward.setDescription("Runs the selected common gametests on " + project.getName());
            forward.setTestClassesDirs(common.getOutput().getClassesDirs());
            forward.exclude("**/*");
            forward.setClasspath(parent.files()); // runs nothing itself
            forward.getFilter().setFailOnNoMatchingTests(false);
            forward.dependsOn(platform);
            platform.configure(test -> {
                // The selection isn't an input of the platform task, so it must not be skipped as up to date for this one
                test.getOutputs().upToDateWhen(new NotRunFor(forward));
                test.doFirst(new CopySelection(forward));
            });
        });
    }

    private static SourceSetContainer sourceSets(final Project project) {
        return project.getExtensions().getByType(SourceSetContainer.class);
    }

    private static Project parent(final Project project) {
        return Objects.requireNonNull(project.getParent(), () -> project + " has gametests but no common project above it");
    }

    private static void deleteRecursively(final File directory) {
        if (!directory.exists()) {
            return;
        }
        try (final Stream<Path> paths = Files.walk(directory.toPath())) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The platform's run arguments plus what the test boot and Sponge's JaCoCo instrumentation read */
    private static final class BootArguments implements CommandLineArgumentProvider {
        private final GameTestSettings settings;

        BootArguments(final GameTestSettings settings) {
            this.settings = settings;
        }

        @Input
        public List<String> getArguments() {
            final List<String> arguments = new ArrayList<>(this.settings.getJvmArguments().get());
            arguments.add("-Dsponge.test.args=" + String.join(" ", this.settings.getBootArguments().get()));
            this.settings.getSystemProperties().get().forEach((name, value) -> arguments.add("-D" + name + "=" + value));
            arguments.add("-Dsponge.jacoco.packages=org.spongepowered");
            arguments.add("-Djunit.platform.launcher.interceptors.enabled=true");
            return arguments;
        }

        @Override
        public Iterable<String> asArguments() {
            return this.getArguments();
        }
    }

    // A fresh server directory each run, with the EULA accepted
    private static final class ResetWorkingDirectory implements Action<Task> {
        @Override
        public void execute(final Task task) {
            final File directory = ((Test) task).getWorkingDir();
            GameTests.deleteRecursively(directory);
            try {
                Files.createDirectories(directory.toPath());
                Files.writeString(directory.toPath().resolve("eula.txt"), "eula=true");
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static final class DeleteHtmlReport implements Action<Task> {
        @Override
        public void execute(final Task task) {
            GameTests.deleteRecursively(((JacocoReport) task).getReports().getHtml().getOutputLocation().get().getAsFile());
        }
    }

    private static final class NotRunFor implements Spec<Task> {
        private final Task forward;

        NotRunFor(final Task forward) {
            this.forward = forward;
        }

        @Override
        public boolean isSatisfiedBy(final Task task) {
            return !task.getProject().getGradle().getTaskGraph().hasTask(this.forward);
        }
    }

    // Copied as the platform task starts, since IntelliJ selects tests through the tooling API's TestLauncher, which
    // only sets the forwarding task's filter once the task graph is final. Gradle has no public way to hand it on.
    private static final class CopySelection implements Action<Task> {
        private final Test forward;

        CopySelection(final Test forward) {
            this.forward = forward;
        }

        @Override
        public void execute(final Task task) {
            final DefaultTestFilter from = (DefaultTestFilter) this.forward.getFilter();
            final DefaultTestFilter to = (DefaultTestFilter) ((Test) task).getFilter();
            to.setCommandLineIncludePatterns(from.getCommandLineIncludePatterns());
            from.getIncludePatterns().forEach(to::includeTestsMatching);
        }
    }
}
