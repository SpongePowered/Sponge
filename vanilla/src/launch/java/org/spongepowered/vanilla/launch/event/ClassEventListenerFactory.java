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
package org.spongepowered.vanilla.launch.event;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V11;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.event.Event;
import org.spongepowered.common.event.filter.EventFilter;
import org.spongepowered.common.event.filter.FilterFactory;
import org.spongepowered.common.event.gen.LoaderClassWriter;
import org.spongepowered.common.util.generator.GeneratorUtils;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class ClassEventListenerFactory implements AnnotatedEventListener.Factory {

    private final AtomicInteger id = new AtomicInteger();
    private final MethodHandles.Lookup lookup;
    private final LoadingCache<Method, MethodHandles.Lookup> cache = Caffeine.newBuilder()
        .weakValues().build(this::createClass);
    private final FilterFactory filterFactory;

    public ClassEventListenerFactory(final FilterFactory factory, final MethodHandles.Lookup lookup) {
        this.filterFactory = Objects.requireNonNull(factory, "filterFactory");
        this.lookup = Objects.requireNonNull(lookup, "classLoader");
    }

    @Override
    public AnnotatedEventListener create(final Object handle, final Method method) throws Throwable {
        final MethodHandles.Lookup lookup = this.cache.get(method);
        return (AnnotatedEventListener) lookup.findConstructor(
            lookup.lookupClass(),
            MethodType.methodType(void.class, method.getDeclaringClass())
        ).invoke(handle);
    }

    MethodHandles.Lookup createClass(final Method method) throws Exception {
        final Class<?> handle = method.getDeclaringClass();
        final Class<?> eventClass = method.getParameterTypes()[0];
        final String listenerName = "Listener_" + handle.getSimpleName() + '_' + method.getName()
                + this.id.incrementAndGet();
        final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(handle, this.lookup);
        final Class<? extends EventFilter> filter = this.filterFactory.createFilter(method);

        if (filter == null && method.getParameterCount() != 1) {
            // basic sanity check
            throw new IllegalStateException("Failed to generate EventFilter for non trivial filtering operation.");
        }
        if (filter != null) {
            final MethodHandles.Lookup clazz = lookup.defineHiddenClass(
                ClassEventListenerFactory.generateFilteredClass(listenerName, handle, method, eventClass),
                true,
                MethodHandles.Lookup.ClassOption.NESTMATE
            );
            final EventFilter filterInstance = filter.getConstructor().newInstance();
            clazz.findStaticVarHandle(clazz.lookupClass(), "FILTER", EventFilter.class)
                .set(filterInstance);
            return clazz;
        }
        return lookup.defineHiddenClass(
            ClassEventListenerFactory.generateClass(listenerName, handle, method, eventClass),
            true,
            MethodHandles.Lookup.ClassOption.NESTMATE
        );
    }

    private static final String BASE_HANDLER = Type.getInternalName(AnnotatedEventListener.class);
    private static final String HANDLE_METHOD_DESCRIPTOR = '(' + Type.getDescriptor(Event.class) + ")V";
    private static final String FILTER_DESCRIPTOR = "(" + Type.getDescriptor(Event.class) + ")[Ljava/lang/Object;";

    // generates a class -- the FILTER field must be set to the event filter class
    private static byte[] generateFilteredClass(final String listenerName, final Class<?> handle, final Method method, final Class<?> eventClass) {
        final String handleName = Type.getInternalName(handle);
        final String name = handleName + '_' + listenerName;
        final String handleDescriptor = handle.descriptorString();
        final StringBuilder eventDescriptor = new StringBuilder("(");
        for (int i = 0; i < method.getParameterCount(); i++) {
            eventDescriptor.append(Type.getDescriptor(method.getParameterTypes()[i]));
        }
        eventDescriptor.append(")V");

        final ClassWriter cw = new LoaderClassWriter(handle.getClassLoader(), ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        MethodVisitor mv;
        final FieldVisitor fv;

        cw.visit(V11, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, name, null, ClassEventListenerFactory.BASE_HANDLER, null);
        {
            fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, "FILTER", EventFilter.class.descriptorString(), null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", '(' + handleDescriptor + ")V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, ClassEventListenerFactory.BASE_HANDLER, "<init>", "(Ljava/lang/Object;)V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "handle",
                ClassEventListenerFactory.HANDLE_METHOD_DESCRIPTOR, null, new String[] { "java/lang/Exception" });
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, name, "FILTER", EventFilter.class.descriptorString());
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(EventFilter.class), "filter",
                ClassEventListenerFactory.FILTER_DESCRIPTOR, true);
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 2);
            final Label l2 = new Label();
            mv.visitJumpInsn(IFNULL, l2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, name, "handle", "Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, handleName);
            for (int i = 0; i < method.getParameterCount(); i++) {
                mv.visitVarInsn(ALOAD, 2);
                mv.visitIntInsn(BIPUSH, i);
                mv.visitInsn(AALOAD);
                final Type paramType = Type.getType(method.getParameterTypes()[i]);
                GeneratorUtils.visitUnboxingMethod(mv, paramType);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, handleName, method.getName(), eventDescriptor.toString(), false);
            mv.visitLabel(l2);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }

    private static byte[] generateClass(final String listenerName, final Class<?> handle, final Method method, final Class<?> eventClass) {
        final String handleName = Type.getInternalName(handle);
        final String name = handleName + '_' + listenerName;
        final String handleDescriptor = handle.descriptorString();
        final String eventName = Type.getInternalName(eventClass);

        final ClassWriter cw = new LoaderClassWriter(handle.getClassLoader(), ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        MethodVisitor mv;

        cw.visit(V11, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, name, null, ClassEventListenerFactory.BASE_HANDLER, null);

        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", '(' + handleDescriptor + ")V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, ClassEventListenerFactory.BASE_HANDLER, "<init>", "(Ljava/lang/Object;)V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "handle", ClassEventListenerFactory.HANDLE_METHOD_DESCRIPTOR, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, name, "handle", "Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, handleName);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, eventName);
            mv.visitMethodInsn(INVOKEVIRTUAL, handleName, method.getName(), "(L" + eventName + ";)V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }

}
