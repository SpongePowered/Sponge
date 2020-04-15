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
package org.spongepowered.common.modlauncher.loading;

import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class SpongeModLauncherLaunchService implements ILaunchPluginService {

    private static final List<String> PROTECTED_PACKAGES = Arrays.asList(
        "org.spongepowered.common.event.tracking.",
        "org.spongepowered.common.launch.",
        "org.spongepowered.common.modLauncher.",
        "org.spongepowered.common.util."
    );

    public static final String NAME = "spongecommon";

    @Override
    public String name() {
        return SpongeModLauncherLaunchService.NAME;
    }

    @Override
    public EnumSet<Phase> handlesClass(final Type classType, final boolean isEmpty) {
        return null;
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty, String reason) {
        return EnumSet.noneOf(Phase.class);
    }

    @Override
    public boolean processClass(final Phase phase, final ClassNode classNode, final Type classType) {
        throw new UnsupportedOperationException("Outdated ModLauncher");
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType, String reason) {
        return false;
    }

    @Override
    public void initializeLaunch(final ITransformerLoader transformerLoader, final Path[] specialPaths) {
        final TransformingClassLoader classLoader = (TransformingClassLoader) Thread.currentThread().getContextClassLoader();
        classLoader.addTargetPackageFilter(name -> PROTECTED_PACKAGES.stream().noneMatch(name::startsWith));
    }
}
