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
package org.spongepowered.vanilla.applaunch;

import org.spongepowered.vanilla.applaunch.pipeline.AppPipeline;
import org.spongepowered.vanilla.applaunch.pipeline.ProductionServerAppPipeline;
import org.spongepowered.vanilla.applaunch.plugin.VanillaPluginEngine;

public final class Main {

    private static AppPipeline appPipeline;
    private static VanillaPluginEngine pluginEngine;
    
    public static void main(String[] args) throws Exception {
        final String[] launchArgs = VanillaCommandLine.configure(args);
        switch (VanillaCommandLine.launchTarget) {
            case CLIENT_DEVELOPMENT:
            case SERVER_DEVELOPMENT:
                Main.appPipeline = new AppPipeline();
                break;
            case SERVER_PRODUCTION:
                Main.appPipeline = new ProductionServerAppPipeline();
                break;
            default:
                throw new RuntimeException("Plebizou doesn't want to do a production SpongeVanilla client :'(");
        }

        Main.appPipeline.prepare();
        Main.pluginEngine = new VanillaPluginEngine(Main.appPipeline.getPluginEnvironment());
        Main.appPipeline.start(launchArgs);
    }

    public static VanillaPluginEngine getPluginEngine() {
        return Main.pluginEngine;
    }
}
