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
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.filter.cause.Last;

import java.lang.reflect.Parameter;

public class LastCauseFilterSourceDelegate extends CauseFilterSourceDelegate {

    public LastCauseFilterSourceDelegate(Last anno) {
    }

    @Override
    protected void insertCauseCall(MethodVisitor mv, Parameter param, Class<?> targetType) {
        mv.visitLdcInsn(Type.getType(targetType));
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Cause.class), "last",
                "(Ljava/lang/Class;)Ljava/util/Optional;", false);
    }

    @Override
    protected void insertCheck(MethodVisitor mv, Parameter param, Class<?> targetType, int local) {
        mv.visitVarInsn(ALOAD, local);
        Label success = new Label();
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "isPresent", "()Z", false);
        mv.visitJumpInsn(IFNE, success);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitLabel(success);
    }

    @Override
    protected void insertTransform(MethodVisitor mv, Parameter param, Class<?> targetType, int local) {
        // who needs strongly typed variables anyway
        mv.visitVarInsn(ALOAD, local);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "get", "()Ljava/lang/Object;", false);
        mv.visitVarInsn(ASTORE, local);
    }

}
