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
package org.spongepowered.common.event.filter.delegate;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.event.filter.data.Has;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

public class HasDataFilterDelegate implements ParameterFilterDelegate {

    private static final String HAS_DESC = Type.getMethodDescriptor(Type.getType(Optional.class), Type.getType(Key.class));
    private final Has anno;

    public HasDataFilterDelegate(final Has anno) {
        this.anno = anno;
    }

    @Override
    public void write(final ClassWriter cw, final MethodVisitor mv, final Method method, final Parameter param, final int localParam) {
        if (!ValueContainer.class.isAssignableFrom(param.getType())) {
            throw new IllegalStateException("Annotated type for data filter is not a ValueContainer");
        }
        mv.visitVarInsn(ALOAD, localParam);
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(ValueContainer.class));
        mv.visitFieldInsn(GETSTATIC, Type.getInternalName(this.anno.container()), this.anno.value(), Type.getDescriptor(Key.class));
        mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(param.getType()), "get", HasDataFilterDelegate.HAS_DESC, true);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "isPresent", "()Z", false);
        final Label success = new Label();
        if (this.anno.inverse()) {
            mv.visitJumpInsn(IFEQ, success);
        } else {
            mv.visitJumpInsn(IFNE, success);
        }
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitLabel(success);
    }

}
