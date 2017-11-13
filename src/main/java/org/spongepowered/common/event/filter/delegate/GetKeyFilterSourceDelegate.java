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

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.filter.data.GetKey;
import org.spongepowered.api.util.Tuple;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

public class GetKeyFilterSourceDelegate implements ParameterFilterSourceDelegate {

    private final GetKey getKey;
    private final String keyId;
    private final Key<?> key;
    private String fieldName;

    public GetKeyFilterSourceDelegate(GetKey getKey) {
        this.getKey = getKey;
        if (!this.getKey.value().contains(":")) {
            this.keyId = "sponge:" + this.getKey.value();
        } else {
            this.keyId = this.getKey.value();
        }

        this.key = Sponge.getRegistry().getType(Key.class, this.keyId).orElseThrow(() -> new IllegalStateException(String.format("No key found with id %s! If the key is created by a plugin with custom data,"
                + "make sure that it is registered before registering yoru event listener", this.getKey.value())));
    }

    public void createFields(ClassWriter cw, int local) {
        this.fieldName = "key" + local;
        FieldVisitor fv = cw.visitField(0, this.fieldName, Type.getDescriptor(Key.class), null, null);
        fv.visitEnd();
    }

    public void writeCtor(String name, MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0);

        // Call Sponge.getRegistry()
        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Sponge.class), "getRegistry", "()" + Type.getDescriptor(GameRegistry.class), false);

        // Load the arguments for GameRegistry#getType
        mv.visitLdcInsn(Type.getType(Key.class));
        mv.visitLdcInsn(this.keyId);

        // Call GameRegistry#getType
        mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(GameRegistry.class), "getType", "(" + Type.getDescriptor(Class.class) + Type.getDescriptor(String.class) + ")" + Type.getDescriptor(Optional.class), true);

        // Call Optional#get, since we've already checked that the key exists
        this.optionalGet(mv);
        // Cast the returned Object to Key
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(Key.class));

        // Store the Key in the field
        mv.visitFieldInsn(PUTFIELD, name, this.fieldName, Type.getDescriptor(Key.class));
    }

    @Override
    public Tuple<Integer, Integer> write(String name, ClassWriter cw, MethodVisitor constructorMv, MethodVisitor mv, Method method, Parameter param, int local) {

        this.createFields(cw, local);
        this.writeCtor(name, constructorMv);

        Class<?> paramType = param.getType();

        Label success = new Label();

        // The parameter type must be a supertype of either the value wrapper or the underlying value
        if (!((paramType.isAssignableFrom(this.key.getElementToken().getRawType()) || paramType.isAssignableFrom(this.key.getValueToken().getRawType()) || ImmutableValue.class.isAssignableFrom(paramType)))) {
            throw new IllegalStateException(String.format("Parameter '%s' must be of type %s or %s", param, key.getElementToken(), key.getValueToken()));
        }

        Class<?> eventClass = method.getParameterTypes()[0];

        if (!(ChangeDataHolderEvent.ValueChange.class.isAssignableFrom(eventClass))) {
            throw new IllegalStateException(String.format("@GetKey was used with event %s, which does not extend ChangeDataHolderEvent.ValueChange", eventClass));
        }

        int temp = local++;
        int paramLocal = local++;


        // Load the key into a temp local
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, name, this.fieldName, Type.getDescriptor(Key.class));
        mv.visitVarInsn(ASTORE, temp);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(ChangeDataHolderEvent.ValueChange.class), "getChanges", "()" + Type.getDescriptor(
                DataTransactionResult.class), true);

        for (DataTransactionResult.DataCategory category: this.getKey.from()) {
            // Dup the DataTransactionResult
            mv.visitInsn(DUP);
            // Load enum value
            mv.visitFieldInsn(GETSTATIC, Type.getInternalName(DataTransactionResult.DataCategory.class), category.name(), Type.getDescriptor(DataTransactionResult.DataCategory.class));
            // Load key
            mv.visitVarInsn(ALOAD, temp);

            // Call DataTransactionResult#get
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(DataTransactionResult.class), "get", "(" + Type.getDescriptor(
                    DataTransactionResult.DataCategory.class) + Type.getDescriptor(Key.class) + ")" + Type.getDescriptor(Optional.class), false);

            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Optional.class), "isPresent", "()Z", false);

            // If the Optional is present, we're done
            mv.visitJumpInsn(IFNE, success);
        }
        // If we get to here, then the key wasn't present in any of the DataCategories.
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);

        // If we jump to success, the Optional is present, so unwrap it
        mv.visitLabel(success);
        this.optionalGet(mv);
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(ImmutableValue.class));

        // If the parameter's type is the underlying value, we need to unwrap the ImmutableValue.
        if (paramType.equals(key.getElementToken().getRawType())) {
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(BaseValue.class), "get", "()Ljava/lang/Object;", true);

            // Cast and store the underlying value into the parameter local
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(paramType));
        } else {
            // The parameter is the wrapping Value type (e.g. MutableBoundedValue). If the wrapper type is immutable,
            // we just need to cast it. If it's mutable, we need to call isMutable, then cast it
            if (Value.class.isAssignableFrom(paramType)) {
                mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(ImmutableValue.class), "asMutable", "()" +  Type.getDescriptor(Value.class), true);
            }
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(paramType));
        }
        // Store the final value into the parameter local
        mv.visitVarInsn(ASTORE, paramLocal);

        return new Tuple<>(local, paramLocal);

    }

    private void optionalGet(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Optional.class), "get", "()" + Type.getDescriptor(Object.class), false);
    }

    public void foo() {
        DataTransactionResult.DataCategory myVar = DataTransactionResult.DataCategory.SUCCESSFUL;
    }

}
