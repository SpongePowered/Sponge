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

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Writes {@link IntelliJRun}s as IntelliJ Application run configurations into {@code .idea/runConfigurations}.
 */
public abstract class GenerateIntelliJRuns extends DefaultTask {

    private Iterable<IntelliJRun> runs = List.of();

    /** Read when Gradle snapshots the task's inputs, so the runs are only realized when this task actually runs. */
    @Nested
    public List<IntelliJRun> getRuns() {
        final List<IntelliJRun> runs = new ArrayList<>();
        this.runs.forEach(runs::add);
        return runs;
    }

    public void setRuns(final Iterable<IntelliJRun> runs) {
        this.runs = runs;
    }

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void generate() {
        final File outputDirectory = this.getOutputDirectory().get().getAsFile();
        for (final IntelliJRun run : this.getRuns()) {
            final Set<File> classpath = new LinkedHashSet<>();
            run.getClasspath().forEach(file -> classpath.addAll(GenerateIntelliJRuns.toIntelliJOutput(file)));
            final Set<File> gradleClasspath = run.getClasspath().getFiles();
            final List<File> excluded = run.getModuleClasspath().getFiles().stream()
                .filter(file -> file.isFile() && !gradleClasspath.contains(file))
                .toList();

            final Document document;
            try {
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            } catch (final ParserConfigurationException e) {
                throw new IllegalStateException(e);
            }
            final Element component = GenerateIntelliJRuns.child(document, "component", "name", "ProjectRunConfigurationManager");
            final Element configuration = GenerateIntelliJRuns.child(component, "configuration",
                "default", "false", "name", run.getName(), "type", "Application", "factoryName", "Application");
            final Element envs = GenerateIntelliJRuns.child(configuration, "envs");
            run.getEnvironment().get().forEach((key, value) -> GenerateIntelliJRuns.child(envs, "env", "name", key, "value", value));
            GenerateIntelliJRuns.child(configuration, "option", "name", "MAIN_CLASS_NAME", "value", run.getMainClass().get());
            GenerateIntelliJRuns.child(configuration, "module", "name", run.getModule().get());
            GenerateIntelliJRuns.child(configuration, "option", "name", "PROGRAM_PARAMETERS", "value", GenerateIntelliJRuns.join(run.getArgs().get()));
            GenerateIntelliJRuns.child(configuration, "option", "name", "VM_PARAMETERS", "value", GenerateIntelliJRuns.join(run.getJvmArgs().get()));
            GenerateIntelliJRuns.child(configuration, "option", "name", "WORKING_DIRECTORY", "value", run.getWorkingDirectory().get());
            final Element classpathModifications = GenerateIntelliJRuns.child(configuration, "classpathModifications");
            classpath.forEach(file -> GenerateIntelliJRuns.child(classpathModifications, "entry", "path", file.getAbsolutePath()));
            excluded.forEach(file -> GenerateIntelliJRuns.child(classpathModifications, "entry", "exclude", "true", "path", file.getAbsolutePath()));
            // The anchor module doesn't depend on the rest of the run's modules, so build the whole project
            GenerateIntelliJRuns.child(GenerateIntelliJRuns.child(configuration, "method", "v", "2"), "option", "name", "MakeProject", "enabled", "true");

            final File file = new File(outputDirectory, run.getName().replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_|_$", "") + ".xml");
            try {
                final Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(new DOMSource(document), new StreamResult(file));
            } catch (final TransformerException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /** Appends a new element with the given alternating attribute names and values to {@code parent}. */
    private static Element child(final Node parent, final String name, final String... attributes) {
        final Document document = parent instanceof Document d ? d : parent.getOwnerDocument();
        final Element element = document.createElement(name);
        for (int i = 0; i < attributes.length; i += 2) {
            element.setAttribute(attributes[i], attributes[i + 1]);
        }
        parent.appendChild(element);
        return element;
    }

    /**
     * Maps a Gradle build output to what IntelliJ compiles to when it builds the project itself:
     * {@code {project}/build/classes/{lang}/{name}} and {@code {project}/build/resources/{name}} become
     * {@code {project}/out/{name}/classes|resources} ({@code main} is {@code production}), and a project's
     * jar in {@code {project}/build/libs} becomes that project's main outputs. Anything else is kept as-is.
     */
    static List<File> toIntelliJOutput(final File file) {
        final File parent = file.getParentFile();
        if (parent == null || parent.getParentFile() == null) {
            return List.of(file);
        }
        if (parent.getName().equals("resources") && parent.getParentFile().getName().equals("build")) {
            return List.of(GenerateIntelliJRuns.intelliJOutput(parent.getParentFile().getParentFile(), file.getName(), "resources"));
        }
        final File grandParent = parent.getParentFile();
        if (grandParent.getName().equals("classes") && grandParent.getParentFile() != null && grandParent.getParentFile().getName().equals("build")) {
            return List.of(GenerateIntelliJRuns.intelliJOutput(grandParent.getParentFile().getParentFile(), file.getName(), "classes"));
        }
        if (file.getName().endsWith(".jar") && parent.getName().equals("libs") && grandParent.getName().equals("build")) {
            final File project = grandParent.getParentFile();
            return List.of(GenerateIntelliJRuns.intelliJOutput(project, "main", "classes"), GenerateIntelliJRuns.intelliJOutput(project, "main", "resources"));
        }
        return List.of(file);
    }

    private static File intelliJOutput(final File project, final String sourceSet, final String kind) {
        return new File(project, "out/" + (sourceSet.equals("main") ? "production" : sourceSet) + "/" + kind);
    }

    /** Joins parameters the way IntelliJ parses them back: whitespace separated, quoted when needed. */
    private static String join(final List<String> parameters) {
        return parameters.stream()
            .map(parameter -> parameter.isEmpty() || parameter.chars().anyMatch(c -> Character.isWhitespace(c) || c == '"')
                ? '"' + parameter.replace("\"", "\\\"") + '"'
                : parameter)
            .collect(Collectors.joining(" "));
    }
}
