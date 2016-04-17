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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.command.AbstractCommandModule;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandDescription;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.annotation.Command;
import org.spongepowered.api.command.annotation.Completion;
import org.spongepowered.api.command.annotation.Default;
import org.spongepowered.api.command.annotation.Parent;
import org.spongepowered.api.command.annotation.Permission;
import org.spongepowered.api.command.annotation.PlainDescription;
import org.spongepowered.api.command.annotation.Standalone;
import org.spongepowered.api.command.annotation.TranslatableDescription;
import org.spongepowered.api.command.dispatcher.SimpleDispatcher;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.TextFunction;
import org.spongepowered.common.command.SpongeCommandManager;
import org.spongepowered.common.command.description.PlainTextCommandDescription;
import org.spongepowered.common.command.description.TranslatableCommandDescription;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * An extension of {@link SpongeCommandManager} to handle annotation-based commands.
 */
public final class AnnotationCommandFactory {

    private static final TypeToken<List<String>> STRING_LIST_TYPE_TOKEN = new TypeToken<List<String>>() {
    };
    // Forwards to SpongeCommandManager
    private final SimpleDispatcher dispatcher;
    // Forwards to SpongeCommandManager
    private final Multimap<PluginContainer, CommandMapping> owners;

    public AnnotationCommandFactory(SimpleDispatcher dispatcher, Multimap<PluginContainer, CommandMapping> owners) {
        this.dispatcher = dispatcher;
        this.owners = owners;
    }

    /**
     * Registers all found {@link Command annotation commands}.
     *
     * @param module The command module we're registering for
     * @param object The object to register commands from
     */
    public void register(final AbstractCommandModule module, final Object object) {
        final List<Method> unsuccessfulRegistration = Lists.newArrayList();

        // First, try to register commands
        for (final Method method : object.getClass().getDeclaredMethods()) {
            if (!this.register(module, object, method)) {
                unsuccessfulRegistration.add(method);
            }
        }

        // Try to associate command suggestion providers for any methods
        // we did not successfully register as a command
        if (!unsuccessfulRegistration.isEmpty()) {
            for (final Method method : unsuccessfulRegistration) {
                this.tryAssociateSuggestionProvider(module, method);
            }
        }
    }

    private boolean register(final AbstractCommandModule module, final Object object, final Method method) {
        @Nullable final Command command = method.getAnnotation(Command.class);
        if (command == null) {
            // Attempting to register annotation command from Method with no @Command annotation
            return false;
        }

        checkState(method.getAnnotation(Completion.class) == null, "Method cannot be decorated with both '@%s' and '@%s'", Command.class.getName(), Completion.class.getName());
        checkState(method.getReturnType().isAssignableFrom(CommandResult.class), "Method return type must be '%s'", CommandResult.class.getName());

        // Resolve description
        @Nullable final CommandDescription description;
        @Nullable final PlainDescription plainDescription = method.getAnnotation(PlainDescription.class);
        @Nullable final TranslatableDescription translatableDescription = method.getAnnotation(TranslatableDescription.class);
        if (plainDescription != null && translatableDescription != null) {
            throw new IllegalStateException("Method cannot be decorated with both '" + PlainDescription.class.getName() + "' and '@" + TranslatableDescription.class.getName() + "'");
        } else if (plainDescription != null) {
            description = new PlainTextCommandDescription(module.getTextSerializer(), plainDescription);
        } else if (translatableDescription != null) {
            final Optional<TextFunction> textFunction = module.getTextFunction();
            if (!textFunction.isPresent()) {
                throw new IllegalStateException("Method is annotated with @TranslatableDescription, but a TextFunction has not been provided to the command module");
            }

            description = new TranslatableCommandDescription(textFunction.get(), translatableDescription);
        } else {
            throw new IllegalStateException("Could not resolve @PlainDescription or @TranslatableDescription");
        }

        // Resolve permissions
        @Nullable final Permission permission = method.getAnnotation(Permission.class);
        @Nullable final String[] permissions = permission == null ? null : permission.value();

        final ArgumentParser parser = ArgumentParser.of(module.getBinder(), method);
        final CommandCallable callable = new AnnotationCommandCallable(object, method, parser, description, permissions, module.getInputTokenizer());
        final List<String> aliases = Arrays.asList(command.value());

        @Nullable final Default defaultAnnotation = method.getAnnotation(Default.class);
        // Resolve groups
        boolean grouped = false;
        boolean defaultedOnly = false;
        for (final Parent parent : method.getAnnotationsByType(Parent.class)) {
            final RegistrationResult result = this.registerGroup(module.getPlugin(), callable, aliases, parent, defaultAnnotation, method);
            if (result == RegistrationResult.GROUP) {
                grouped = true;
            } else if (result == RegistrationResult.DEFAULT && !grouped) {
                defaultedOnly = true;
            }
        }

        @Nullable final Standalone standalone = method.getAnnotation(Standalone.class);
        if (!grouped || standalone != null) {
            // Tell people that they're being silly.
            if (!defaultedOnly && defaultAnnotation != null) {
                module.getPlugin().getLogger().warn("Annotation command '{}' (from '{}') is decorated with '@{}' but is a root-only command - ignoring", aliases, object.getClass().getName(), Default.class.getName());
            }

            this.dispatcher.register(callable, aliases).ifPresent(mapping -> this.owners.put(module.getPlugin(), mapping));
        }

        return true;
    }

