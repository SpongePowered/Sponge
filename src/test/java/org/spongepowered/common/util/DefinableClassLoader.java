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
package org.spongepowered.common.util;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DefinableClassLoader extends ClassLoader {

    private final Map<String, byte[]> definedClasses;
    private final MethodHandles.Lookup lookup;

    public DefinableClassLoader(final ClassLoader parent) {
        super(parent);
        this.definedClasses = new ConcurrentHashMap<>();
        try {

            final var classWriter = new ClassWriter(0);
            classWriter.visit(
                Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                "org/spongepowered/common/util/DefinableClassLoader$Holder", null,
                "java/lang/Object", null
            );

            classWriter.visitNestHost("org/spongepowered/common/util/DefinableClassLoader");

            classWriter.visitInnerClass(
                "java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles", "Lookup",
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC
            );

            classWriter.visitInnerClass(
                "org/spongepowered/common/util/DefinableClassLoader$Holder",
                "org/spongepowered/common/util/DefinableClassLoader", "Holder",
                Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC
            );

            {
                final var fieldVisitor = classWriter.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC, "lookup",
                    "Ljava/lang/invoke/MethodHandles$Lookup;", null, null
                );
                fieldVisitor.visitEnd();
            }
            {
                final var init = classWriter.visitMethod(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null);
                init.visitCode();
                init.visitVarInsn(Opcodes.ALOAD, 0);
                init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                init.visitInsn(Opcodes.RETURN);
                init.visitMaxs(1, 1);
                init.visitEnd();
            }
            {
                final var clinit = classWriter.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                clinit.visitCode();
                clinit.visitMethodInsn(
                    Opcodes.INVOKESTATIC, "java/lang/invoke/MethodHandles", "lookup",
                    "()Ljava/lang/invoke/MethodHandles$Lookup;",
                    false
                );
                clinit.visitFieldInsn(
                    Opcodes.PUTSTATIC, "org/spongepowered/common/util/DefinableClassLoader$Holder", "lookup",
                    "Ljava/lang/invoke/MethodHandles$Lookup;"
                );
                clinit.visitInsn(Opcodes.RETURN);
                clinit.visitMaxs(1, 0);
                clinit.visitEnd();
            }
            final var holder = this.defineClass("org.spongepowered.common.util.DefinableClassLoader$Holder", classWriter.toByteArray());
            final var lookup = holder.getField("lookup");
            this.lookup = (MethodHandles.Lookup) lookup.get(null);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public MethodHandles.Lookup lookup() {
        return this.lookup;
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> defineClass(final String name, final byte[] b) {
        this.definedClasses.put(name, b);
        return (Class<T>) this.defineClass(name, b, 0, b.length);
    }


    @Nullable
    @Override
    public InputStream getResourceAsStream(final String name) {
        final String normalized = name.replace("/", ".").replace(".class", "");
        if (this.definedClasses.containsKey(normalized)) {
            final byte[] buf = this.definedClasses.get(normalized);
            final byte[] cloned = buf.clone();
            return new ByteArrayInputStream(cloned);
        }
        return super.getResourceAsStream(name);
    }
}
