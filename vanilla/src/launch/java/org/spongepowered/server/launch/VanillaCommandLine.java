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
package org.spongepowered.server.launch;

import static java.util.Arrays.asList;

import joptsimple.HelpFormatter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class VanillaCommandLine {

    private VanillaCommandLine() {
    }

    private static final OptionParser parser = new OptionParser();

    public static final OptionSpec<Void> HELP = parser.acceptsAll(asList("help", "h", "?"), "Show this help text").forHelp();
    public static final OptionSpec<Void> VERSION = parser.acceptsAll(asList("version", "v"), "Display the SpongeVanilla version");

    // Automatic dependency download
    public static final OptionSpec<Void> NO_DOWNLOAD = parser.accepts("no-download", "Do not auto-download required dependencies");
    public static final OptionSpec<Void> NO_VERIFY_CLASSPATH = parser.accepts("no-verify-classpath",
            "Don't check the classpath for required dependencies");

    // Console
    public static final OptionSpec<Void> NO_REDIRECT_STDOUT = parser.accepts("no-redirect-stdout", "Don't redirect standard output to the logger");

    // Launchwrapper
    public static final OptionSpec<String> TWEAK_CLASS = parser.accepts("tweakClass", "Tweak classes to load").withRequiredArg();

    public static final OptionSpec<String> ACCESS_TRANSFORMER = parser.accepts("at",
            "Additional access transformer files to apply").withRequiredArg();

    public static final OptionSpec<Void> SCAN_CLASSPATH = parser.accepts("scan-classpath", "Scan class directories in classpath for plugins");
    public static final OptionSpec<Void> SCAN_FULL_CLASSPATH = parser.accepts("scan-full-classpath", "Scan full classpath for plugins");

    // Vanilla Minecraft Server options
    // Note: --singleplayer and --demo are unsupported on SpongeVanilla (and probably have no use on the dedicated server anyway)
    public static final OptionSpec<Integer> PORT = parser.acceptsAll(asList("port", "p"), "The port to launch the server on")
            .withRequiredArg().ofType(Integer.class);
    public static final OptionSpec<File> WORLD_DIR = parser.accepts("universe", "The directory to store the world(s) in")
            .withRequiredArg().ofType(File.class);
    public static final OptionSpec<String> WORLD_NAME = parser.acceptsAll(asList("world", "w"), "The name of the main world for the server")
            .withRequiredArg();
    public static final OptionSpec<Void> BONUS_CHEST = parser.accepts("bonusChest", "Spawn a bonus chest in the generated world");

    private static Optional<OptionSet> options = Optional.empty();

    static {
        // Note: This is automatically parsed by Mixin but we add it to the command-line help
        parser.accepts("mixin", "Additional Mixin configs to load").withRequiredArg();
        parser.allowsUnrecognizedOptions();
    }

    public static Optional<OptionSet> getOptions() {
        return options;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getUnrecognizedOptions() {
        return (List<String>) getOptions().get().nonOptionArguments();
    }

    public static OptionSet parse(String... args) {
        if (options.isPresent()) {
            return options.get();
        }

        OptionSet parsed = parser.parse(args);
        options = Optional.of(parsed);
        return parsed;
    }

    public static OptionSet parse(Collection<String> args) {
        if (options.isPresent()) {
            return options.get();
        }

        OptionSet parsed = parser.parse(args.toArray(new String[args.size()]));
        options = Optional.of(parsed);
        return parsed;
    }

    public static void printHelp(OutputStream out) throws IOException {
        parser.printHelpOn(out);
    }

    public static void printHelp(HelpFormatter formatter, OutputStream out) throws IOException {
        parser.formatHelpWith(formatter);
        printHelp(out);
    }

}
