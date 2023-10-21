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
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V11;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.event.Event;
import org.spongepowered.common.event.filter.EventFilter;
import org.spongepowered.common.event.filter.FilterFactory;
import org.spongepowered.common.event.filter.FilterGenerator;
import org.spongepowered.common.event.gen.LoaderClassWriter;
import org.spongepowered.common.util.generator.GeneratorUtils;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

public final class ClassEventListenerFactory implements AnnotatedEventListener.Factory {

    private static final String FILTER = "filter";
    private final FilterFactory filterFactory;

    public ClassEventListenerFactory(final FilterFactory factory) {
        this.filterFactory = Objects.requireNonNull(factory, "filterFactory");
    }

    @Override
    public AnnotatedEventListener create(final Object handle, final ListenerClassVisitor.DiscoveredMethod method,
                                         final MethodHandles.Lookup handleLookup) throws Throwable {
        final MethodHandles.Lookup lookup = this.createLookup(method, handleLookup);
        return (AnnotatedEventListener) lookup.findConstructor(
            lookup.lookupClass(),
            MethodType.methodType(void.class, method.declaringClass())
        ).invoke(handle);
    }

    MethodHandles.Lookup createLookup(final ListenerClassVisitor.DiscoveredMethod method, final MethodHandles.Lookup handleLookup) throws Exception {
        final Class<?> handle = method.declaringClass();
        final Class<?> eventClass = method.parameterTypes()[0].clazz();
        final String listenerName = "Listener_" + handle.getSimpleName() + '_' + method.methodName();
        final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(handle, handleLookup);
        final @Nullable EventFilter filter = this.filterFactory.create(method, lookup);

        if (filter == null && method.parameterTypes().length != 1) {
            // basic sanity check
            throw new IllegalStateException("Failed to generate EventFilter for non trivial filtering operation.");
        }
        if (filter != null) {
            final MethodHandles.Lookup clazz = lookup.defineHiddenClass(
                ClassEventListenerFactory.generateFilteredClass(listenerName, handle, method),
                true,
                MethodHandles.Lookup.ClassOption.NESTMATE
            );
            clazz.findStaticVarHandle(clazz.lookupClass(), ClassEventListenerFactory.FILTER, EventFilter.class)
                .set(filter);
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

    // generates a class -- the FILTER field must be set to the event filter class
    private static byte[] generateFilteredClass(
        final String listenerName, final Class<?> handle, final ListenerClassVisitor.DiscoveredMethod method
    ) {
        final String handleName = Type.getInternalName(handle);
        final String name = handleName + '_' + listenerName;
        final String handleDescriptor = handle.descriptorString();
        final StringBuilder eventDescriptor = new StringBuilder("(");
        for (int i = 0; i < method.parameterTypes().length; i++) {
            eventDescriptor.append(method.parameterTypes()[i].type().getDescriptor());
        }
        eventDescriptor.append(")V");

        final ClassWriter cw = new LoaderClassWriter(handle.getClassLoader(), ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        MethodVisitor mv;
        final FieldVisitor fv;

        cw.visit(V11, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, name, null, ClassEventListenerFactory.BASE_HANDLER, null);
        {
            fv = cw.visitField(
                ACC_PRIVATE + ACC_STATIC,
                ClassEventListenerFactory.FILTER,
                EventFilter.class.descriptorString(),
                null,
                null
            );
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
            mv.visitFieldInsn(GETSTATIC, name, ClassEventListenerFactory.FILTER, EventFilter.class.descriptorString());
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(EventFilter.class), "filter",
                FilterGenerator.FILTER_DESCRIPTOR, true);
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 2);
            final Label l2 = new Label();
            mv.visitJumpInsn(IFNULL, l2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, name, "handle", "Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, handleName);
            for (int i = 0; i < method.parameterTypes().length; i++) {
                mv.visitVarInsn(ALOAD, 2);
                mv.visitIntInsn(BIPUSH, i);
                mv.visitInsn(AALOAD);
                final Type paramType = method.parameterTypes()[i].type();
                GeneratorUtils.visitUnboxingMethod(mv, paramType);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, handleName, method.methodName(), eventDescriptor.toString(), false);
            mv.visitLabel(l2);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }

    private static byte[] generateClass(final String listenerName, final Class<?> handle, final ListenerClassVisitor.DiscoveredMethod method, final Class<?> eventClass) {
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
            mv.visitMethodInsn(INVOKEVIRTUAL, handleName, method.methodName(), "(L" + eventName + ";)V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }

}
