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
package org.spongepowered.mlpatcher;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Set;

public class AsmFixerAgent {
    private static final int ASM_VERSION = Opcodes.ASM9;

    private static Instrumentation instrumentation;

    public static void premain(final String agentArgs, final Instrumentation instrumentation) {
        AsmFixerAgent.instrumentation = instrumentation;
        AsmFixerAgent.setup();
    }

    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
        AsmFixerAgent.instrumentation = instrumentation;
        AsmFixerAgent.setup();
    }

    private static void setup() {
        AsmFixerAgent.instrumentation.addTransformer(new MLFixer());
    }

    private AsmFixerAgent() {
    }

    private static class MLFixer implements ClassFileTransformer {

        record PatchInfo(Set<String> methodsToPatch) {

            public PatchInfo(final String... methods) {
                this(Set.of(methods));
            }
        }

        private final Map<String, PatchInfo> patch = Map.of(
            "cpw/mods/modlauncher/TransformerClassWriter$SuperCollectingVisitor", new PatchInfo("<init>"),
            "cpw/mods/modlauncher/ClassTransformer", new PatchInfo("transform"),
            "cpw/mods/modlauncher/PredicateVisitor", new PatchInfo("<init>")
        );

        @Override
        public byte[] transform(
            final Module module,
            final ClassLoader loader,
            final String className,
            final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain,
            final byte[] classfileBuffer
        ) throws IllegalClassFormatException {
            if (!this.patch.containsKey(className)) {
                return classfileBuffer;
            }

            final ClassReader reader = new ClassReader(classfileBuffer);
            final ClassWriter writer = new ClassWriter(reader, 0);

            reader.accept(new Cls(writer, this.patch.get(className)), 0);

            return writer.toByteArray();
        }

        private static class Cls extends ClassVisitor {
            private final PatchInfo data;
            public Cls(final ClassVisitor parent, final PatchInfo data) {
                super(AsmFixerAgent.ASM_VERSION, parent);
                this.data = data;
            }

            @Override
            public MethodVisitor visitMethod(
                final int access,
                final String name,
                final String descriptor,
                final String signature,
                final String[] exceptions
            ) {
                final MethodVisitor parent = super.visitMethod(
                    access,
                    name,
                    descriptor,
                    signature,
                    exceptions
                );

                if (this.data.methodsToPatch.contains(name)) {
                    return new Method(parent);
                } else {
                    return parent;
                }

            }

        }

        private static class Method extends MethodVisitor {
            public Method(final MethodVisitor parent) {
                super(AsmFixerAgent.ASM_VERSION, parent);
            }

            @Override
            public void visitLdcInsn(final Object value) {
                if (value instanceof Integer && ((Integer) value).intValue() == Opcodes.ASM7) {
                    super.visitLdcInsn(AsmFixerAgent.ASM_VERSION);
                } else {
                    super.visitLdcInsn(value);
                }
            }
        }

    }


}
