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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
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
import static org.objectweb.asm.Opcodes.V1_6;

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
import org.spongepowered.common.event.gen.DefineableClassLoader;
import org.spongepowered.common.event.gen.LoaderClassWriter;
import org.spongepowered.common.util.generator.GeneratorUtils;

import java.util.concurrent.atomic.AtomicInteger;

public final class ClassEventListenerFactory implements AnnotatedEventListener.Factory {

    private final AtomicInteger id = new AtomicInteger();
    private final DefineableClassLoader classLoader;
    private final LoadingCache<ListenerClassVisitor.DiscoveredMethod, Class<? extends AnnotatedEventListener>> cache = Caffeine.newBuilder()
        .weakValues().build(this::createClass);
    private final FilterFactory filterFactory;

    private final String targetPackage;

    public ClassEventListenerFactory(
        final String targetPackage, final FilterFactory factory, final DefineableClassLoader classLoader) {
        checkNotNull(targetPackage, "targetPackage");
        checkArgument(!targetPackage.isEmpty(), "targetPackage cannot be empty");
        this.targetPackage = targetPackage + '.';
        this.filterFactory = checkNotNull(factory, "filterFactory");
        this.classLoader = checkNotNull(classLoader, "classLoader");
    }

    @Override
    public AnnotatedEventListener create(final Object handle, final ListenerClassVisitor.DiscoveredMethod method) throws Exception {
        return this.cache.get(method)
                .getConstructor(method.declaringClass())
                .newInstance(handle);
    }

    Class<? extends AnnotatedEventListener> createClass(final ListenerClassVisitor.DiscoveredMethod method) throws Exception {
        final Class<?> handle = method.declaringClass();
        final Class<?> eventClass = method.parameterTypes()[0].clazz();
        final String name = this.targetPackage + eventClass.getSimpleName() + "Listener_" + handle.getSimpleName() + '_' + method.methodName()
                + this.id.incrementAndGet();
        final Class<? extends EventFilter> filter = this.filterFactory.createFilter(method);

        if (filter == null && method.parameterTypes().length != 1) {
            // basic sanity check
            throw new IllegalStateException("Failed to generate EventFilter for non trivial filtering operation.");
        }
        if (filter != null) {
            filter.newInstance();
            return this.classLoader.defineClass(name, ClassEventListenerFactory.generateClass(name, handle, method, eventClass, filter));
        }
        return this.classLoader.defineClass(name, ClassEventListenerFactory.generateClass(name, handle, method, eventClass));
    }

    private static final String BASE_HANDLER = Type.getInternalName(AnnotatedEventListener.class);
    private static final String HANDLE_METHOD_DESCRIPTOR = '(' + Type.getDescriptor(Event.class) + ")V";
    private static final String FILTER_DESCRIPTOR = "(" + Type.getDescriptor(Event.class) + ")[Ljava/lang/Object;";

    private static byte[] generateClass(String name, final Class<?> handle, final ListenerClassVisitor.DiscoveredMethod method, final Class<?> eventClass, final Class<? extends EventFilter> filter) {
        name = name.replace('.', '/');
        final String handleName = Type.getInternalName(handle);
        final String handleDescriptor = Type.getDescriptor(handle);
        final String filterName = Type.getInternalName(filter);
        final String eventDescriptor = method.descriptor();

        final ClassWriter cw = new LoaderClassWriter(handle.getClassLoader(), ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        MethodVisitor mv;
        final FieldVisitor fv;

        cw.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, name, null, ClassEventListenerFactory.BASE_HANDLER, null);
        {
            fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, "FILTER", "L" + filterName + ";", null, null);
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, filterName);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, filterName, "<init>", "()V", false);
            mv.visitFieldInsn(PUTSTATIC, name, "FILTER", "L" + filterName + ";");
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
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
            mv.visitFieldInsn(GETSTATIC, name, "FILTER", "L" + filterName + ";");
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
            for (int i = 0; i < method.parameterTypes().length; i++) {
                mv.visitVarInsn(ALOAD, 2);
                mv.visitIntInsn(BIPUSH, i);
                mv.visitInsn(AALOAD);
                final Type paramType = method.parameterTypes()[i].type();
                GeneratorUtils.visitUnboxingMethod(mv, paramType);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, handleName, method.methodName(), eventDescriptor, false);
            mv.visitLabel(l2);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }

    private static byte[] generateClass(String name, final Class<?> handle, final ListenerClassVisitor.DiscoveredMethod method, final Class<?> eventClass) {
        name = name.replace('.', '/');
        final String handleName = Type.getInternalName(handle);
        final String handleDescriptor = Type.getDescriptor(handle);
        final String eventName = Type.getInternalName(eventClass);

        final ClassWriter cw = new LoaderClassWriter(handle.getClassLoader(), ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        MethodVisitor mv;

        cw.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, name, null, ClassEventListenerFactory.BASE_HANDLER, null);

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
