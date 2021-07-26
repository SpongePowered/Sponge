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
package org.spongepowered.fabric.installer;

import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.tinylog.Logger;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public final class InstallerMain {

    private final Installer installer;

    public InstallerMain(final String[] args) throws Exception {
        LauncherCommandLine.configure(args);
        this.installer = new Installer(LauncherCommandLine.installerDirectory);
    }

    public static void main(final String[] args) throws Exception {
        new InstallerMain(args).run();
    }

    public void run() {
        try  {
            this.downloadAndRun();
        } catch (final Exception ex) {
            Logger.error(ex, "Failed to download Sponge libraries and/or Minecraft");
            System.exit(2);
        } finally {
            this.installer.getLibraryManager().finishedProcessing();
        }
    }

    public void downloadAndRun() throws Exception {
        try {
            this.installer.getLibraryManager().validate();
        } catch (final ExecutionException ex) {
            final /* @Nullable */ Throwable cause = ex.getCause();
        }
        this.installer.getLibraryManager().finishedProcessing();

        Logger.info("Environment has been verified.");

        this.installer.getLibraryManager().getAll().values().stream()
            .map(LibraryManager.Library::getFile)
            .forEach(path -> {
                Logger.debug("Adding jar {} to classpath", path);
                FabricLauncherBase.getLauncher().addToClassPath(path);
            });

        final List<String> gameArgs = new ArrayList<>(LauncherCommandLine.remainingArgs);
        Collections.addAll(gameArgs, this.installer.getLauncherConfig().args.split(" "));

        final String className = "org.spongepowered.fabric.applaunch.Main";
        InstallerMain.invokeMain(className, gameArgs.toArray(new String[0]));
    }

    private static void invokeMain(final String className, final String[] args) {
        try {
            Class.forName(className)
                .getMethod("main", String[].class)
                .invoke(null, (Object) args);
        } catch (final InvocationTargetException ex) {
            Logger.error(ex.getCause(), "Failed to invoke main class {} due to an error", className);
            System.exit(1);
        } catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException ex) {
            Logger.error(ex, "Failed to invoke main class {} due to an error", className);
            System.exit(1);
        }
    }
}
