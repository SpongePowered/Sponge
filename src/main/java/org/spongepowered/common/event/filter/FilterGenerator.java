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
package org.spongepowered.common.event.filter;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.After;
import org.spongepowered.api.event.filter.cause.All;
import org.spongepowered.api.event.filter.cause.Before;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.data.Has;
import org.spongepowered.api.event.filter.data.Supports;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.event.filter.delegate.AfterCauseFilterSourceDelegate;
import org.spongepowered.common.event.filter.delegate.AllCauseFilterSourceDelegate;
import org.spongepowered.common.event.filter.delegate.BeforeCauseFilterSourceDelegate;
import org.spongepowered.common.event.filter.delegate.CancellationEventFilterDelegate;
import org.spongepowered.common.event.filter.delegate.ExcludeSubtypeFilterDelegate;
import org.spongepowered.common.event.filter.delegate.FilterDelegate;
import org.spongepowered.common.event.filter.delegate.FirstCauseFilterSourceDelegate;
import org.spongepowered.common.event.filter.delegate.GetterFilterSourceDelegate;
import org.spongepowered.common.event.filter.delegate.HasDataFilterDelegate;
import org.spongepowered.common.event.filter.delegate.IncludeSubtypeFilterDelegate;
import org.spongepowered.common.event.filter.delegate.LastCauseFilterSourceDelegate;
import org.spongepowered.common.event.filter.delegate.ParameterFilterDelegate;
import org.spongepowered.common.event.filter.delegate.ParameterFilterSourceDelegate;
import org.spongepowered.common.event.filter.delegate.RootCauseFilterSourceDelegate;
import org.spongepowered.common.event.filter.delegate.SubtypeFilterDelegate;
import org.spongepowered.common.event.filter.delegate.SupportsDataFilterDelegate;
import org.spongepowered.common.util.generator.GeneratorUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FilterGenerator {

    public static final boolean FILTER_DEBUG = Boolean.parseBoolean(System.getProperty("sponge.filter.debug", "false"));

    public static FilterGenerator getInstance() {
        return Holder.INSTANCE;
    }

    FilterGenerator() {
    }

    public byte[] generateClass(String name, Method method) {
        name = name.replace('.', '/');
        Parameter[] params = method.getParameters();

        SubtypeFilterDelegate sfilter = null;
        final List<FilterDelegate> additional = new ArrayList<>();
        boolean cancellation = false;
        for (Annotation anno : method.getAnnotations()) {
            Object obj = FilterGenerator.filterFromAnnotation(anno.annotationType());
            if (obj == null) {
                continue;
            }
            if (obj instanceof SubtypeFilter) {
                if (sfilter != null) {
                    throw new IllegalStateException("Cannot have both @Include and @Exclude annotations present at once");
                }
                sfilter = ((SubtypeFilter) obj).getDelegate(anno);
            } else if (obj instanceof EventTypeFilter) {
                EventTypeFilter etf = (EventTypeFilter) obj;
                additional.add(etf.getDelegate(anno));
                if (etf == EventTypeFilter.CANCELLATION) {
                    cancellation = true;
                }
            }
        }
        if (!cancellation && Cancellable.class.isAssignableFrom(params[0].getType())) {
            additional.add(new CancellationEventFilterDelegate(Tristate.FALSE));
        }

        // we know there are no filters, skip generating a class
        if (additional.isEmpty() && sfilter == null && params.length == 1) {
            return null;
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        MethodVisitor mv;

        cw.visit(V1_6, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, name, null, "java/lang/Object", new String[] { Type.getInternalName(EventFilter.class) });

        if (sfilter != null) {
            sfilter.createFields(cw);
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            if (sfilter != null) {
                sfilter.writeCtor(name, cw, mv);
            }
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "filter", "(" + Type.getDescriptor(Event.class) + ")[Ljava/lang/Object;", null, null);
            mv.visitCode();
            // index of the next available local variable
            int local = 2;
            if (sfilter != null) {
                local = sfilter.write(name, cw, mv, method, local);
            }
            for (FilterDelegate eventFilter : additional) {
                local = eventFilter.write(name, cw, mv, method, local);
            }

            // local var indices of the parameters values
            int[] plocals = new int[params.length - 1];
            for (int i = 1; i < params.length; i++) {
                Parameter param = params[i];
                ParameterFilterSourceDelegate source = null;
                List<ParameterFilterDelegate> paramFilters = new ArrayList<>();
                for (Annotation anno : param.getAnnotations()) {
                    Object obj = FilterGenerator.filterFromAnnotation(anno.annotationType());
                    if (obj == null) {
                        continue;
                    }
                    if (obj instanceof ParameterSource) {
                        if (source != null) {
                            throw new IllegalStateException("Cannot have multiple parameter filter source annotations (for " + param.getName() + ")");
                        }
                        source = ((ParameterSource) obj).getDelegate(anno);
                    } else if (obj instanceof ParameterFilter) {
                        paramFilters.add(((ParameterFilter) obj).getDelegate(anno));
                    }
                }
                if (source == null) {
                    throw new IllegalStateException("Cannot have additional parameters filters without a source (for " + param.getName() + ")");
                }
                if (source instanceof AllCauseFilterSourceDelegate && !paramFilters.isEmpty()) {
                    // TODO until better handling for filtering arrays is added
                    throw new IllegalStateException(
                            "Cannot have additional parameters filters without an array source (for " + param.getName() + ")");
                }
                Tuple<Integer, Integer> localState = source.write(cw, mv, method, param, local);
                local = localState.first();
                plocals[i - 1] = localState.second();

                for (ParameterFilterDelegate paramFilter : paramFilters) {
                    paramFilter.write(cw, mv, method, param, plocals[i - 1]);
                }
            }

            // create the return array
            if (params.length == 1) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitIntInsn(BIPUSH, params.length);
            }
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            // load the event into the array
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(AASTORE);
            // load all the params into the array
            for (int i = 1; i < params.length; i++) {
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, i);
                Type paramType = Type.getType(params[i].getType());
                mv.visitVarInsn(paramType.getOpcode(ILOAD), plocals[i - 1]);
                GeneratorUtils.visitBoxingMethod(mv, paramType);
                mv.visitInsn(AASTORE);
            }
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();
        byte[] data = cw.toByteArray();

        if (FilterGenerator.FILTER_DEBUG) {
            File outDir = new File(".sponge.debug.out");
            File outFile = new File(outDir, name + ".class");
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                out.write(data);
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }

        return data;
    }

    private static Object filterFromAnnotation(Class<? extends Annotation> cls) {
        Object filter;
        if ((filter = SubtypeFilter.valueOf(cls)) != null)
            return filter;
        if ((filter = EventTypeFilter.valueOf(cls)) != null)
            return filter;
        if ((filter = ParameterSource.valueOf(cls)) != null)
            return filter;
        if ((filter = ParameterFilter.valueOf(cls)) != null)
            return filter;
        return null;
    }

    private enum SubtypeFilter {
        INCLUDE(Include.class, IncludeSubtypeFilterDelegate::new),
        EXCLUDE(Exclude.class, ExcludeSubtypeFilterDelegate::new),
        ;

        private static final Map<Class<? extends Annotation>, SubtypeFilter> BY_CLAZZ;
        private final Class<? extends Annotation> cls;
        private final Function<Annotation, SubtypeFilterDelegate> factory;

        @SuppressWarnings("unchecked")
        <T extends Annotation> SubtypeFilter(final Class<? extends Annotation> cls, final Function<T, SubtypeFilterDelegate> factory) {
            this.cls = cls;
            this.factory = (Function<Annotation, SubtypeFilterDelegate>) factory;
        }

        public SubtypeFilterDelegate getDelegate(final Annotation anno) {
            return this.factory.apply(anno);
        }

        public static SubtypeFilter valueOf(final Class<? extends Annotation> cls) {
            return SubtypeFilter.BY_CLAZZ.get(cls);
        }

        static {
            final Map<Class<? extends Annotation>, SubtypeFilter> byClazz = new HashMap<>();
            for (final SubtypeFilter value : SubtypeFilter.values()) {
                byClazz.put(value.cls, value);
            }
            BY_CLAZZ = Collections.unmodifiableMap(byClazz);
        }
    }

    private enum EventTypeFilter {
        CANCELLATION(IsCancelled.class, CancellationEventFilterDelegate::new),
        ;

        private static final Map<Class<? extends Annotation>, EventTypeFilter> BY_CLAZZ;
        private final Class<? extends Annotation> cls;
        private final Function<Annotation, FilterDelegate> factory;

        @SuppressWarnings("unchecked")
        <T extends Annotation> EventTypeFilter(final Class<T> cls, final Function<T, FilterDelegate> factory) {
            this.cls = cls;
            this.factory = (Function<Annotation, FilterDelegate>) factory;
        }

        public FilterDelegate getDelegate(final Annotation anno) {
            return this.factory.apply(anno);
        }

        public static EventTypeFilter valueOf(Class<? extends Annotation> cls) {
            return EventTypeFilter.BY_CLAZZ.get(cls);
        }

        static {
           final Map<Class<? extends Annotation>, EventTypeFilter> byClazz = new HashMap<>();
            for (final EventTypeFilter value : EventTypeFilter.values()) {
                byClazz.put(value.cls, value);
            }
            BY_CLAZZ = Collections.unmodifiableMap(byClazz);
        }
    }

    private enum ParameterSource {
        CAUSE_FIRST(First.class, FirstCauseFilterSourceDelegate::new),
        CAUSE_LAST(Last.class, LastCauseFilterSourceDelegate::new),
        CAUSE_BEFORE(Before.class, BeforeCauseFilterSourceDelegate::new),
        CAUSE_AFTER(After.class, AfterCauseFilterSourceDelegate::new),
        CAUSE_ALL(All.class, AllCauseFilterSourceDelegate::new),
        CAUSE_ROOT(Root.class, RootCauseFilterSourceDelegate::new),
        GETTER(Getter.class, GetterFilterSourceDelegate::new),
        ;

        private static final Map<Class<? extends Annotation>, ParameterSource> BY_CLAZZ;
        private final Class<? extends Annotation> cls;
        private final Function<Annotation, ParameterFilterSourceDelegate> factory;

        @SuppressWarnings("unchecked")
        <T extends Annotation> ParameterSource(final Class<T> cls, final Function<T, ParameterFilterSourceDelegate> factory) {
            this.cls = cls;
            this.factory = (Function<Annotation, ParameterFilterSourceDelegate>) factory;
        }

        public ParameterFilterSourceDelegate getDelegate(Annotation anno) {
            return this.factory.apply(anno);
        }

        public static ParameterSource valueOf(final Class<? extends Annotation> cls) {
            return ParameterSource.BY_CLAZZ.get(cls);
        }

        static {
            final Map<Class<? extends Annotation>, ParameterSource> byClazz = new HashMap<>();
            for (final ParameterSource value : ParameterSource.values()) {
                byClazz.put(value.cls, value);
            }
            BY_CLAZZ = Collections.unmodifiableMap(byClazz);
        }
    }

    private enum ParameterFilter {
        SUPPORTS(Supports.class, SupportsDataFilterDelegate::new),
        HAS(Has.class, HasDataFilterDelegate::new),
        ;

        private static final Map<Class<? extends Annotation>, ParameterFilter> BY_CLAZZ;
        private final Class<? extends Annotation> cls;
        private final Function<Annotation, ParameterFilterDelegate> factory;

        @SuppressWarnings("unchecked")
        <T extends Annotation> ParameterFilter(final Class<T> cls, final Function<T, ParameterFilterDelegate> factory) {
            this.cls = cls;
            this.factory = (Function<Annotation, ParameterFilterDelegate>) factory;
        }

        public ParameterFilterDelegate getDelegate(final Annotation anno) {
            return this.factory.apply(anno);
        }

        public static ParameterFilter valueOf(final Class<? extends Annotation> cls) {
            return ParameterFilter.BY_CLAZZ.get(cls);
        }

        static {
            final Map<Class<? extends Annotation>, ParameterFilter> byClazz = new HashMap<>();
            for (final ParameterFilter value : ParameterFilter.values()) {
                byClazz.put(value.cls, value);
            }
            BY_CLAZZ = Collections.unmodifiableMap(byClazz);
        }
    }

    private static final class Holder {

        static final FilterGenerator INSTANCE = new FilterGenerator();

    }

}
