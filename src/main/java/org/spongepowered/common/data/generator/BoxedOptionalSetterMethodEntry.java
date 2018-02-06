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
package org.spongepowered.common.data.generator;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.PUTFIELD;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.common.data.InternalCopies;

import java.lang.reflect.Method;

final class BoxedOptionalSetterMethodEntry extends AbstractSetterMethodEntry {

    BoxedOptionalSetterMethodEntry(Method method, KeyEntry keyEntry) {
        super(method, keyEntry);
    }

    @Override
    void preVisit(MethodVisitor mv, String targetInternalName, String mutableInternalName) {
        // Add the nullable annotation, is forced to be present in the interfaces
        mv.visitParameterAnnotation(0, "Ljavax/annotation/Nullable;", true).visitEnd();
    }

    @Override
    void visit0(MethodVisitor mv, String targetInternalName, String mutableInternalName) {
        // Load the parameter
        mv.visitVarInsn(ALOAD, 1);
        // Create a copy before setting the object
        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(InternalCopies.class),
                mutableInternalName.equals(targetInternalName) ? "mutableCopyNullable" : "immutableCopyNullable",
                "(Ljava/lang/Object;)Ljava/lang/Object;", false);
        // Put the parameter into a optional
        mv.visitMethodInsn(INVOKESTATIC, "java/util/Optional", "ofNullable", "(Ljava/lang/Object;)Ljava/util/Optional;", false);
        // Put it in the field
        mv.visitFieldInsn(PUTFIELD, targetInternalName, this.keyEntry.valueFieldName, this.keyEntry.valueFieldDescriptor);
    }
}
