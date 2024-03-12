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
package org.spongepowered.common.inject;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.StringJoiner;

public final class SpongeInjectionPoint implements AnnotatedElement {

    private final Type source;
    private final Type type;
    private final Annotation[] annotations;

    SpongeInjectionPoint(final Type source, final Type type, final Annotation[] annotations) {
        this.annotations = annotations;
        this.source = source;
        this.type = type;
    }

    public Type getSource() {
        return this.source;
    }

    public Type getType() {
        return this.type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> @Nullable A getAnnotation(final Class<A> annotationClass) {
        return (A) Arrays.stream(this.annotations).filter(annotationClass::isInstance).findFirst().orElse(null);
    }

    @Override
    public Annotation[] getAnnotations() {
        return Arrays.copyOf(this.annotations, this.annotations.length);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return Arrays.copyOf(this.annotations, this.annotations.length);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeInjectionPoint.class.getSimpleName() + "[", "]")
                .add("source=" + this.source)
                .add("type=" + this.type)
                .add("annotations=" + Arrays.toString(this.annotations))
                .toString();
    }
}
