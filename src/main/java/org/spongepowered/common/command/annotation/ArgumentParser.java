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
package org.spongepowered.common.command.annotation;

import com.google.common.collect.Lists;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.annotation.Classifier;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.binding.Binder;
import org.spongepowered.api.command.binding.Binding;
import org.spongepowered.api.command.binding.BindingKey;
import org.spongepowered.api.command.provider.ProvisionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

final class ArgumentParser {

    private final List<Parameter> parameters;

    private ArgumentParser(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    Object[] parse(CommandSource source, CommandArgs args) throws ProvisionException {
        Object[] parsedObjects = new Object[this.parameters.size()];

        for (int i = 0; i < this.parameters.size(); i++) {
            Parameter entry = this.parameters.get(i);
            parsedObjects[i] = entry.binding.getProvider().get(source, args, entry.modifiers);
        }

        return parsedObjects;
    }

    static ArgumentParser of(Binder binder, Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        Type[] types = method.getGenericParameterTypes();

        List<Parameter> parameters = Lists.newArrayList();
        for (int i = 0; i < types.length; i++) {
            parameters.add(createParameter(binder, types[i], Arrays.asList(annotations[i])));
        }

        return new ArgumentParser(parameters);
    }

    private static <T> Parameter createParameter(Binder binder, Type type, List<? extends Annotation> annotations) {
        @Nullable Annotation classifier = null;
        List<Annotation> modifiers = Lists.newArrayList();

        for (Annotation annotation : annotations) {
            if (annotation.annotationType().getAnnotation(Classifier.class) != null) {
                classifier = annotation;
            } else {
                modifiers.add(annotation);
            }
        }

        Optional<Binding<T>> binding = binder.getBinding(BindingKey.of(type, classifier != null ? classifier.annotationType() : null));
        if (!binding.isPresent()) {
            throw new IllegalArgumentException("Could not find a binding for the parameter type '" + type + "'");
        }

        return new Parameter(binding.get(), modifiers);
    }

    private static class Parameter {

        private final Binding<?> binding;
        private final List<Annotation> modifiers;

        Parameter(Binding<?> binding, List<Annotation> modifiers) {
            this.binding = binding;
            this.modifiers = modifiers;
        }
    }
}
