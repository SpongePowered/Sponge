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
package org.spongepowered.launch;

import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;

public class JavaVersionCheckUtils {

    private static final String REQUIRED_VERSION = "1.8.0_20";

    private static final String ERROR_MESSAGE = "We have detected that you are JRE version %s, which is not supported!!\n"
                                                + "In order to run Sponge, you **must** be running JRE version %s or above.\n"
                                                + "Older builds or newer Java major versions (like 9 or 10) are not supported.\n"
                                                + "This can be downloaded from Oracle: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html";

    public static void ensureJava8() {
        String version = getCurrentVersion();
        if (getVersionValue(version) < getVersionValue(REQUIRED_VERSION)) {
            if(!version.startsWith("1.8")) {
                String error = String.format(ERROR_MESSAGE, version, REQUIRED_VERSION);

                if (!GraphicsEnvironment.isHeadless()) {
                    JOptionPane.showMessageDialog(null, error, "PEBKACException!", JOptionPane.ERROR_MESSAGE);
                }
                throw new RuntimeException(error);
            }
            System.out.println("You may be running an outdated version of Java. Any crashes from Sponge may require an update to Java.");
        }
    }

    private static String getCurrentVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Calculates a double value based on a Java version string such that a
     * higher version will produce a higher value
     *
     * @param version The Java version
     * @return The double value of the Java version
     */
    private static double getVersionValue(String version) {
        // Get rid of any dashes, such as those in early access versions which have "-ea" on the end of the version
        if(version.contains("-")) {
            version = version.substring(0, version.indexOf('-'));
        }
        // Replace underscores with periods for easier String splitting
        version = version.replace('_', '.');
        // Split the version up into parts
        String[] versionParts = version.split("\\.");
        double versionValue = 0;
        for(int i = 0; i < versionParts.length; i++) {
            try {
                int part = Integer.valueOf(versionParts[i]);
                // The value of the part of the version is related to it's proximity to the beginning
                // Multiply by 3 to "pad" each of the parts a bit more so a higher value
                // of a less significant version part couldn't as easily outweight the
                // more significant version parts.
                versionValue += part * Math.pow(10, versionParts.length - (i - 1) * 3);
            } catch(NumberFormatException e) {
                continue;
            }
        }
        return versionValue;
    }
}