    private RegistrationResult registerGroup(final PluginContainer container, final CommandCallable callable, final List<String> aliases, final Parent parent, @Nullable final Default defaultAnnotation, final Method method) {
        SimpleDispatcher dispatcher = this.dispatcher;

        for (final String part : parent.value()) {
            @Nullable final CommandCallable sibling = dispatcher.get(part).map(CommandMapping::getCallable).orElse(null);
            if (sibling == null) {
                final SimpleDispatcher orphan = new SimpleDispatcher();
                dispatcher.register(orphan, part);
                dispatcher = orphan;
            } else if (sibling instanceof SimpleDispatcher) {
                dispatcher = (SimpleDispatcher) sibling;
            } else {
                throw new IllegalStateException("Can't put command '" + method.getName() + "' at " + Arrays.toString(parent.value()) + " because there is an existing command there: " + sibling.toString());
            }
        }

        if (defaultAnnotation != null && !this.isRootDispatcher(dispatcher)) {
            dispatcher.setDefaultCommand(callable);
            if (defaultAnnotation.defaultOnly()) {
                return RegistrationResult.DEFAULT;
            }
        }

        dispatcher.register(callable, aliases).ifPresent(mapping -> this.owners.put(container, mapping));

        return RegistrationResult.GROUP;
    }

    private void tryAssociateSuggestionProvider(final AbstractCommandModule module, final Method method) {
        @Nullable final Completion completion = method.getAnnotation(Completion.class);
        if (completion == null) {
            return;
        }

        checkState(STRING_LIST_TYPE_TOKEN.isAssignableFrom(method.getGenericReturnType()), "Method return type must be List<String>");

        SimpleDispatcher dispatcher = this.dispatcher;
        @Nullable CommandCallable sibling = null;

        for (String part : completion.value()) {
            sibling = dispatcher.get(part).map(CommandMapping::getCallable).orElse(null);
            if (sibling instanceof SimpleDispatcher) {
                dispatcher = (SimpleDispatcher) sibling;
            }
        }

        if (sibling != null && sibling instanceof AnnotationCommandCallable) {
            ArgumentParser parser = ArgumentParser.of(module.getBinder(), method);
            ((AnnotationCommandCallable) sibling).setSuggestionMethod(method, parser);
        }
    }

    private boolean isRootDispatcher(final SimpleDispatcher dispatcher) {
        return dispatcher == this.dispatcher;
    }

    private enum RegistrationResult {
        /**
         * The command was registered in a group.
         */
        GROUP,
        /**
         * The command was registered as a default only.
         */
        DEFAULT;
    }
}
