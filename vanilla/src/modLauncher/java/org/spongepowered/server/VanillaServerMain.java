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
package org.spongepowered.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.jar.Manifest;

public class VanillaServerMain {

    private static class Hidden {

        static void runLauncher(final String[] args) {
            try {
                cpw.mods.modlauncher.Launcher.main(args);
            } catch (final cpw.mods.modlauncher.InvalidLauncherSetupException e) {
                System.err.println("Server is missing files, please re-download the server.");
                System.exit(1);
            }
        }
    }

    public static void main(final String[] args) {
        try {
            Class.forName("cpw.mods.modlauncher.Launcher", false, ClassLoader.getSystemClassLoader());
        } catch (final ClassNotFoundException e) {
            System.err.println("FATAL, You might not have a valid SpongeVanilla jar.");
            System.exit(1);
        }

        final String launchArgs = Optional.ofNullable(
            VanillaServerMain.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF"))
            .map(VanillaServerMain::fromInput)
            .map(Manifest::getMainAttributes)
            .map(attributes -> attributes.getValue("ServerLaunchArgs"))
            .orElseThrow(() -> new RuntimeException("Missing manifest"));
        final String[] defaultArgs = launchArgs.split(" ");
        final String[] joinedArgs = new String[args.length + defaultArgs.length];
        System.arraycopy(defaultArgs, 0, joinedArgs, 0, defaultArgs.length);
        System.arraycopy(args, 0, joinedArgs, defaultArgs.length, args.length);
        Hidden.runLauncher(joinedArgs);
    }

    private static Manifest fromInput(final InputStream inputStream) {
        try {
            return new Manifest(inputStream);
        } catch (final IOException e) {
            return null;
        }
    }
}
