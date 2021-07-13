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
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.POP;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.filter.data.GetValue;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.util.generator.GeneratorUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

public class GetValueFilterSourceDelegate implements ParameterFilterSourceDelegate {
    private static final Type CAUSE = Type.getType(Cause.class);
    private static final Type VALUE_CONTAINER = Type.getType(ValueContainer.class);
    private static final Type OPTIONAL = Type.getType(Optional.class);
    private static final String OPTIONAL_IS_PRESENT = Type.getMethodDescriptor(Type.BOOLEAN_TYPE);
    private static final String OPTIONAL_GET = Type.getMethodDescriptor(Type.getType(Object.class));
    private static final String VALUE_CONTAINER_GET = Type.getMethodDescriptor(GetValueFilterSourceDelegate.OPTIONAL, Type.getType(Key.class));
    private final GetValue anno;

    public GetValueFilterSourceDelegate(final GetValue anno) {
        this.anno = anno;
    }

    @Override
    public Tuple<Integer, Integer> write(
        final ClassWriter cw, final MethodVisitor mv, final Method method, final int paramIdx, int local, final int[] plocals,
        final Parameter[] params
    ) {
        final Field targetField;
        try {
            targetField = this.anno.container().getField(this.anno.value());
        } catch (final NoSuchFieldException ex) {
            throw new IllegalArgumentException(String.format("Field %s specified by GetValue annotation was not found in container %s", this.anno.value(), this.anno.container()));
        }

        if (!Key.class.isAssignableFrom(targetField.getType())) {
            throw new IllegalArgumentException(String.format("Field %s.%s was not a Key", targetField.getName(), targetField.getType()));
        }

        final Class<?> paramType = params[paramIdx].getType();
        final Type eventType = Type.getType(params[0].getType());
        // key := <container>.<value>
        final int keyIdx = local++;
        mv.visitFieldInsn(GETSTATIC, Type.getInternalName(this.anno.container()), this.anno.value(), Type.getDescriptor(Key.class));
        mv.visitVarInsn(ASTORE, keyIdx);

        final Label success = new Label();
        final Label failure = new Label();
        // for all parameters p' before `param` that inherit from ValueContainer, excluding the event itself
        for (int i = paramIdx - 1; i > 0; --i) {
            final Parameter param = params[i];
            if (!ValueContainer.class.isAssignableFrom(param.getType())) {
                continue;
            }
            mv.visitVarInsn(ALOAD, plocals[i - 1]); // p'
            mv.visitVarInsn(ALOAD, keyIdx);
            // x = p'.get(key)
            mv.visitMethodInsn(
                INVOKEINTERFACE,
                Type.getInternalName(param.getType()),
                "get",
                GetValueFilterSourceDelegate.VALUE_CONTAINER_GET,
                true
            );
            // if (x.isPresent()) goto success
            mv.visitInsn(DUP);
            mv.visitMethodInsn(
                INVOKEVIRTUAL,
                GetValueFilterSourceDelegate.OPTIONAL.getInternalName(),
                "isPresent",
                GetValueFilterSourceDelegate.OPTIONAL_IS_PRESENT,
                false
            );
            mv.visitJumpInsn(IFNE, success);
            mv.visitInsn(POP); // drop the optional from the stack if we're unsuccessful
        }

        // since none have been reached yet
        // x = locals[1].cause().first(ValueContainer.class)
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(
            INVOKEINTERFACE,
            eventType.getInternalName(),
            "cause",
            "()" + GetValueFilterSourceDelegate.CAUSE.getDescriptor(),
            true
        );
        mv.visitLdcInsn(GetValueFilterSourceDelegate.VALUE_CONTAINER);
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            GetValueFilterSourceDelegate.CAUSE.getInternalName(),
            "first",
            Type.getMethodDescriptor(GetValueFilterSourceDelegate.OPTIONAL, Type.getType(Class.class)),
            false
        );
        mv.visitInsn(DUP);
        // if (!x.isPresent()) goto failure;
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            GetValueFilterSourceDelegate.OPTIONAL.getInternalName(),
            "isPresent",
            GetValueFilterSourceDelegate.OPTIONAL_IS_PRESENT,
            false
        );
        mv.visitJumpInsn(IFEQ, failure);
        // event:
        // x = x.get().get(key)
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            GetValueFilterSourceDelegate.OPTIONAL.getInternalName(),
            "get",
            GetValueFilterSourceDelegate.OPTIONAL_GET,
            false
        ); // Optional<ValueContainer>

        mv.visitVarInsn(ALOAD, keyIdx);
        mv.visitMethodInsn(
            INVOKEINTERFACE,
            GetValueFilterSourceDelegate.VALUE_CONTAINER.getInternalName(),
            "get",
            GetValueFilterSourceDelegate.VALUE_CONTAINER_GET,
            true
        );
        // if (x.isPresent()) goto success;
        mv.visitInsn(DUP);
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            GetValueFilterSourceDelegate.OPTIONAL.getInternalName(),
            "isPresent",
            GetValueFilterSourceDelegate.OPTIONAL_IS_PRESENT,
            false
        );
        mv.visitJumpInsn(IFNE, success);
        mv.visitLabel(failure);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitLabel(success);
        // x = x.get()
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            GetValueFilterSourceDelegate.OPTIONAL.getInternalName(),
            "get",
            GetValueFilterSourceDelegate.OPTIONAL_GET,
            false
        );
        final Type param = Type.getType(paramType);,
        if (paramType.isPrimitive()) {
            GeneratorUtils.visitUnboxingMethod(mv, param);
        }

        final int paramLocal = local;
        local += param.getSize();
        mv.visitVarInsn(param.getOpcode(ISTORE), paramLocal);
        return new Tuple<>(local, paramLocal);
    }
}
