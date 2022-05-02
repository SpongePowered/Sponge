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
package org.spongepowered.common.event.manager;

import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ListenerClassVisitor extends ClassVisitor {

    public static final String LISTENER_DESCRIPTOR = Type.getDescriptor(Listener.class);
    public static final String SPONGE_API_ANNOTATION_PREFIX = "org.spongepowered.api";

    public static final int ASM_VERSION = Opcodes.ASM9;

    final List<DiscoveredMethod> foundListenerMethods = new LinkedList<>();
    final Class<?> declaringClass;

    private ListenerClassVisitor(final Class<?> handle) {
        super(ListenerClassVisitor.ASM_VERSION);
        this.declaringClass = handle;
    }

    static List<DiscoveredMethod> getEventListenerMethods(final Class<?> handle) throws
        IOException,
        NoSuchMethodException {
        final @Nullable InputStream classStream = handle.getClassLoader().getResourceAsStream(
            handle.getName().replace(".", "/") + ".class");
        if (classStream == null) {
            throw new IOException("Could not find class " + handle.getName());
        }
        final ClassReader reader = new ClassReader(classStream);
        final ListenerClassVisitor classVisitor = new ListenerClassVisitor(handle);
        reader.accept(classVisitor, 0);
        return classVisitor.foundListenerMethods();
    }

    @Override
    public MethodVisitor visitMethod(
        final int access, final String name, final String descriptor, final String signature, final String[] exceptions
    ) {
        return new ListenerMethodVisitor(this, name, descriptor, access, signature);
    }

    public List<DiscoveredMethod> foundListenerMethods() {
        return Collections.unmodifiableList(this.foundListenerMethods);
    }

    static class ListenerMethodVisitor extends MethodVisitor {
        private final ListenerClassVisitor classVisitor;
        private final DiscoveredMethod discoveredMethod;

        public ListenerMethodVisitor(
            final ListenerClassVisitor listenerClassVisitor, final String name, final String descriptor,
            final int access,
            final @Nullable String signature
        ) {
            super(ListenerClassVisitor.ASM_VERSION);
            this.classVisitor = listenerClassVisitor;
            final Type[] type = Type.getArgumentTypes(descriptor);
            final ListenerParameter[] parameters = new ListenerParameter[type.length];
            this.discoveredMethod = new DiscoveredMethod(
                this.classVisitor.declaringClass,
                access, signature,
                name, descriptor, parameters
            );
            for (int i = 0; i < type.length; i++) {
                parameters[i] = new ListenerParameter(this.discoveredMethod, type[i]);
            }
        }

        @Override
        public void visitLocalVariable(
            final String name, final String descriptor, final @Nullable String signature, final Label start,
            final Label end, final int index
        ) {
            if (index == 0 || index > this.discoveredMethod.parameters.length) {
                return;
            }
            final ListenerParameter parameter = this.discoveredMethod.parameters[index - 1];
            parameter.name = name;
            if (index != 1) {
                return;
            }
            if (signature != null) { // We only care about the first parameter
                new SignatureReader(signature).accept(new ListenerSignatureVisitor(parameter));
            } else if (this.discoveredMethod.signature != null) {
                // In some compiled languages, the local variable signature is
                // not available, but we can still grab it from the method's
                // generic signature.
                new SignatureReader(this.discoveredMethod.signature)
                    .accept(new ListenerSignatureVisitor(parameter));
            }
        }

        @Override
        @Nullable
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
            final Type type = Type.getType(descriptor);
            if (Objects.equals(descriptor, ListenerClassVisitor.LISTENER_DESCRIPTOR)) {
                this.classVisitor.foundListenerMethods.add(this.discoveredMethod);
                final ListenerAnnotation e = new ListenerAnnotation(type, this.discoveredMethod);
                this.discoveredMethod.annotations.add(e);
                this.discoveredMethod.listenerAnnotation = new Listener() {
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Listener.class;
                    }

                    @Override
                    public Order order() {
                        return Order.DEFAULT;
                    }

                    @Override
                    public boolean beforeModifications() {
                        return false;
                    }
                };
                return new ListenerExtractor(this.discoveredMethod);
            }

            // Ignore non Sponge API annotations
            if (!type.getClassName().startsWith(ListenerClassVisitor.SPONGE_API_ANNOTATION_PREFIX)) {
                return null;
            }

            final ListenerAnnotation e = new ListenerAnnotation(type, this.discoveredMethod);
            this.discoveredMethod.annotations.add(e);
            return new ListenerAnnotationVisitor(e);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
            final int parameter, final String descriptor, final boolean visible
        ) {
            final ListenerAnnotation annotation = this.discoveredMethod.parameters[parameter].addAnnotation(descriptor);
            return new ListenerAnnotationVisitor(annotation);
        }

    }

    static final class ListenerSignatureVisitor extends SignatureVisitor {
        private final ListenerParameter parameter;

        public ListenerSignatureVisitor(final ListenerParameter parameter) {
            super(ListenerClassVisitor.ASM_VERSION);
            this.parameter = parameter;
        }

        @Override
        public void visitClassType(final String name) {
            // The order of operations is that visitClassType gets called
            // twice, once for the generic class, then a second time for
            // the generic type. Ideally we don't have to deal with additional
            // nested types, but if we do, well, we'll just ignore them.
            if (this.parameter.baseType == null) {
                this.parameter.baseType = Type.getType("L" + name + ";");
            } else if (this.parameter.genericType == null) {
                this.parameter.genericType = Type.getType("L" + name + ";");
            }
        }

        @Override
        public void visitTypeArgument() {
            // And sometimes, some plugins just care about the parent type
            // and wildcard their bounds.
            this.parameter.wildcard = true;
        }
    }

    static final class ListenerAnnotationVisitor extends AnnotationVisitor {
        private final ListenerAnnotation annotation;

        public ListenerAnnotationVisitor(final ListenerAnnotation annotation) {
            super(ListenerClassVisitor.ASM_VERSION);
            this.annotation = annotation;
        }

        @Override
        public void visit(final @Nullable String name, final Object value) {
            try {
                this.annotation.put(name == null ? "value" : name, value);
            } catch (final ClassNotFoundException | AnnotationFormatException e) {
                e.printStackTrace();
            }
        }

        @Override
        public AnnotationVisitor visitArray(final String name) {
            this.annotation.initReturnTypes();
            return this;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public void visitEnum(final String name, final String descriptor, final String value) {
            try {
                final String className = Type.getType(descriptor).getClassName();
                this.visit(name, Enum.valueOf((Class<? extends Enum>) this.annotation.discoveredMethod.classByLoader(className), value));
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    static final class ListenerExtractor extends AnnotationVisitor {
        private final DiscoveredMethod discoveredMethod;

        public ListenerExtractor(
            final ListenerClassVisitor.DiscoveredMethod discoveredMethod
        ) {
            super(ListenerClassVisitor.ASM_VERSION);
            this.discoveredMethod = discoveredMethod;
        }

        @Override
        public void visit(final String name, final Object value) {
            if ("beforeModifications".equals(name)) {
                final Listener existing = this.discoveredMethod.listenerAnnotation;
                this.discoveredMethod.listenerAnnotation = new Listener() {
                    @Override
                    public Order order() {
                        return existing.order();
                    }

                    @Override
                    public boolean beforeModifications() {
                        return (boolean) value;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Listener.class;
                    }
                };
            }
        }

        @Override
        public void visitEnum(final String name, final String descriptor, final String value) {
            if ("order".equals(name)) {
                final Order order = Order.valueOf(value);
                final Listener existing = this.discoveredMethod.listenerAnnotation;
                this.discoveredMethod.listenerAnnotation = new Listener() {
                    @Override
                    public Order order() {
                        return order;
                    }

                    @Override
                    public boolean beforeModifications() {
                        return existing.beforeModifications();
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Listener.class;
                    }
                };
            }
        }
    }

    public static final class DiscoveredMethod {
        private final Class<?> declaringClass;
        private final int access;
        private final String methodName;
        private final String descriptor;
        final @Nullable String signature;
        final ListenerParameter[] parameters;
        final List<ListenerAnnotation> annotations;
        @MonotonicNonNull Listener listenerAnnotation;

        public DiscoveredMethod(
            final Class<?> declaringClass,
            final int access, final @Nullable String signature,
            final String methodName,
            final String descriptor, final ListenerParameter[] parameters
        ) {
            this.declaringClass = declaringClass;
            this.access = access;
            this.methodName = methodName;
            this.signature = signature;
            this.parameters = parameters;
            this.descriptor = descriptor;
            this.annotations = new LinkedList<>();
        }

        public Class<?> declaringClass() {
            return this.declaringClass;
        }

        public Class<?> classByLoader(final String className) throws ClassNotFoundException {
            return Class.forName(className, false, this.declaringClass.getClassLoader());
        }

        public Optional<Class<?>> optionalClassByLoader(final String className) {
            try {
                return Optional.of(this.classByLoader(className));
            } catch (final ClassNotFoundException e) {
                return Optional.empty();
            }
        }

        public String methodName() {
            return this.methodName;
        }

        public String descriptor() {
            return this.descriptor;
        }

        public ListenerParameter[] parameterTypes() {
            return this.parameters;
        }

        public List<ListenerAnnotation> annotations() {
            return Collections.unmodifiableList(this.annotations);
        }

        public Listener listener() {
            return this.listenerAnnotation;
        }

        public int access() {
            return this.access;
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final DiscoveredMethod that = (DiscoveredMethod) o;
            return this.access == that.access && this.declaringClass.equals(that.declaringClass) && this.methodName.equals(
                that.methodName) && this.descriptor.equals(that.descriptor) && Objects.equals(
                this.signature, that.signature) && Arrays.equals(this.parameters, that.parameters) && this.annotations.equals(
                that.annotations) && Objects.equals(this.listenerAnnotation, that.listenerAnnotation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.access, this.methodName, this.descriptor, this.signature, this.listenerAnnotation);
        }

        @Override
        public String toString() {
            return "DiscoveredMethod{" +
                "declaringClass=" + this.declaringClass +
                ", access=" + this.access +
                ", methodName='" + this.methodName + '\'' +
                ", descriptor='" + this.descriptor + '\'' +
                ", signature='" + this.signature + '\'' +
                ", parameters=" + Arrays.toString(this.parameters) +
                ", annotations=" + this.annotations +
                ", listenerAnnotation=" + this.listenerAnnotation +
                '}';
        }
    }

    public static final class ListenerParameter {

        private final DiscoveredMethod method;
        private final Type type;
        private final List<ListenerAnnotation> annotations;
        public boolean wildcard = false;
        @MonotonicNonNull Type baseType;
        @MonotonicNonNull Type genericType;
        @MonotonicNonNull String name;

        ListenerParameter(final DiscoveredMethod method, final Type type) {
            this.method = method;
            this.type = type;
            this.annotations = new LinkedList<>();
        }

        public Class<?> clazz() throws ClassNotFoundException {
            return this.method.classByLoader(this.type.getClassName());
        }

        public Type type() {
            return this.type;
        }

        public ListenerAnnotation addAnnotation(final String descriptor) {
            final ListenerAnnotation listenerAnnotation = new ListenerAnnotation(Type.getType(descriptor), this.method);
            this.annotations.add(listenerAnnotation);
            return listenerAnnotation;
        }

        public String name() {
            return this.name;
        }

        public List<ListenerAnnotation> annotations() {
            return Collections.unmodifiableList(this.annotations);
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final ListenerParameter that = (ListenerParameter) o;
            return this.method.equals(that.method) && this.type.equals(that.type) && this.annotations.equals(that.annotations);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.method, this.type, this.annotations);
        }

        public java.lang.reflect.Type genericType() throws ClassNotFoundException {
            if (this.genericType == null) {
                if (this.wildcard) {
                    return TypeFactory.parameterizedClass(this.clazz(), TypeFactory.unboundWildcard());
                }
                return TypeFactory.parameterizedClass(this.clazz());
            }
            final java.lang.reflect.Type generic = TypeFactory.parameterizedClass(this.method.classByLoader(this.genericType.getClassName()));
            return TypeFactory.parameterizedClass(this.clazz(), generic);
        }
    }

    @SuppressWarnings("unchecked")
    public static final class ListenerAnnotation {

        private final Type type;
        final DiscoveredMethod discoveredMethod;
        private final Map<String, Object> values = new ConcurrentHashMap<>();
        private final Map<String, Class<?>> returnTypes = new ConcurrentHashMap<>();
        private @MonotonicNonNull Annotation annotation;

        ListenerAnnotation(
            final Type type,
            final DiscoveredMethod discoveredMethod
        ) {
            this.type = type;
            this.discoveredMethod = discoveredMethod;
        }

        public Type type() {
            return this.type;
        }

        public void put(final String name, final Object value) throws ClassNotFoundException,
            AnnotationFormatException {

            Object realValue = value;
            if (value instanceof Type) {
                try {
                    realValue = this.discoveredMethod.classByLoader(((Type) value).getClassName());
                } catch (ClassNotFoundException ignored) {
                }
            }

            final Class<?> returnType = this.returnTypes.get(name);
            if (returnType != null && returnType.isArray()) {
                final Class<?> componentType = returnType.getComponentType();
                final Object previous = this.values.get(name);
                Object newArray;
                if (previous == null) {
                    newArray = Array.newInstance(componentType, 1);
                    Array.set(newArray, 0, realValue);
                } else {
                    newArray = Array.newInstance(componentType, Array.getLength(previous) + 1);
                    System.arraycopy(previous, 0, newArray, 0, Array.getLength(previous));
                    Array.set(newArray, Array.getLength(previous), realValue);
                }
                this.values.put(name, newArray);
            } else {
                this.values.put(name, realValue);
            }

            this.annotation = TypeFactory.annotation(
                (Class<? extends Annotation>) this.discoveredMethod.classByLoader(this.type.getClassName()), this.values);
        }

        public Annotation annotation() throws ClassNotFoundException, AnnotationFormatException {
            if (this.annotation == null) {
                this.annotation = TypeFactory.annotation(
                    (Class<? extends Annotation>) this.discoveredMethod.classByLoader(this.type.getClassName()), this.values);
            }
            return this.annotation;
        }

        public void initReturnTypes() {
            try {
                for (Method element : this.discoveredMethod.classByLoader(this.type.getClassName()).getDeclaredMethods()) {
                    this.returnTypes.put(element.getName(), element.getReturnType());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
