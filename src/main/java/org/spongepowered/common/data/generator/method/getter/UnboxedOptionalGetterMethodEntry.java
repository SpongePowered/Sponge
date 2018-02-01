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
package org.spongepowered.common.data.generator.method.getter;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.common.data.generator.KeyEntry;
import org.spongepowered.common.data.generator.method.MethodEntry;

import java.lang.reflect.Method;

public class UnboxedOptionalGetterMethodEntry extends MethodEntry {

    public UnboxedOptionalGetterMethodEntry(Method method, KeyEntry keyEntry) {
        super(method, keyEntry);
    }

    @Override
    public void preVisit(MethodVisitor mv, String implClassDescriptor, String mutableImplClassName) {
        // Add the nullable annotation, is forced to be present in the interfaces
        mv.visitAnnotation("Ljavax/annotation/Nullable;", true).visitEnd();
    }

    @Override
    public void visit(MethodVisitor mv, String implClassDescriptor, String mutableImplClassName) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassDescriptor, this.keyEntry.valueFieldName, "Ljava/util/Optional;");
        mv.visitInsn(ACONST_NULL);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "orElse", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(this.method.getReturnType()));
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 1);
    }
}
