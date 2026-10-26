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
package org.spongepowered.common.test.gametest;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Applies the gametest mixins only to a game test run, which sets the {@code sponge.gametest} system property: the
 * vanilla game test server from its launch handler, the dev runs from their JVM arguments. The gametest modules are
 * plugins, so a copy dropped into a production server would otherwise change it.
 */
public final class GameTestMixinPlugin implements IMixinConfigPlugin {

    public static final String PROPERTY = "sponge.gametest";

    private static final Logger LOGGER = LogManager.getLogger("sponge");
    private static boolean warned;

    @Override
    public void onLoad(final String mixinPackage) {
        MixinExtrasBootstrap.init();
    }

    // Read as each mixin applies, not when the config loads: Mixin loads configs before the launch handler runs
    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        if (Boolean.getBoolean(GameTestMixinPlugin.PROPERTY)) {
            return true;
        }
        if (!GameTestMixinPlugin.warned) {
            GameTestMixinPlugin.warned = true;
            LOGGER.warn("Not a game test run (-D{}=true): the gametest mixins stay inactive", GameTestMixinPlugin.PROPERTY);
        }
        return false;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
    }
}
