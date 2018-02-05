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
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.MethodVisitor;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;

import java.lang.reflect.Method;
import java.util.List;

abstract class AbstractSetterMethodEntry extends MethodEntry {

    AbstractSetterMethodEntry(Method method, KeyEntry keyEntry) {
        super(method, keyEntry);
    }

    @Override
    final void visit(MethodVisitor mv, String targetInternalName, String mutableInternalName, List<KeyEntry> keyEntries) {
        if (mutableInternalName.equals(targetInternalName)) { // Check if we are transforming the mutable class
            mv.visitVarInsn(ALOAD, 0);
        } else {
            // Copy the immutable manipulator
            mv.visitTypeInsn(NEW, targetInternalName);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, targetInternalName, "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, 2);
            for (KeyEntry entry : keyEntries) {
                if (entry == this.keyEntry) {
                    continue;
                }
                mv.visitVarInsn(ALOAD, 2);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, targetInternalName, entry.valueFieldName, entry.valueFieldDescriptor);
                mv.visitFieldInsn(PUTFIELD, targetInternalName, entry.valueFieldName, entry.valueFieldDescriptor);
            }
            mv.visitVarInsn(ALOAD, 2);
        }
        visit0(mv, targetInternalName, mutableInternalName);
        if (DataManipulator.class.isAssignableFrom(this.method.getReturnType())) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ARETURN);
        } else if (ImmutableDataManipulator.class.isAssignableFrom(this.method.getReturnType())) {
            mv.visitVarInsn(ALOAD, 2);
            mv.visitInsn(ARETURN);
        } else {
            mv.visitInsn(RETURN);
        }
    }

    abstract void visit0(MethodVisitor mv, String targetInternalName, String mutableInternalName);
}
