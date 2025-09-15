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
package org.spongepowered.transformers.modlauncher.jacoco;

import cpw.mods.modlauncher.TransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class JacocoPluginService implements ILaunchPluginService {
    private static final Logger logger = LogManager.getLogger();

    private static final String MIXIN_DESC = "Lorg/spongepowered/asm/mixin/Mixin;";

    private static final EnumSet<Phase> YAY = EnumSet.of(Phase.BEFORE);
    private static final EnumSet<Phase> NAY = EnumSet.noneOf(Phase.class);

    private boolean disabled;
    private String[] packages;
    private ClassLoader loader;

    public JacocoPluginService() {
        try {
            getClass().getClassLoader().loadClass("org.jacoco.core.JaCoCo");
        } catch (final ClassNotFoundException e) {
            this.disabled = true;
            return;
        }

        this.packages = System.getProperty("sponge.jacoco.packages", "").split(",");
        for (int i = 0; i < this.packages.length; i++) {
            final String pkg = this.packages[i].replace('.', '/');
            if (!pkg.endsWith("/")) {
                this.packages[i] = pkg + "/";
            }
        }
    }

    @Override
    public String name() {
        return "jacoco";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return this.disabled || isEmpty ? NAY : YAY;
    }

    private boolean testClassName(final String name) {
        for (final String pkg : this.packages) {
            if (name.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int processClassWithFlags(final Phase phase, final ClassNode classNode, final Type classType, final String reason) {
        if (ITransformerActivity.COMPUTING_FRAMES_REASON.equals(reason)) {
            return ComputeFlags.NO_REWRITE;
        }

        // Capture a reference to the transforming class loader
        if (this.loader == null) {
            // First transformed class context, aka game entrypoint is supposed to be the transforming class loader
            final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            if (!((Object) contextLoader instanceof TransformingClassLoader)) { // Ignore IDE warning
                throw new IllegalStateException("Context class loader of the first transformed class is not a transforming class loader");
            }
            // Context class loader may change for next classes, so capture it
            this.loader = contextLoader;
        }

        // Do not instrument every class for two reasons:
        // - Performance
        // - JaCoCo changes the LVT which can cause mixins targeting the current class to fail
        if (!this.testClassName(classNode.name)) {
            return ComputeFlags.NO_REWRITE;
        }

        // We need the original bytes for two reasons:
        // - JaCoCo computes a checksum from the bytes
        // - JaCoCo requires expanded frames, which the current ClassNode doesn't have
        byte[] originalBytes;
        try (final InputStream in = this.loader.getResourceAsStream(classNode.name + ".class")) {
            if (in == null) {
                logger.warn("Failed to find original class bytes for class {}", classNode.name);
                return ComputeFlags.NO_REWRITE;
            }

            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            in.transferTo(buffer);
            originalBytes = buffer.toByteArray();
        } catch (IOException e) {
            logger.warn("Failed to read original class bytes for class {}", classNode.name, e);
            return ComputeFlags.NO_REWRITE;
        }

        // Detect the mixin annotation
        boolean mixin = false;
        if (classNode.invisibleAnnotations != null) {
            for (final AnnotationNode annotation : classNode.invisibleAnnotations) {
                if (MIXIN_DESC.equals(annotation.desc)) {
                    mixin = true;
                    break;
                }
            }
        }

        final byte[] instrumentedBytes = Instrumenter.instrument(originalBytes, mixin);
        final ClassReader classReader = new ClassReader(instrumentedBytes);
        final ClassNode instrumentedClassNode = new ClassNode();
        classReader.accept(instrumentedClassNode, 0);

        // Completely overwrite any previous transformation, luckily we are in phase BEFORE and all other transformers we use are in phase AFTER
        classNode.fields = instrumentedClassNode.fields;
        if (mixin) {
            // Mixin doesn't like JaCoCo instructions in <init> but mixin constructors are never called anyway
            List<MethodNode> methods = new ArrayList<>();
            for (final MethodNode method : classNode.methods) {
                if (method.name.equals("<init>")) {
                    methods.add(method);
                }
            }
            for (final MethodNode method : instrumentedClassNode.methods) {
                if (!method.name.equals("<init>")) {
                    methods.add(method);
                }
            }
            classNode.methods = methods;
        } else {
            classNode.methods = instrumentedClassNode.methods;
        }

        // JaCoCo has already updated the frames
        return ComputeFlags.SIMPLE_REWRITE;
    }


}
