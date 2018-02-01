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
package org.spongepowered.common.data.generator.method;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.common.data.generator.KeyEntry;

import java.lang.reflect.Method;

public abstract class MethodEntry {

    protected final Method method;
    protected final KeyEntry keyEntry;

    public MethodEntry(Method method, KeyEntry keyEntry) {
        this.keyEntry = keyEntry;
        this.method = method;
    }

    public void visit(ClassVisitor classVisitor, String implClassName, String mutableImplClassName) {
        final MethodVisitor mv = classVisitor.visitMethod(ACC_PUBLIC, this.method.getName(),
                Type.getMethodDescriptor(this.method), null, null);
        preVisit(mv, implClassName, mutableImplClassName);
        mv.visitCode();
        visit(mv, implClassName, mutableImplClassName);
        mv.visitEnd();
    }

    public void preVisit(MethodVisitor mv, String implClassName, String mutableImplClassName) {
    }

    public abstract void visit(MethodVisitor methodVisitor, String implClassDescriptor, String mutableImplClassName);
}
