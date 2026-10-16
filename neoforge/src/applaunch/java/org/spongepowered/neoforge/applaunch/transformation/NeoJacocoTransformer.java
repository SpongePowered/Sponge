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
package org.spongepowered.neoforge.applaunch.transformation;

import net.neoforged.fml.classloading.ModuleClassLoader;
import net.neoforged.neoforgespi.transformation.ClassProcessor;
import net.neoforged.neoforgespi.transformation.ClassProcessorIds;
import net.neoforged.neoforgespi.transformation.ProcessorName;
import org.spongepowered.common.applaunch.transformation.jacoco.JacocoTransformer;

import java.util.Set;

/**
 * Coverage for the game layer under test, like the modlauncher plugin on Vanilla and Forge.
 * It instruments the original class bytes, so it runs before anything else changes them.
 */
public class NeoJacocoTransformer extends JacocoTransformer implements ClassProcessor {
    private static final ProcessorName NAME = new ProcessorName("neoforge", JacocoTransformer.NAME);

    @Override
    public ProcessorName name() {
        return NeoJacocoTransformer.NAME;
    }

    // Every other processor, Sponge's included, runs after frame computation by default
    @Override
    public Set<ProcessorName> runsBefore() {
        return Set.of(ClassProcessorIds.COMPUTING_FRAMES, ClassProcessorIds.ACCESS_TRANSFORMERS, ClassProcessorIds.MIXIN);
    }

    @Override
    public Set<ProcessorName> runsAfter() {
        return Set.of();
    }

    @Override
    protected boolean isTransformingClassloader(final ClassLoader loader) {
        return loader instanceof ModuleClassLoader;
    }

    @Override
    public boolean handlesClass(final SelectionContext context) {
        return !this.disabled && !context.empty();
    }

    @Override
    public ComputeFlags processClass(final TransformationContext context) {
        return this.transform(context.node()) ? ComputeFlags.SIMPLE_REWRITE : ComputeFlags.NO_REWRITE;
    }
}
