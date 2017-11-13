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
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;

import com.google.common.collect.Sets;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.After;
import org.spongepowered.api.event.filter.cause.Before;
import org.spongepowered.api.event.filter.cause.ContextValue;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.data.GetKey;
import org.spongepowered.api.util.Tuple;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GetKeyFilterSourceDelegate implements ParameterFilterSourceDelegate {

    private static final Set<Class<?>> TAG_ANNOTATIONS = Sets
            .newHashSet(After.class, Before.class, ContextValue.class, First.class, Last.class, Root.class, Getter.class);

    private Class<?> paramType;
    private final GetKey getKey;
    private final String keyId;
    private final Key<?> key;
    private String fieldName;
    private Parameter sourceParam;
    private int sourceParamIndex;
    private boolean parameterIsSpongeValue;

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

    private void sanityCheck(Parameter param, Method method) {
        // The parameter type must be a supertype of either the value wrapper or the underlying value
        if (!(this.paramType.isAssignableFrom(this.key.getElementToken().getRawType()) || paramType.isAssignableFrom(this.key.getValueToken().getRawType()))) {
            if (ImmutableValue.class.isAssignableFrom(paramType)) {
                try {
                    Class<?> immutableType = this.key.getValueToken().getRawType().getMethod("asImmutable").getReturnType();
                    if (!this.paramType.isAssignableFrom(immutableType)) {
                        throw new IllegalStateException(String.format("Parameter '%s' must be a supertype of %s", param, immutableType));
                    }
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new IllegalStateException(
                        String.format("Parameter '%s' must be of type %s or %s", param, key.getElementToken(), key.getValueToken()));
            }
        }

        Class<?> sourceType = this.sourceParam.getType();
        if (Event.class.isAssignableFrom(sourceType)) {
            if (!(ChangeDataHolderEvent.ValueChange.class.isAssignableFrom(this.sourceParam.getType()))) {
                throw new IllegalStateException(String.format("@GetKey was used with event %s, which does not extend ChangeDataHolderEvent.ValueChange", sourceType));
            }
        } else if (!DataHolder.class.isAssignableFrom(sourceType)) {
            throw new IllegalStateException(String.format("Tag on @GetKey parameter %s matched parameter %s, which is not a DataHolder or ChangeDataHolderEvent.ValueChange", param, sourceType));
        }

    }

    private void findSourceParam(Parameter param, Method method) {
        String tag = this.getKey.tag();
        Parameter[] params = method.getParameters();
        List<Tuple<Parameter, Integer>> matchedParams = new ArrayList<>();

        for (int i = 0; i < params.length; i++) {
            Parameter methodParam = params[i];
            if (methodParam == param) {
                continue;
            }

            String paramTag;
            if (i == 0) {
                paramTag = ""; // The event has an implicit tag of ""
            } else {
                Set<Object> sourceAnnotations = Arrays.stream(methodParam.getAnnotations()).filter(p -> TAG_ANNOTATIONS.contains(p.annotationType())).collect(Collectors.toSet());
                if (sourceAnnotations.size() == 0) {
                    continue;
                } else if (sourceAnnotations.size() > 1) {
                    throw new IllegalStateException(String.format("Parameter %s has incompatible annotations: %s", methodParam, sourceAnnotations));
                }

                Object annotation = sourceAnnotations.iterator().next();
                try {
                    paramTag = (String) annotation.getClass().getMethod("tag").invoke(annotation);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            if (tag.equals(paramTag)) {
                matchedParams.add(new Tuple<>(methodParam, i + 1)); // Account for implicit 'this' at index 0
            }
        }
        if (matchedParams.size() == 0) {
            throw new IllegalStateException(String.format("No matches found for tag '%s' on @GetKey parameter '%s'", tag, param));
        }
        if (matchedParams.size() > 1) {
            throw new IllegalStateException(String.format("Tag '%s' for @GetKey on parameter %s matches multiple other parameters: '%s'", tag, param, matchedParams));
        }
        this.sourceParam = matchedParams.get(0).getFirst();
        this.sourceParamIndex = matchedParams.get(0).getSecond();
    }

    @Override
    public Tuple<Integer, Integer> write(String name, ClassWriter cw, MethodVisitor constructorMv, MethodVisitor mv, Method method, Parameter param, int local) {

        this.paramType = param.getType();
        this.fieldName = "key" + local;
        this.parameterIsSpongeValue = BaseValue.class.isAssignableFrom(this.paramType);

        this.findSourceParam(param, method);
        this.sanityCheck(param, method);


        this.createFields(cw, local);
        this.writeCtor(name, constructorMv);

        Class<?> paramType = param.getType();

        Label success = new Label();

        int temp = local++;
        int paramLocal = local++;


        // Load the key into a temp local
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, name, this.fieldName, Type.getDescriptor(Key.class));
        mv.visitVarInsn(ASTORE, temp);

        boolean isMutable;
        if (Event.class.isAssignableFrom(this.sourceParam.getType())) {
            isMutable = this.handleEventSource(mv, success, temp);
        } else {
            isMutable = this.handleDataHolder(mv, success, temp);
        }


        // If we get to here, then the key wasn't present in any of the DataCategories.
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);

        // If we jump to success, the Optional is present, so unwrap it
        mv.visitLabel(success);
        this.optionalGet(mv);
        //mv.visitTypeInsn(CHECKCAST, Type.getInternalName(ImmutableValue.class));

        // If the parameter's type is the underlying value, we need to unwrap the ImmutableValue.
        if (!this.parameterIsSpongeValue) {
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(BaseValue.class), "get", "()Ljava/lang/Object;", true);

            // Cast and store the underlying value into the parameter local
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(paramType));
        } else {
            // If the type wanted by the parameter and the type we have disagree, we need to convert by calling asMutable or asImmutable
            // If they agree, we can just cast
            if (Value.class.isAssignableFrom(paramType) && !isMutable) {
                mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(ImmutableValue.class), "asMutable", "()" +  Type.getDescriptor(Value.class), true);
            } else if (ImmutableValue.class.isAssignableFrom(paramType) && isMutable) {
                mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(ImmutableValue.class), "asImmutable", "()" +  Type.getDescriptor(ImmutableValue.class), true);
            }
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(paramType));
        }
        // Store the final value into the parameter local
        mv.visitVarInsn(ASTORE, paramLocal);

        return new Tuple<>(local, paramLocal);

    }

    private boolean handleDataHolder(MethodVisitor mv, Label success, int temp) {
        mv.visitVarInsn(ALOAD, this.sourceParamIndex);
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(DataHolder.class));

        // Load key
        mv.visitVarInsn(ALOAD, temp);
        mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(ValueContainer.class), "getValue", "(" + Type.getDescriptor(Key.class) + ")" + Type.getDescriptor(Optional.class), true);

        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Optional.class), "isPresent", "()Z", false);

        // If the Optional is present, we're done
        mv.visitJumpInsn(IFNE, success);

        // getValue returns a mutable value
        return true;
    }

    private boolean handleEventSource(MethodVisitor mv, Label success, int temp) {
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

        // DataTransactionResult#get returns an immutable value
        return false;
    }

    private void optionalGet(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Optional.class), "get", "()" + Type.getDescriptor(Object.class), false);
    }

    public void foo() {
        DataTransactionResult.DataCategory myVar = DataTransactionResult.DataCategory.SUCCESSFUL;
    }

}
