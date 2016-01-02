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

    private static final String REQUIRED_VERSION = "1.8.0_40";

    private static final String ERROR_MESSAGE = "We have detected that you are JRE version %s, which is out of date!\n"
            + "In order to run Sponge, you **must** be running JRE version %s (or above).\n"
            + "Previous builds of JRE version 1.8 will not work with Sponge.\n"
            + "This can be downloaded from Oracle: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html";

    public static void ensureJava8() {
        String version = getCurrentVersion();
        if (version.compareTo(REQUIRED_VERSION) < 0) {
            String error = String.format(ERROR_MESSAGE, version, REQUIRED_VERSION);

            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null, error, "PEBKACException!", JOptionPane.ERROR_MESSAGE);
            }
            throw new RuntimeException(error);
        }
    }

    private static String getCurrentVersion() {
        return System.getProperty("java.version");
    }
}
