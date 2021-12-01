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

import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;

public class JavaVersionCheckUtils {

    private static final int INVALID_VERSION = -1;
    private static final String REQUIRED_VERSION = "17";

    private static final String ERROR_MESSAGE = "We have detected that you are running Java version %s, which is not supported!!\n"
        + "In order to run Sponge (and Minecraft 1.17+), you **must** be running a Java runtime version %s or above.\n"
        + "This can be downloaded from AdoptOpenJDK: https://adoptopenjdk.net/?variant=openjdk17&jvmVariant=hotspot";

    public static void ensureJava17() {
        final String version = JavaVersionCheckUtils.getCurrentVersion();
        if (JavaVersionCheckUtils.getMajorVersion(version) < JavaVersionCheckUtils.getMajorVersion(JavaVersionCheckUtils.REQUIRED_VERSION)) {
            final String error = String.format(JavaVersionCheckUtils.ERROR_MESSAGE, version, JavaVersionCheckUtils.REQUIRED_VERSION);

            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null, error, "PEBKACException!", JOptionPane.ERROR_MESSAGE);
            }
            throw new RuntimeException(error);
        }
    }

    private static String getCurrentVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Simple metric to convert java versions to integers
     *
     * @param version The Java version
     * @return The double value of the Java version
     */
    private static int getMajorVersion(String version) {
        // Get rid of any dashes, such as those in early access versions which have "-ea" on the end of the version
        if(version.contains("-")) {
            version = version.substring(0, version.indexOf('-'));
        }
        // Replace underscores with periods for easier String splitting
        version = version.replace('_', '.');
        // Split the version up into parts
        final String[] versionParts = version.split("\\.", -1);
        if (versionParts.length == 0) {
            return JavaVersionCheckUtils.INVALID_VERSION;
        }

        final int majorVersion = JavaVersionCheckUtils.tryParseInt(versionParts[0]);
        if (majorVersion == 1 && versionParts.length > 1) { // legacy versions are 1.x
            return JavaVersionCheckUtils.tryParseInt(versionParts[1]);
        }

        return majorVersion;
    }

    private static int tryParseInt(final String input) {
        try {
            return Integer.parseInt(input);
        } catch (final NumberFormatException ex) {
            return JavaVersionCheckUtils.INVALID_VERSION;
        }
    }

}
