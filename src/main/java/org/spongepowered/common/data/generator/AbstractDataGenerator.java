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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.objectweb.asm.Opcodes.ACC_BRIDGE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.F_APPEND;
import static org.objectweb.asm.Opcodes.F_SAME;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IF_ACMPNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

import com.google.common.base.CaseFormat;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.generator.DataGenerator;
import org.spongepowered.api.data.generator.KeyValue;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.immutable.ImmutableMapValue;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.data.value.immutable.ImmutablePatternListValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.PatternListValue;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.generator.GeneratorUtils;
import org.spongepowered.common.data.InternalCopies;
import org.spongepowered.common.data.generator.method.MethodEntry;
import org.spongepowered.common.data.generator.method.getter.GetterMethodEntry;
import org.spongepowered.common.data.generator.method.getter.UnboxedOptionalGetterMethodEntry;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeMapValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongePatternListValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeMapValue;
import org.spongepowered.common.data.value.mutable.SpongePatternListValue;
import org.spongepowered.common.util.TypeTokenHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

@SuppressWarnings({"unchecked", "NullableProblems", "ConstantConditions"})
public class AbstractDataGenerator<M extends DataManipulator<M, I>,
        I extends ImmutableDataManipulator<I, M>,
        G extends DataGenerator<M, I, G, R>,
        R extends DataGenerator<?, ?, ?, R>>
        implements DataGenerator<M, I, G, R> {

    protected String id;
    @Nullable protected String name;
    protected Predicate<? extends DataHolder> dataHolderPredicate;
    protected int contentVersion;
    protected final List<KeyEntry> keyEntries = new ArrayList<>();
    @Nullable protected Class<M> mutableInterface;
    @Nullable protected Class<I> immutableInterface;

    AbstractDataGenerator() {
        reset();
    }

    @Override
    public R from(DataRegistration<?, ?> value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public R reset() {
        this.id = null;
        this.name = null;
        this.dataHolderPredicate = dataHolder -> true;
        this.contentVersion = 1;
        this.keyEntries.clear();
        return (R) this;
    }

    @Override
    public G name(String name) {
        checkArgument(StringUtils.isNotEmpty(name), "name cannot be null or empty");
        this.name = name;
        return (G) this;
    }

    @Override
    public G version(int contentVersion) {
        checkArgument(contentVersion > 0, "content version must be greater then zero");
        this.contentVersion = contentVersion;
        return (G) this;
    }

    @Override
    public G predicate(Predicate<? extends DataHolder> predicate) {
        checkNotNull(predicate, "predicate");
        this.dataHolderPredicate = predicate;
        return (G) this;
    }

    @Override
    public G id(String id) {
        checkArgument(StringUtils.isNotEmpty(id), "id cannot be null or empty");
        this.id = id;
        return (G) this;
    }

    private static final TypeVariable<Class<Optional>> optionalVariable = Optional.class.getTypeParameters()[0];
    private static final String keysFieldName = "keys";

    @Override
    public DataRegistration<M, I> build() {
        checkState(this.id != null, "The id must be set");
        final PluginContainer plugin = Sponge.getCauseStackManager().getCurrentCause()
                .first(PluginContainer.class).get();

        // Apply builder specific settings, validations, etc...
        preBuild();

        final Map<String, KeyEntry> keysById = new HashMap<>();

        // Step 1: Populate the key entries and map them by id
        for (KeyEntry keyEntry : this.keyEntries) {
            final String id = keyEntry.key.getId();
            final int index = id.indexOf(':');
            final String idNoNamespace = id.substring(index + 1);

            keysById.put(id, keyEntry);
            // The first key has priority, if keys
            // are used from two (or more) plugins
            // with the same non namespace id
            keysById.putIfAbsent(idNoNamespace, keyEntry);

            final String underscoreId = id.replace(':', '_');

            // Assign names to the fields, these should never conflict
            keyEntry.keyFieldName = "key$" + underscoreId;
            keyEntry.valueFieldName = "value$" + underscoreId;
            keyEntry.defaultValueFieldName = "default_value$" + underscoreId;

            // Generate the descriptor and signature of the key
            keyEntry.keyFieldDescriptor = Type.getDescriptor(Key.class);
            keyEntry.keyFieldSignature = String.format("L%s<%s>;", Type.getInternalName(Key.class),
                    GeneratorHelper.toSignature(keyEntry.key.getValueToken()));

            // Generate the descriptor and signature of the value
            final TypeToken<?> elementType = keyEntry.key.getElementToken();
            keyEntry.valueClass = elementType.getRawType();
            keyEntry.valueType = Type.getType(keyEntry.valueClass);
            keyEntry.valueTypeName = Type.getInternalName(keyEntry.valueClass);
            keyEntry.boxedValueDescriptor = Type.getDescriptor(keyEntry.valueClass);
            keyEntry.boxedValueFieldSignature = GeneratorHelper.toSignature(elementType);
            // No need for the signature, the descriptor is specific enough
            if (keyEntry.boxedValueFieldSignature.equals(keyEntry.boxedValueDescriptor)) {
                keyEntry.boxedValueFieldSignature = null;
            }
            // Unbox the element type, the unboxed
            // version will be used as field type
            keyEntry.boxedValueClass = Primitives.unwrap(keyEntry.valueClass);
            keyEntry.valueFieldDescriptor = Type.getDescriptor(keyEntry.boxedValueClass);
            keyEntry.valueFieldSignature = GeneratorHelper.toSignature(elementType);
            // No need for the signature, the descriptor is specific enough
            if (keyEntry.valueFieldSignature.equals(keyEntry.valueFieldDescriptor)) {
                keyEntry.valueFieldSignature = null;
            }
            if (keyEntry instanceof BoundedKeyEntry) {
                final BoundedKeyEntry bounded = (BoundedKeyEntry) keyEntry;
                // The minimum value field, uses the same signature/descriptor as the value field
                bounded.minimumFieldName = "minimum_value$" + underscoreId;
                // The maximum value field, uses the same signature/descriptor as the value field
                bounded.maximumFieldName = "maximum_value$" + underscoreId;
                // The comparator field
                bounded.comparatorFieldName = "value_comparator$" + underscoreId;
                bounded.comparatorFieldDescriptor = Type.getDescriptor(Comparator.class);
                bounded.comparatorFieldSignature = String.format("L%s<%s>;",
                        Type.getInternalName(Comparator.class), bounded.boxedValueFieldSignature);
            }
        }

        // Step 2: Collect methods that should be generated
        final Set<MethodEntry> mutableMethodEntries = new HashSet<>();
        final Set<MethodEntry> immutableMethodEntries = new HashSet<>();

        collectMethodEntries(this.mutableInterface, keysById, mutableMethodEntries);
        collectMethodEntries(this.immutableInterface, keysById, immutableMethodEntries);

        // Step 3: Start generating things?

        // Assuming that people use the lower underscore format? Replace - with _ just in case.
        final String mutableClassName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                plugin.getId().replace('-', '_') + '.' +
                        this.id.replace('-', '_'));
        final String immutableClassName = mutableClassName + "$Immutable"; // Inner class

        // Convert to internal names
        final String mutableInternalName = mutableClassName.replace('.', '/');
        final String immutableInternalName = immutableClassName.replace('.', '/');

        // Generate the classes
        final ClassWriter cv = new ClassWriter(0);

        final String interf;
        String interfSignature;

        // Check if interfaces were provided, isn't required
        if (this.mutableInterface != null) {
            checkState(this.immutableInterface != null); // Should never happen

            interfSignature = null; // No signature is needed, the interface should have defined all the generics
            interf = Type.getInternalName(this.mutableInterface);
        } else {
            // TODO: Add ListData, MappedData and VariantData support

            interfSignature = String.format("Lorg/spongepowered/api/data/manipulator/DataManipulator<L%s;L%s;>;",
                    mutableInternalName, immutableInternalName);
            interf = "org/spongepowered/api/data/manipulator/DataManipulator";
        }

        cv.visit(V1_8, ACC_PUBLIC | ACC_SUPER, mutableInternalName, interfSignature,
                "java/lang/Object", new String[] { interf });

        // Define the inner immutable class
        cv.visitInnerClass(immutableInternalName, mutableInternalName, "Immutable", ACC_PUBLIC | ACC_STATIC);

        // The ImmutableSet builder must also be visited?
        cv.visitInnerClass("com/google/common/collect/ImmutableSet$Builder",
                "com/google/common/collect/ImmutableSet", "Builder", ACC_PUBLIC | ACC_STATIC);

        // Generate the fields
        visitFields(cv, this.keyEntries, false);

        MethodVisitor mv;
        {
            // Generate the <init> method (constructor)
            mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            for (KeyEntry entry : this.keyEntries) {
                // Load this
                mv.visitVarInsn(ALOAD, 0);
                // Get the static value
                // The static fields are inside the mutable class
                mv.visitFieldInsn(GETSTATIC, mutableInternalName, entry.defaultValueFieldName, entry.valueFieldDescriptor);
                // Put the value in the object
                mv.visitFieldInsn(PUTFIELD, mutableInternalName, entry.valueFieldName, entry.valueFieldDescriptor);
            }
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            // Generate the fill method
            mv = cv.visitMethod(ACC_PUBLIC, "fill",
                    "(Lorg/spongepowered/api/data/DataHolder;Lorg/spongepowered/api/data/merge/MergeFunction;)Ljava/util/Optional;",
                    "(Lorg/spongepowered/api/data/DataHolder;Lorg/spongepowered/api/data/merge/MergeFunction;)Ljava/util/Optional<" + interfSignature
                            + ">;",
                    null);
            mv.visitCode();
            // final Optional<TestData> optData = dataHolder.get(TestData.class);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(interf);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/api/data/DataHolder", "get", "(Ljava/lang/Class;)Ljava/util/Optional;", true);
            mv.visitVarInsn(ASTORE, 3);
            // Start of: if (optData.isPresent()) {}
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "isPresent", "()Z", false);
            Label jumpLabel = new Label();
            mv.visitJumpInsn(IFEQ, jumpLabel);
            // TestDataImpl data = (TestDataImpl) overlap.merge(this, optData.get());
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "get", "()Ljava/lang/Object;", false);
            mv.visitTypeInsn(CHECKCAST, "org/spongepowered/api/data/value/ValueContainer");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/spongepowered/api/data/merge/MergeFunction", "merge",
                    "(Lorg/spongepowered/api/data/value/ValueContainer;Lorg/spongepowered/api/data/value/ValueContainer;)Lorg/spongepowered/api/data/value/ValueContainer;",
                    true);
            mv.visitTypeInsn(CHECKCAST, mutableInternalName);
            mv.visitVarInsn(ASTORE, 4);
            // Transfer the contents from the retrieved container to this container
            for (KeyEntry entry : this.keyEntries) {
                // Load this
                mv.visitVarInsn(ALOAD, 0);
                // Load the 4th local variable, "data"
                mv.visitVarInsn(ALOAD, 4);
                // Retrieve from the other container
                mv.visitFieldInsn(GETFIELD, mutableInternalName, entry.valueFieldName, entry.valueFieldDescriptor);
                // Put into this container
                mv.visitFieldInsn(PUTFIELD, mutableInternalName, entry.valueFieldName, entry.valueFieldDescriptor);
            }
            // End of: if (optData.isPresent()) {}
            mv.visitLabel(jumpLabel);
            mv.visitFrame(F_APPEND, 1, new Object[]{"java/util/Optional"}, 0, null);
            // return Optional.of(this);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "java/util/Optional", "of", "(Ljava/lang/Object;)Ljava/util/Optional;", false);
            mv.visitInsn(ARETURN);
            // The end
            mv.visitMaxs(3, 5);
            mv.visitEnd();
        }
        {
            // public <E> TestDataImpl set(Key<? extends BaseValue<E>> key, E value) {}
            mv = cv.visitMethod(ACC_PUBLIC, "set",
                    String.format("(Lorg/spongepowered/api/data/key/Key;Ljava/lang/Object;)L%s;", interf),
                    String.format("<E:Ljava/lang/Object;>(Lorg/spongepowered/api/data/key/Key<+Lorg/spongepowered/api/data/value/BaseValue<TE;>;>;"
                                    + "TE;)%s", interfSignature), null);
            mv.visitCode();
            // checkNotNull(key, "key");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn("key");
            mv.visitMethodInsn(INVOKESTATIC, "com/google/common/base/Preconditions", "checkNotNull",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitInsn(POP);
            // checkNotNull(value, "value");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn("value");
            mv.visitMethodInsn(INVOKESTATIC, "com/google/common/base/Preconditions", "checkNotNull",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitInsn(POP);
            for (KeyEntry entry : this.keyEntries) {
                // Start of: if ((Key) key == key$my_string) {}
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(GETSTATIC, mutableInternalName, entry.keyFieldName, entry.keyFieldDescriptor);
                final Label jumpLabel = new Label();
                mv.visitJumpInsn(IF_ACMPNE, jumpLabel);
                // this.value$my_string = (String) InternalCopies.mutableCopy(value);
                // this.value$my_int = (Integer) value; // No internal copy for primitives
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 2);
                // Primitives need to be unboxed and other objects may need to be cloned
                if (entry.boxedValueClass != entry.valueClass) { // Primitive
                    GeneratorUtils.visitUnboxingMethod(mv, Type.getType(entry.valueClass));
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(InternalCopies.class), "mutableCopy",
                            "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                    mv.visitTypeInsn(CHECKCAST, entry.valueTypeName);
                }
                mv.visitFieldInsn(PUTFIELD, mutableInternalName, entry.valueFieldName, entry.valueFieldDescriptor);
                // return this;
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(ARETURN);
                // End of: if ((Key) key == key$my_string) {}
                mv.visitLabel(jumpLabel);
                mv.visitFrame(F_SAME, 0, null, 0, null);
            }
            // return this;
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(2, 3);
            mv.visitEnd();
        }
        {
            // public <E> Optional<E> get(Key<? extends BaseValue<E>> key)
            mv = cv.visitMethod(ACC_PUBLIC, "get", "(Lorg/spongepowered/api/data/key/Key;)Ljava/util/Optional;",
                    "<E:Ljava/lang/Object;>(Lorg/spongepowered/api/data/key/Key<+Lorg/spongepowered/api/data/value/BaseValue<TE;>;>;)Ljava/util/Optional<TE;>;",
                    null);
            mv.visitCode();
            // checkNotNull(key, "key");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn("key");
            mv.visitMethodInsn(INVOKESTATIC, "com/google/common/base/Preconditions", "checkNotNull",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitInsn(POP);
            for (KeyEntry entry : this.keyEntries) {
                // Start of: if ((Key) key == key$my_string) {}
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(GETSTATIC, mutableClassName, entry.keyFieldName, entry.keyFieldDescriptor);
                final Label jumpLabel = new Label();
                mv.visitJumpInsn(IF_ACMPNE, jumpLabel);
                // return Optional.of((E) InternalCopies.mutableCopy(this.value$my_string));
                // Load this
                mv.visitVarInsn(ALOAD, 0);
                // Load the field from this
                mv.visitFieldInsn(GETFIELD, mutableClassName, entry.valueFieldName, entry.valueFieldDescriptor);
                // Primitives need to be boxed and other objects may need to be cloned
                if (entry.boxedValueClass != entry.valueClass) { // Primitive
                    GeneratorUtils.visitBoxingMethod(mv, Type.getType(entry.valueClass));
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(InternalCopies.class), "mutableCopy",
                            "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                }
                // Put the object into a Optional
                mv.visitMethodInsn(INVOKESTATIC, "java/util/Optional", "of", "(Ljava/lang/Object;)Ljava/util/Optional;", false);
                // Return it
                mv.visitInsn(ARETURN);
                // End of: if ((Key) key == key$my_string) {}
                mv.visitLabel(jumpLabel);
                mv.visitFrame(F_SAME, 0, null, 0, null);
            }
            // return Optional.empty();
            mv.visitMethodInsn(INVOKESTATIC, "java/util/Optional", "empty", "()Ljava/util/Optional;", false);
            mv.visitInsn(ARETURN);
            // End
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            // public boolean supports(Key<?> key) {}
            mv = cv.visitMethod(ACC_PUBLIC, "supports", "(Lorg/spongepowered/api/data/key/Key;)Z", "(Lorg/spongepowered/api/data/key/Key<*>;)Z",
                    null);
            mv.visitCode();
            // checkNotNull(key, "key");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn("key");
            mv.visitMethodInsn(INVOKESTATIC, "com/google/common/base/Preconditions", "checkNotNull",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitInsn(POP);
            // return keys.contains(key);
            mv.visitFieldInsn(GETSTATIC, mutableInternalName, "keys",
                    "Lcom/google/common/collect/ImmutableSet;");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/google/common/collect/ImmutableSet", "contains", "(Ljava/lang/Object;)Z", false);
            mv.visitInsn(IRETURN);
            // End
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            // public Set<Key<?>> getKeys() {}
            mv = cv.visitMethod(ACC_PUBLIC, "getKeys", "()Ljava/util/Set;", "()Ljava/util/Set<Lorg/spongepowered/api/data/key/Key<*>;>;", null);
            mv.visitCode();
            // return keys;
            mv.visitFieldInsn(GETSTATIC, "org/spongepowered/common/data/generator/test/TestDataImpl", "keys",
                    "Lcom/google/common/collect/ImmutableSet;");
            mv.visitInsn(ARETURN);
            // End
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            // public TestData copy() {}
            mv = cv.visitMethod(ACC_PUBLIC, "copy", "()Lorg/spongepowered/common/data/generator/test/TestData;", null, null);
            mv.visitCode();
            // TestDataImpl copy = new TestDataImpl();
            mv.visitTypeInsn(NEW, mutableInternalName);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, mutableInternalName, "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, 1);
            // copy.value$my_int = this.value$my_int;
            for (KeyEntry entry : this.keyEntries) {
                // Load "copy"
                mv.visitVarInsn(ALOAD, 1);
                // Load this
                mv.visitVarInsn(ALOAD, 0);
                // Get the value from this
                mv.visitFieldInsn(GETFIELD, mutableInternalName, entry.valueFieldName, entry.valueFieldDescriptor);
                // Put the value into "copy"
                mv.visitFieldInsn(PUTFIELD, mutableInternalName, entry.valueFieldName, entry.valueFieldDescriptor);
            }
            // return copy;
            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(ARETURN);
            // End
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            // public Immutable asImmutable() {}
            mv = cv.visitMethod(ACC_PUBLIC, "asImmutable", String.format("()L%s;", immutableInternalName), null, null);
            mv.visitCode();
            // Immutable immutable = new Immutable();
            mv.visitTypeInsn(NEW, immutableInternalName);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, immutableInternalName, "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, 1);
            // immutable.value$my_string = this.value$my_string;
            for (KeyEntry entry : this.keyEntries) {
                // Load "immutable"
                mv.visitVarInsn(ALOAD, 1);
                // Load this
                mv.visitVarInsn(ALOAD, 0);
                // Get the value from this
                mv.visitFieldInsn(GETFIELD, mutableInternalName, entry.valueFieldName, entry.valueFieldDescriptor);
                // Put the value into "immutable"
                mv.visitFieldInsn(PUTFIELD, immutableInternalName, entry.valueFieldName, entry.valueFieldDescriptor);
            }
            // return immutable;
            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(ARETURN);
            // End
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            // public int getContentVersion() {}
            mv = cv.visitMethod(ACC_PUBLIC, "getContentVersion", "()I", null, null);
            mv.visitCode();
            // return 5;
            // Load the content version
            GeneratorHelper.visitPushInt(mv, this.contentVersion);
            // And return it
            mv.visitInsn(IRETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        // TODO: Generate more synthetic bridges if methods get overridden?
        {
            mv = cv.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "asImmutable",
                    "()Lorg/spongepowered/api/data/manipulator/ImmutableDataManipulator;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, mutableInternalName, "asImmutable",
                    String.format("()L%s;", immutableInternalName), false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cv.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "copy",
                    "()Lorg/spongepowered/api/data/manipulator/DataManipulator;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, mutableInternalName, "copy",
                    String.format("()L%s;", mutableInternalName), false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cv.visitMethod(ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC, "set",
                    "(Lorg/spongepowered/api/data/key/Key;Ljava/lang/Object;)Lorg/spongepowered/api/data/manipulator/DataManipulator;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, mutableInternalName, "set",
                    String.format("(Lorg/spongepowered/api/data/key/Key;Ljava/lang/Object;)L%s;", mutableInternalName), false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        {
            mv = cv.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "copy", "()Lorg/spongepowered/api/data/value/ValueContainer;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, mutableInternalName, "copy",
                    String.format("()L%s;", mutableInternalName), false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        return null;
    }

    static void visitImmutableValueCreation(MethodVisitor mv, KeyEntry keyEntry,
            String targetInternalName, String mutableInternalName) {
        try {
            visitImmutableValueCreation0(mv, keyEntry, targetInternalName, mutableInternalName);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void visitImmutableValueCreation0(MethodVisitor mv, KeyEntry keyEntry,
            String targetInternalName, String mutableInternalName) throws NoSuchMethodException {
        final Class<?> rawType = keyEntry.key.getValueToken().getRawType();
        // So many different value types
        final Constructor<?> constructor;
        final Class<?> valueClass;
        if (rawType.isAssignableFrom(PatternListValue.class) ||
                rawType.isAssignableFrom(ImmutablePatternListValue.class)) {
            valueClass = ImmutableSpongePatternListValue.class;
            constructor = ImmutableSpongePatternListValue.class.getConstructor(
                    Key.class, List.class, List.class);
        } else if (rawType.isAssignableFrom(ListValue.class) ||
                rawType.isAssignableFrom(ImmutableListValue.class)) {
            valueClass = ImmutableSpongeListValue.class;
            constructor = ImmutableSpongeListValue.class.getConstructor(
                    Key.class, List.class, List.class);
        } else if (rawType.isAssignableFrom(MapValue.class) ||
                rawType.isAssignableFrom(ImmutableMapValue.class)) {
            valueClass = ImmutableSpongeMapValue.class;
            constructor = ImmutableSpongeMapValue.class.getConstructor(
                    Key.class, Map.class, Map.class);
        } else if (rawType.isAssignableFrom(OptionalValue.class) ||
                rawType.isAssignableFrom(ImmutableOptionalValue.class)) {
            valueClass = ImmutableSpongeOptionalValue.class;
            constructor = ImmutableSpongeMapValue.class.getConstructor(
                    Key.class, Map.class, Map.class);
        } else if (keyEntry instanceof BoundedKeyEntry) {
            valueClass = ImmutableSpongeBoundedValue.class;
            constructor = ImmutableSpongeBoundedValue.class.getConstructor(
                    Key.class, Object.class, Object.class, Comparator.class, Object.class, Object.class);
        } else {
            valueClass = ImmutableSpongeValue.class;
            constructor = ImmutableSpongeValue.class.getConstructor(
                    Key.class, Object.class, Object.class);
        }
        // TODO: Use cached methods if possible
        // TODO: WeightedTableValue support
        visitBaseValueCreation(mv, keyEntry, targetInternalName, mutableInternalName, constructor, valueClass);
    }

    static void visitValueCreation(MethodVisitor mv, KeyEntry keyEntry,
            String targetInternalName, String mutableInternalName) {
        try {
            visitValueCreation0(mv, keyEntry, targetInternalName, mutableInternalName);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void visitValueCreation0(MethodVisitor mv, KeyEntry keyEntry,
            String targetInternalName, String mutableInternalName) throws NoSuchMethodException {
        final Class<?> rawType = keyEntry.key.getValueToken().getRawType();
        // So many different value types
        final Constructor<?> constructor;
        final Class<?> valueClass;
        if (rawType.isAssignableFrom(PatternListValue.class) ) {
            valueClass = SpongePatternListValue.class;
            constructor = SpongePatternListValue.class.getConstructor(
                    Key.class, List.class, List.class);
        } else if (rawType.isAssignableFrom(ImmutablePatternListValue.class)) {
            valueClass = ImmutableSpongePatternListValue.class;
            constructor = ImmutableSpongePatternListValue.class.getConstructor(
                    Key.class, List.class, List.class);
        } else if (rawType.isAssignableFrom(ListValue.class)) {
            valueClass = SpongeListValue.class;
            constructor = SpongeListValue.class.getConstructor(
                    Key.class, List.class, List.class);
        } else if (rawType.isAssignableFrom(ImmutableListValue.class)) {
            valueClass = ImmutableSpongeListValue.class;
            constructor = ImmutableSpongeListValue.class.getConstructor(
                    Key.class, List.class, List.class);
        } else if (rawType.isAssignableFrom(MapValue.class) ||
                rawType.isAssignableFrom(ImmutableMapValue.class)) {
            valueClass = SpongeMapValue.class;
            constructor = SpongeMapValue.class.getConstructor(
                    Key.class, Map.class, Map.class);
        } else if (rawType.isAssignableFrom(ImmutableMapValue.class)) {
            valueClass = ImmutableSpongeMapValue.class;
            constructor = ImmutableSpongeMapValue.class.getConstructor(
                    Key.class, Map.class, Map.class);
        } else if (rawType.isAssignableFrom(OptionalValue.class) ||
                rawType.isAssignableFrom(ImmutableOptionalValue.class)) {
            valueClass = ImmutableSpongeOptionalValue.class;
            constructor = ImmutableSpongeMapValue.class.getConstructor(
                    Key.class, Map.class, Map.class);
        } else if (keyEntry instanceof BoundedKeyEntry) {
            valueClass = ImmutableSpongeBoundedValue.class;
            constructor = ImmutableSpongeBoundedValue.class.getConstructor(
                    Key.class, Object.class, Object.class, Comparator.class, Object.class, Object.class);
        } else {
            valueClass = ImmutableSpongeValue.class;
            constructor = ImmutableSpongeValue.class.getConstructor(
                    Key.class, Object.class, Object.class);
        }
        // TODO: WeightedTableValue support
        visitBaseValueCreation(mv, keyEntry, targetInternalName, mutableInternalName, constructor, valueClass);
    }

    private static void visitBaseValueCreation(MethodVisitor mv, KeyEntry keyEntry,
            String targetInternalName, String mutableInternalName, Constructor<?> constructor, Class<?> valueClass) {
        // The value type that will be created
        final String valueType = Type.getInternalName(valueClass);
        final String constructorDesc = Type.getConstructorDescriptor(constructor);
        mv.visitTypeInsn(NEW, valueType);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, mutableInternalName, keyEntry.keyFieldName, keyEntry.keyFieldDescriptor);
        mv.visitFieldInsn(GETSTATIC, mutableInternalName, keyEntry.defaultValueFieldName, keyEntry.valueFieldDescriptor);
        // Load "this"
        mv.visitVarInsn(ALOAD, 0);
        // Get the value from "this"
        mv.visitFieldInsn(GETFIELD, targetInternalName, keyEntry.valueFieldName, keyEntry.valueFieldDescriptor);
        // Box the value, if it's a primitive
        GeneratorUtils.visitBoxingMethod(mv, keyEntry.valueType);
        if (keyEntry instanceof BoundedKeyEntry) {
            // TODO: Inline min/max constants? For ints, doubles, etc...
            final BoundedKeyEntry bounded = (BoundedKeyEntry) keyEntry;
            mv.visitFieldInsn(GETSTATIC, mutableInternalName, bounded.comparatorFieldName, bounded.comparatorFieldDescriptor);
            // Load the minimum value
            mv.visitFieldInsn(GETSTATIC, mutableInternalName, bounded.minimumFieldName, bounded.valueFieldDescriptor);
            // Box the minimum value, if it's a primitive
            GeneratorUtils.visitBoxingMethod(mv, keyEntry.valueType);
            // Load the maximum value
            mv.visitFieldInsn(GETSTATIC, mutableInternalName, bounded.maximumFieldName, bounded.valueFieldDescriptor);
            // Box the maximum value, if it's a primitive
            GeneratorUtils.visitBoxingMethod(mv, keyEntry.valueType);
        }
        mv.visitMethodInsn(INVOKESPECIAL, valueType, "<init>", constructorDesc, false);
    }

    static void visitFields(ClassVisitor cv, List<KeyEntry> keyEntries, boolean applyStaticFields) {
        FieldVisitor fv;
        if (applyStaticFields) {
            fv = cv.visitField(ACC_PUBLIC | ACC_STATIC, keysFieldName, "Lcom/google/common/collect/ImmutableSet;",
                    "Lcom/google/common/collect/ImmutableSet<Lorg/spongepowered/api/data/key/Key<*>;>;", null);
            fv.visitEnd();
        }
        for (KeyEntry entry : keyEntries) {
            // Check if the mutable class is being generated, this is the
            // only class that will hold the static fields, the immutable
            // version will be a inner class.
            if (applyStaticFields) {
                // Visit the key field
                fv = cv.visitField(ACC_PUBLIC | ACC_STATIC, entry.keyFieldName,
                        "Lorg/spongepowered/api/data/key/Key;", entry.keyFieldSignature, null);
                fv.visitEnd();
                // Visit the default value field
                fv = cv.visitField(ACC_PUBLIC | ACC_STATIC, entry.defaultValueFieldName,
                        entry.valueFieldDescriptor, entry.valueFieldSignature, null);
                fv.visitEnd();
                if (entry instanceof BoundedKeyEntry) {
                    final BoundedKeyEntry bounded = (BoundedKeyEntry) entry;
                    // Visit the comparator field
                    fv = cv.visitField(ACC_PUBLIC | ACC_STATIC, bounded.comparatorFieldName,
                            bounded.comparatorFieldDescriptor, bounded.comparatorFieldSignature, null);
                    fv.visitEnd();
                    // Visit the minimum value field
                    fv = cv.visitField(ACC_PUBLIC | ACC_STATIC, bounded.minimumFieldName,
                            bounded.valueFieldDescriptor, bounded.valueFieldSignature, null);
                    fv.visitEnd();
                    // Visit the maximum value field
                    fv = cv.visitField(ACC_PUBLIC | ACC_STATIC, bounded.maximumFieldName,
                            bounded.valueFieldDescriptor, bounded.valueFieldSignature, null);
                    fv.visitEnd();
                }
            }
            // Visit the value field, not private, so we still have field access between the mutable and immutable classes
            fv = cv.visitField(ACC_PUBLIC, entry.defaultValueFieldName,
                    entry.valueFieldDescriptor, entry.valueFieldSignature, null);
            fv.visitEnd();
        }
    }

    static void collectMethodEntries(Class<?> targetClass,
            Map<String, KeyEntry> keysById, Set<MethodEntry> methodEntries) {
        for (Method method : targetClass.getMethods()) {
            final KeyValue keyValue = method.getAnnotation(KeyValue.class);
            if (keyValue != null) {
                final KeyEntry keyEntry = keysById.get(keyValue.value());
                checkState(keyEntry != null, "Cannot find a mapping for the KeyValue value: %s", keyValue.value());
                final Class<?> returnType = method.getReturnType();
                if (returnType.equals(void.class)) { // Setter?

                } else { // Getter?
                    // Getters don't have parameter counts
                    checkState(method.getParameterCount() == 0,
                            "The method %s has a return type (not void) and parameters?", method.getName());
                    final TypeToken<?> returnTypeToken = TypeToken.of(method.getGenericReturnType());
                    // Check if the object can be "casted", just "compatible" generics
                    boolean compatible = TypeTokenHelper.isAssignable(keyEntry.key.getElementToken(), returnTypeToken);
                    // Optionals support unboxed return types if annotated with @Nullable
                    if (!compatible && keyEntry.key.getElementToken().isSubtypeOf(Optional.class)) {
                        final TypeToken<?> genericType = returnTypeToken.resolveType(optionalVariable);
                        compatible = TypeTokenHelper.isAssignable(genericType, returnTypeToken);
                        // Force a @Nullable annotation on unboxed fields
                        if (compatible) {
                            checkState(method.getAnnotation(Nullable.class) != null,
                                    "A unboxed optional getter method (%s) requires a @Nullable annotation.", method.getName());
                            methodEntries.add(new UnboxedOptionalGetterMethodEntry(method, keyEntry));
                            continue;
                        }
                    }
                    // Must be compatible at this point, no more special cases
                    checkState(compatible, "The key type %s is not assignable to the return type %s in the method %s",
                            keyEntry.key.getElementToken(), returnTypeToken, method.getName());
                    methodEntries.add(new GetterMethodEntry(method, keyEntry));
                }
            }
        }
    }

    void preBuild() {
    }
}
