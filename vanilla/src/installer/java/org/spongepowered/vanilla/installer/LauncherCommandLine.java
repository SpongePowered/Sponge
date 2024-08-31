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
package org.spongepowered.vanilla.installer;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class LauncherCommandLine {

    private static final OptionParser PARSER = new OptionParser();
    private static final ArgumentAcceptingOptionSpec<Path> INSTALLER_DIRECTORY_ARG = LauncherCommandLine.PARSER
        .accepts("installerDir", "Alternative installer directory").withRequiredArg()
        .withValuesConvertedBy(new PathConverter(PathProperties.DIRECTORY_EXISTING))
        .defaultsTo(Paths.get("."));
    private static final ArgumentAcceptingOptionSpec<Path> LIBRARIES_DIRECTORY_ARG = LauncherCommandLine.PARSER
        .accepts("librariesDir", "Alternative libraries directory").withRequiredArg()
        .withValuesConvertedBy(new PathConverter(PathProperties.DIRECTORY_EXISTING))
        .defaultsTo(Paths.get("libraries"));
    private static final ArgumentAcceptingOptionSpec<String> LAUNCH_TARGET_ARG = LauncherCommandLine.PARSER
        .accepts("launchTarget", "Launch target").withRequiredArg();
    private static final NonOptionArgumentSpec<String> REMAINDER = LauncherCommandLine.PARSER.nonOptions().ofType(String.class);

    static {
        LauncherCommandLine.PARSER.allowsUnrecognizedOptions();
    }

    public static Path installerDirectory, librariesDirectory;
    public static String launchTarget;
    public static List<String> remainingArgs;

    private LauncherCommandLine() {
    }

    public static void configure(final String[] args) {
        final OptionSet options = LauncherCommandLine.PARSER.parse(args);
        LauncherCommandLine.installerDirectory = options.valueOf(LauncherCommandLine.INSTALLER_DIRECTORY_ARG);
        LauncherCommandLine.librariesDirectory = options.valueOf(LauncherCommandLine.LIBRARIES_DIRECTORY_ARG);
        LauncherCommandLine.launchTarget = options.valueOf(LauncherCommandLine.LAUNCH_TARGET_ARG);
        LauncherCommandLine.remainingArgs = UnmodifiableCollections.copyOf(options.valuesOf(LauncherCommandLine.REMAINDER));
    }
}
