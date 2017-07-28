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

import com.google.common.base.MoreObjects;
import com.google.common.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

import javax.annotation.Nullable;

public final class SpongeInjectionPoint implements AnnotatedElement {

    private final TypeToken<?> source;
    private final TypeToken<?> type;
    private final Annotation[] annotations;

    SpongeInjectionPoint(TypeToken<?> source, TypeToken<?> type, Annotation[] annotations) {
        this.annotations = annotations;
        this.source = source;
        this.type = type;
    }

    public TypeToken<?> getSource() {
        return this.source;
    }

    public TypeToken<?> getType() {
        return this.type;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
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
        return MoreObjects.toStringHelper(this)
                .add("source", this.source)
                .add("type", this.type)
                .add("annotations", Arrays.toString(this.annotations))
                .toString();
    }
}
