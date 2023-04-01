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
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.event.manager.ListenerClassVisitor;

public class CancellationEventFilterDelegate implements FilterDelegate {

    private final Tristate state;

    public CancellationEventFilterDelegate(final IsCancelled state) {
        this(state.value());
    }

    public CancellationEventFilterDelegate(final Tristate state) {
        this.state = state;
    }

    @Override
    public int write(final String name, final ClassWriter cw, final MethodVisitor mv,
        final ListenerClassVisitor.DiscoveredMethod method, final int locals
    ) throws ClassNotFoundException {
        if (this.state == Tristate.UNDEFINED) {
            return locals;
        }

        final boolean checkCancellable = !Cancellable.class.isAssignableFrom(method.parameterTypes()[0].clazz());
        final String cancellableClassName = Type.getInternalName(Cancellable.class);
        final Label notCancelled = new Label();

        if (checkCancellable) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(INSTANCEOF, cancellableClassName);
            mv.visitJumpInsn(IFEQ, notCancelled);
        }

        mv.visitVarInsn(ALOAD, 1);
        if (checkCancellable) {
            mv.visitTypeInsn(CHECKCAST, cancellableClassName);
        }
        mv.visitMethodInsn(INVOKEINTERFACE, cancellableClassName, "isCancelled", "()Z", true);

        if (this.state == Tristate.TRUE) {
            final Label cancelled = new Label();
            mv.visitJumpInsn(IFNE, cancelled);

            if (checkCancellable) {
                mv.visitLabel(notCancelled);
            }
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);

            mv.visitLabel(cancelled);
        } else {
            mv.visitJumpInsn(IFEQ, notCancelled);

            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);

            mv.visitLabel(notCancelled);
        }

        return locals;
    }

}
