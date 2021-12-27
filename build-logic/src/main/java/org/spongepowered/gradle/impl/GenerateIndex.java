package org.spongepowered.gradle.impl;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// generate a bundler-style index for local libs
// todo: unify with the json index we use for downloaded libs
public abstract class GenerateIndex extends DefaultTask {

    @InputFiles
    public abstract ConfigurableFileCollection getBundledArtifacts();

    @OutputFile
    public abstract RegularFileProperty getIndex();

    @TaskAction
    public void doGenerate() throws IOException {
        final Path output = this.getIndex().get().getAsFile().toPath();
        Files.createDirectories(output.getParent());

        try (final BufferedWriter writer = Files.newBufferedWriter(output)) {
            for (final File artifact : this.getBundledArtifacts()) {
                final String shaHash;
                try (final InputStream in = new FileInputStream(artifact)) {
                    final MessageDigest hasher = MessageDigest.getInstance("SHA-256");
                    final byte[] buf = new byte[4096];
                    int read;
                    while ((read = in.read(buf)) != -1) {
                        hasher.update(buf, 0, read);
                    }

                    shaHash = OutputDependenciesToJson.toHexString(hasher.digest());
                } catch (final IOException | NoSuchAlgorithmException ex) {
                    throw new GradleException("Failed to create hash for " + artifact, ex);
                }
                // sha256<tab>real name<tab>sanitized name<LF>
                writer.write(shaHash + "\t" + artifact.getName() + "\t" + artifact.getName().replace(".jar", "") + "\n");
            }
        }
    }


}
