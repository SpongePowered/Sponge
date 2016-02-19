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
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.util.Tuple;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

public class GetterFilterSourceDelegate implements ParameterFilterSourceDelegate {

    private final Getter anno;

    public GetterFilterSourceDelegate(Getter a) {
        this.anno = a;
    }

    @Override
    public Tuple<Integer, Integer> write(ClassWriter cw, MethodVisitor mv, Method method, Parameter param, int local) {
        Class<?> targetType = param.getType();
        Class<?> eventClass = method.getParameterTypes()[0];
        String targetMethod = this.anno.value();
        Method targetMethodObj = null;
        for (Method mth : eventClass.getMethods()) {
            if (targetMethod.equals(mth.getName())) {
                if (targetMethodObj != null) {
                    throw new IllegalArgumentException("Multiple matches found for getter " + targetMethod + " in type " + eventClass.getName());
                }
                if (mth.getParameterCount() != 0) {
                    throw new IllegalArgumentException(
                            "Method " + mth.toGenericString() + " specified by getter annotation has non-zero parameter count");
                }
                if (!mth.getReturnType().equals(Optional.class) && !mth.getReturnType().equals(targetType)) {
                    throw new IllegalArgumentException("Method " + mth.toGenericString() + " does not return the correct type. Expected: "
                            + targetType.getName() + " Found: " + mth.getReturnType().getName());
                }
                targetMethodObj = mth;
            }
        }
        Class<?> declaringClass = targetMethodObj.getDeclaringClass();
        mv.visitVarInsn(ALOAD, 1);
        int op = declaringClass.isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL;
        mv.visitMethodInsn(op, Type.getInternalName(declaringClass), targetMethod,
                "()" + Type.getDescriptor(targetMethodObj.getReturnType()), true);
        int paramLocal = local++;
        mv.visitVarInsn(ASTORE, paramLocal);
        Label failure = new Label();
        Label success = new Label();
        if (Optional.class.equals(targetMethodObj.getReturnType()) && !Optional.class.equals(targetType)) {
            // Unwrap the optional
            mv.visitVarInsn(ALOAD, paramLocal);

            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "isPresent", "()Z", false);
            mv.visitJumpInsn(IFEQ, failure);

            mv.visitVarInsn(ALOAD, paramLocal);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "get", "()Ljava/lang/Object;", false);

            mv.visitVarInsn(ASTORE, paramLocal);
        }
        mv.visitVarInsn(ALOAD, paramLocal);
        mv.visitTypeInsn(INSTANCEOF, Type.getInternalName(targetType));
        mv.visitJumpInsn(IFNE, success);
        mv.visitLabel(failure);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitLabel(success);

        return new Tuple<>(local, paramLocal);
    }

}
