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
package org.spongepowered.bootstrap.forge;

import cpw.mods.jarhandling.SecureJar;

public class ForgeBootstrap extends ForgeLikeBootstrap {

    public ForgeBootstrap(final String[] args) {
        super(args);
    }

    @Override
    public String name() {
        return "Forge";
    }

    @Override
    protected boolean filterApplicationModule(final SecureJar jar) {
        final SecureJar.ModuleDataProvider data = jar.moduleDataProvider();
        return data.findFile("META-INF/mods.toml").isPresent() || data.findFile("net/minecraft/client/main/Main.class").isPresent()
            || data.getManifest().getMainAttributes().getValue("FMLModType") != null;
    }

    public static void main(final String[] args) throws Exception {
        new ForgeBootstrap(args).devBoot(true);
    }
}
