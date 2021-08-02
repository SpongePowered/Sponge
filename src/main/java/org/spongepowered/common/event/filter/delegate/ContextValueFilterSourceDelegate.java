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
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.filter.cause.ContextValue;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public class ContextValueFilterSourceDelegate extends CauseFilterSourceDelegate {
    private static final Type EVENT_CONTEXT = Type.getType(EventContext.class);
    private static final Type EVENT_CONTEXT_KEY = Type.getType(EventContextKey.class);
    private static final Type EVENT_CONTEXT_KEYS = Type.getType(EventContextKeys.class);

    private final ContextValue anno;

    public ContextValueFilterSourceDelegate(final ContextValue anno) {
        this.anno = anno;
    }

    @Override
    protected void insertCauseCall(final MethodVisitor mv, final Parameter param, final Class<?> targetType) {
        final Field targetField;
        try {
            targetField = EventContextKeys.class.getField(this.anno.value());
        } catch (final NoSuchFieldException ex) {
            throw new IllegalArgumentException(String.format("Field %s specified by GetValue annotation was not found in EventContextKeys", this.anno.value()));
        }

        if (!EventContextKey.class.isAssignableFrom(targetField.getType())) {
            throw new IllegalArgumentException(String.format("Field %s.%s was not an EventContextKey", targetField.getName(), targetField.getType()));
        }

        // cause.context().get(EventContextKeys.<anno.value()>
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            CauseFilterSourceDelegate.CAUSE.getInternalName(),
            "context",
            "()" + ContextValueFilterSourceDelegate.EVENT_CONTEXT.getDescriptor(),
            false
        );
        mv.visitFieldInsn(
            GETSTATIC,
            ContextValueFilterSourceDelegate.EVENT_CONTEXT_KEYS.getInternalName(),
            this.anno.value(),
            ContextValueFilterSourceDelegate.EVENT_CONTEXT_KEY.getDescriptor()
        );
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            ContextValueFilterSourceDelegate.EVENT_CONTEXT.getInternalName(),
            "get",
            Type.getMethodDescriptor(ParameterFilterSourceDelegate.OPTIONAL, ContextValueFilterSourceDelegate.EVENT_CONTEXT_KEY),
            false
        );
    }

    @Override
    protected void insertTransform(final MethodVisitor mv, final Parameter param, final Class<?> targetType, final int local) {
        mv.visitVarInsn(ALOAD, local);
        final Label failure = new Label();
        final Label success = new Label();

        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            ParameterFilterSourceDelegate.OPTIONAL.getInternalName(),
            "isPresent",
            "()Z",
            false
        );
        mv.visitJumpInsn(IFEQ, failure);

        // Unwrap the optional
        mv.visitVarInsn(ALOAD, local);
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            ParameterFilterSourceDelegate.OPTIONAL.getInternalName(),
            "get",
            "()Ljava/lang/Object;",
            false
        );
        mv.visitVarInsn(ASTORE, local);

        mv.visitVarInsn(ALOAD, local);

        mv.visitTypeInsn(INSTANCEOF, Type.getInternalName(targetType));

        if (this.anno.typeFilter().length != 0) {
            mv.visitJumpInsn(IFEQ, failure);
            mv.visitVarInsn(ALOAD, local);
            // For each type we do an instance check and jump to either failure or success if matched
            for (int i = 0; i < this.anno.typeFilter().length; i++) {
                final Class<?> filter = this.anno.typeFilter()[i];
                if (i < this.anno.typeFilter().length - 1) {
                    mv.visitInsn(DUP);
                }
                mv.visitTypeInsn(INSTANCEOF, Type.getInternalName(filter));
                if (this.anno.inverse()) {
                    mv.visitJumpInsn(IFNE, failure);
                } else {
                    mv.visitJumpInsn(IFNE, success);
                }
            }
            if (this.anno.inverse()) {
                mv.visitJumpInsn(GOTO, success);
            }
            // If the annotation was not reversed then we fall into failure as no types were matched
        } else {
            mv.visitJumpInsn(IFNE, success);
        }
        mv.visitLabel(failure);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);

        mv.visitLabel(success);
    }
}
