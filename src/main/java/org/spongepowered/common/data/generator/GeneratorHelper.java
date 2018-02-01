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

import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.SIPUSH;

import com.google.common.reflect.TypeToken;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import java.lang.reflect.ParameterizedType;

public final class GeneratorHelper {

    public static String toSignature(TypeToken<?> typeToken) {
        final SignatureWriter writer = new SignatureWriter();
        visitSignature(typeToken, writer);
        return writer.toString();
    }

    private static void visitSignature(TypeToken<?> typeToken, SignatureVisitor visitor) {
        TypeToken<?> componentType;
        if (typeToken.isPrimitive()) {
            visitor.visitBaseType(Type.getDescriptor(typeToken.getRawType()).charAt(0));
        } else if ((componentType = typeToken.getComponentType()) != null) {
            visitSignature(componentType, visitor.visitArrayType());
        } else {
            visitor.visitClassType(Type.getInternalName(typeToken.getRawType()));
            final java.lang.reflect.Type type = typeToken.getType();
            if (type instanceof ParameterizedType) {
                for (java.lang.reflect.Type paramType : ((ParameterizedType) type).getActualTypeArguments()) {
                    visitSignature(TypeToken.of(paramType), visitor.visitTypeArgument(SignatureVisitor.INSTANCEOF));
                }
            }
            visitor.visitEnd();
        }
    }

    public static int getReturnOpcode(Class<?> clazz) {
        if (clazz == byte.class ||
                clazz == boolean.class ||
                clazz == short.class ||
                clazz == char.class ||
                clazz == int.class) {
            return Opcodes.IRETURN;
        } else if (clazz == double.class) {
            return Opcodes.DRETURN;
        } else if (clazz == float.class) {
            return Opcodes.FRETURN;
        } else if (clazz == long.class) {
            return Opcodes.LRETURN;
        } else if (clazz == void.class) {
            return Opcodes.RETURN;
        } else {
            return Opcodes.ARETURN;
        }
    }

    public static void visitIntInsn(MethodVisitor mv, int value) {
        if (value == 0) {
            mv.visitInsn(ICONST_0);
        } else if (value == 1) {
            mv.visitInsn(ICONST_1);
        } else if (value == 2) {
            mv.visitInsn(ICONST_2);
        } else if (value == 3) {
            mv.visitInsn(ICONST_3);
        } else if (value == 4) {
            mv.visitInsn(ICONST_4);
        } else if (value == 5) {
            mv.visitInsn(ICONST_5);
        } else if (value >= -128 && value <= 127) {
            mv.visitIntInsn(BIPUSH, value);
        } else if (value >= -32768 && value <= 32767) {
            mv.visitIntInsn(SIPUSH, value);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    private GeneratorHelper() {
    }
}
