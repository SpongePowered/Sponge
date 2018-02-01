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

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.util.generator.GeneratorUtils;
import org.spongepowered.common.data.generator.GeneratorHelper;
import org.spongepowered.common.data.generator.KeyEntry;
import org.spongepowered.common.data.generator.method.MethodEntry;

import java.lang.reflect.Method;

public class BoxedPrimitiveGetterMethodEntry extends MethodEntry {

    public BoxedPrimitiveGetterMethodEntry(Method method, KeyEntry keyEntry) {
        super(method, keyEntry);
    }

    @Override
    public void visit(MethodVisitor mv, String implClassDescriptor, String mutableImplClassName) {
        mv.visitVarInsn(ALOAD, 0);
        final Class<?> returnType = this.keyEntry.valueClass;
        // Load the value
        mv.visitFieldInsn(GETFIELD, implClassDescriptor,
                this.keyEntry.valueFieldName, Type.getDescriptor(returnType));
        // Box the primitive value
        GeneratorUtils.visitBoxingMethod(mv, Type.getType(this.keyEntry.valueClass));
        // Return the primitive value
        mv.visitInsn(GeneratorHelper.getReturnOpcode(this.keyEntry.boxedValueClass));
        mv.visitMaxs(1, 1);
    }
}
