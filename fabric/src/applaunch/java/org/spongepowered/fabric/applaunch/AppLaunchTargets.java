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
package org.spongepowered.fabric.applaunch;

public enum AppLaunchTargets {
    CLIENT_DEVELOPMENT("sponge_client_dev"),
    CLIENT_PRODUCTION("sponge_client_prod"),
    SERVER_DEVELOPMENT("sponge_server_dev"),
    SERVER_PRODUCTION("sponge_server_prod"),
    CLIENT_INTEGRATION_TEST("sponge_client_it"),
    SERVER_INTEGRATION_TEST("sponge_server_it");

    private final String launchTarget;

    AppLaunchTargets(final String launchTarget) {
        this.launchTarget = launchTarget;
    }

    public String getLaunchTarget() {
        return this.launchTarget;
    }

    public static AppLaunchTargets from(final String launchTarget) {

        switch (launchTarget) {
            case "sponge_client_dev":
                return AppLaunchTargets.CLIENT_DEVELOPMENT;
            case "sponge_client_prod":
                return AppLaunchTargets.CLIENT_PRODUCTION;
            case "sponge_server_dev":
                return AppLaunchTargets.SERVER_DEVELOPMENT;
            case "sponge_server_prod":
                return AppLaunchTargets.SERVER_PRODUCTION;
            case "sponge_client_it":
                return AppLaunchTargets.CLIENT_INTEGRATION_TEST;
            case "sponge_server_it":
                return AppLaunchTargets.SERVER_INTEGRATION_TEST;
        }

        return null;
    }
}
