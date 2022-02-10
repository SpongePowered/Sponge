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

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.event.manager.ListenerClassVisitor;

public abstract class CauseFilterSourceDelegate implements ParameterFilterSourceDelegate {
    protected static final Type CAUSE = Type.getType(Cause.class);

    @Override
    public Tuple<Integer, Integer> write(
        final ClassWriter cw, final MethodVisitor mv, final ListenerClassVisitor.DiscoveredMethod method,
        final int paramIdx, int local, final int[] plocals,
        final ListenerClassVisitor.ListenerParameter[] params
    ) {
        // Get the cause
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Event.class), "cause",
                "()" + CauseFilterSourceDelegate.CAUSE.getDescriptor(), true);
        final ListenerClassVisitor.ListenerParameter param = params[paramIdx];

        this.insertCauseCall(mv, param, param.type());
        final int paramLocal = local++;
        mv.visitVarInsn(ASTORE, paramLocal);

        this.insertTransform(mv, param, param.type(), paramLocal);

        return new Tuple<>(local, paramLocal);
    }

    protected abstract void insertCauseCall(MethodVisitor mv, ListenerClassVisitor.ListenerParameter param, Type targetType);

    protected abstract void insertTransform(MethodVisitor mv, ListenerClassVisitor.ListenerParameter param, Type targetType, int local);
}
