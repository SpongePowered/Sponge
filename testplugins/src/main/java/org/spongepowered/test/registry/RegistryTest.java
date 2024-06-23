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
package org.spongepowered.test.registry;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.annotation.CatalogedBy;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

@Plugin("registrytest")
public final class RegistryTest implements LoadableModule {

    private final PluginContainer plugin;
    private final Logger logger;
    private boolean verbose;

    @Inject
    public RegistryTest(final PluginContainer plugin, final Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @Override
    public void enable(final CommandContext ctx) {
    }

    @Listener
    private void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(
            this.plugin,
            Command.builder()
                .executor(context -> {
                    this.verbose = !this.verbose;
                    context.sendMessage(Component.text("Verbose flag set to " + this.verbose));
                    return CommandResult.success();
                })
                .build(),
            "toggleverbose");
        event.register(
            this.plugin,
            Command.builder()
                .executor(context -> {
                    for (final Field field : RegistryTypes.class.getDeclaredFields()) {
                        final Object registryField;
                        try {
                            registryField = field.get(null);
                        } catch (final IllegalAccessException e) {
                            this.logger.error("Failed to get field {}: {}", field.getName(), e.getMessage());
                            if (this.verbose) {
                                this.logger.error("Exception", e);
                            }
                            continue;
                        }

                        if (registryField instanceof DefaultedRegistryType<?> registryType) {
                            if (registryType.find().isEmpty()) {
                                this.logger.error("Registry {} is empty", registryType.location());
                                continue;
                            }

                            final var typeArg = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                            final Class<?> catalogEntryClass;
                            switch (typeArg) {
                                case final ParameterizedType parameterizedTypeArg ->
                                    catalogEntryClass = (Class<?>) parameterizedTypeArg.getRawType();
                                case final Class<?> clazz -> catalogEntryClass = clazz;
                                case null, default -> {
                                    this.logger.error("Unhandled catalog entry arg type: {}", typeArg);
                                    continue;
                                }
                            }

                            final var catalogedByAnnotation = catalogEntryClass.getDeclaredAnnotation(CatalogedBy.class);

                            if (catalogedByAnnotation == null) {
                                this.logger.error("Class {} in registry {} is not annotated with CatalogedBy", catalogEntryClass.getSimpleName(), registryType.location());
                                continue;
                            }

                            final var catalogClass = catalogedByAnnotation.value()[0];
                            if (!Modifier.isFinal(catalogClass.getModifiers())) {
                                this.logger.error("{} is not final", catalogClass.getSimpleName());
                            }

                            if (Arrays.stream(catalogClass.getDeclaredConstructors()).anyMatch(ctor -> !Modifier.isPrivate(ctor.getModifiers()))) {
                                this.logger.error("{} has non-private constructors", catalogClass.getSimpleName());
                            }

                            final Method registryMethod;
                            try {
                                registryMethod = catalogClass.getDeclaredMethod("registry");
                            } catch (final NoSuchMethodException e) {
                                this.logger.error("{}.registry() does not exist", catalogClass.getSimpleName());
                                continue;
                            }

                            final Object registryReturn;
                            try {
                                registryReturn = registryMethod.invoke(null);
                            } catch (final Throwable e) {
                                this.logger.error("{}.registry() failed: {}", catalogClass.getSimpleName(), e.getMessage());
                                if (this.verbose) {
                                    this.logger.error("Exception", e);
                                }
                                continue;
                            }

                            if (registryReturn == null) {
                                this.logger.error("{}.registry() returned null", catalogClass.getSimpleName());
                                continue;
                            }

                            if (registryReturn != registryType.get()) {
                                this.logger.error("{}.registry() returned a different registry than the one specified in RegistryTypes", catalogClass.getSimpleName());
                                continue;
                            }

                            for (Field catalogField : catalogClass.getDeclaredFields()) {
                                final Object catalogObj;
                                try {
                                    catalogObj = catalogField.get(null);
                                } catch (final Throwable e) {
                                    this.logger.error("Failed to get field {}: {}", catalogField.getName(), e.getMessage());
                                    if (this.verbose) {
                                        this.logger.error("Exception", e);
                                    }
                                    continue;
                                }

                                if (catalogObj instanceof DefaultedRegistryReference<?> reference) {
                                    if (reference.find().isEmpty()) {
                                        this.logger.error("{}.{}.find() is empty", catalogClass.getSimpleName(), catalogField.getName());
                                    }
                                }
                            }

                        } else {
                            this.logger.error("{} is not a DefaultedRegistryType", field.getName());
                        }
                    }
                    return CommandResult.success();
                })
                .build(),
            "checkregistries");
    }
}
