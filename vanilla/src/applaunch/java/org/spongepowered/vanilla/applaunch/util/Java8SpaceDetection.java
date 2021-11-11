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
package org.spongepowered.vanilla.applaunch.util;

import org.apache.logging.log4j.Level;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.util.PrettyPrinter;

public final class Java8SpaceDetection {

    public static void check() {
        // If we're running Java 8, the actual version is 1.8, if we're running
        // Java 9+, the version starts with that version number.
        // Thus, we can just check to see if the major version is version, which
        // we signify by checking to see if the version string starts with 1.
        final String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.")) {
            final String location = Java8SpaceDetection.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
            if (location.contains(" ") || location.contains("%20")) {
                // this is going to fail, so bomb out now.
                new PrettyPrinter(100)
                        .add("UNABLE TO START SPONGE - SPACE IN PATH RUNNING ON JAVA 8").centre()
                        .hr()
                        .add("We have detected the following situation where Sponge is unable to load:")
                        .add()
                        .add("* You are running Java 8; and")
                        .add("* You have placed Sponge in a folder that has a space in its name (or one of its parent folders does); or")
                        .add("* Your Sponge jar file has a space in its name.")
                        .add()
                        .add("To allow Sponge to run, do ONE of the following:")
                        .add()
                        .add("* Upgrade your version of Java to at least version 9 (though we recommend Java 17); or")
                        .add("* Make sure that the names of your Sponge jar and all parent folders do not contain any spaces.")
                        .add()
                        .add("Once you have done one of the above, try again.")
                        .hr()
                        .add("Technical Details:")
                        .add("* Detected Java Version: %s", javaVersion)
                        .add("* Detected JAR location: %s", location)
                        .log(AppLaunch.logger(), Level.FATAL);
                // We force and exit with an error code to try to indicate to server panels that something is wrong.
                // We don't use an exception here because we don't want to spew an error after this message,
                // the above message is the most important thing and we don't want users to miss it.
                System.exit(1);
            }
        }
    }

}
