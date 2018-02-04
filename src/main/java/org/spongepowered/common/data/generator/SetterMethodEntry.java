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
import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.MethodVisitor;
import org.spongepowered.api.util.generator.GeneratorUtils;

import java.lang.reflect.Method;

final class SetterMethodEntry extends MethodEntry {

    SetterMethodEntry(Method method, KeyEntry keyEntry) {
        super(method, keyEntry);
    }

    @Override
    void visit(MethodVisitor mv, String targetInternalName, String mutableInternalName) {
        // Load "this"
        mv.visitVarInsn(ALOAD, 0);
        final Class<?> paramType = this.method.getParameterTypes()[0];
        // Load the parameter
        mv.visitVarInsn(ALOAD, 1);
        if (this.keyEntry.boxedValueClass.equals(paramType)) {
            // Check if it's null, will be skipped for primitives
            mv.visitMethodInsn(INVOKESTATIC, "com/google/common/base/Preconditions",
                    "checkNotNull", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
            // Unbox the value, if it's a primitive
            GeneratorUtils.visitUnboxingMethod(mv, this.keyEntry.valueType);
        }
        mv.visitFieldInsn(PUTFIELD, targetInternalName, this.keyEntry.valueFieldName, this.keyEntry.valueFieldDescriptor);
        mv.visitInsn(RETURN);
    }
}
