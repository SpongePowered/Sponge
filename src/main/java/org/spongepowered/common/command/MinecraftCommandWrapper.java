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
package org.spongepowered.common.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.InvocationCommandException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.command.ICommandBridge;
import org.spongepowered.common.bridge.command.CommandHandlerBridge;
import org.spongepowered.common.mixin.core.command.CommandHandlerAccessor;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Wrapper around ICommands so they fit into the Sponge command system.
 */
public class MinecraftCommandWrapper implements CommandCallable {

    private static final String TRANSLATION_NO_PERMISSION = "commands.generic.permission";
    private final PluginContainer owner;
    protected final ICommand command;
    private static final ThreadLocal<Deque<Throwable>> commandErrors = new ThreadLocal<Deque<Throwable>>() {

        @Override
        protected Deque<Throwable> initialValue() {
            return new LinkedList<>();
        }
    };
    // This differs from null in that null means "not active".
    private static final Exception noError = new Exception();

    public MinecraftCommandWrapper(final PluginContainer owner, final ICommand command) {
        this.owner = owner;
        this.command = command;

        // Add the namespaced alias to the wrapped command so permission checks are sent to the right command.
        if (this.command instanceof ICommandBridge) {
            ((ICommandBridge) this.command).bridge$updateNamespacedAlias(this.owner.getId());
        }
    }

    private String[] splitArgs(String arguments) {
        // Why split with a limit of -1? Because that's how you make it include trailing spaces. Because that makes sense. Isn't java great?
        return arguments.isEmpty() ? new String[0] : arguments.split(" ", -1);
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {

        if (!testPermission(source)) {
            throw new CommandPermissionException(Text.of(SpongeImpl.getGame().getRegistry()
                    .getTranslationById(TRANSLATION_NO_PERMISSION).get()));
        }

        CommandHandler handler = (CommandHandler) SpongeImpl.getServer().getCommandManager();
        final ICommandSender mcSender = WrapperICommandSender.of(source);
        final String[] splitArgs = splitArgs(arguments);
        int usernameIndex = 0;
        try {
            usernameIndex = ((CommandHandlerAccessor) handler).accessor$getUsernameIndex(this.command, splitArgs);
        } catch (net.minecraft.command.CommandException e) {
            throw new RuntimeException(e);
        }
        int successCount = 0;

        if (!throwEvent(mcSender, splitArgs)) {
            return CommandResult.empty();
        }
        // Below this is copied from CommandHandler.execute. This might need to be updated between versions.
        int affectedEntities = 1;
        if (usernameIndex > -1) {
            List<Entity> list = null;
            try {
                list = EntitySelector.matchEntities(mcSender, splitArgs[usernameIndex], Entity.class);
            } catch (net.minecraft.command.CommandException e) {
                throw new RuntimeException(e);
            }
            String previousNameVal = splitArgs[usernameIndex];
            affectedEntities = list.size();

            ((CommandHandlerBridge) handler).bridge$setExpandedSelector(true);

            for (Entity entity : list) {
                splitArgs[usernameIndex] = entity.getCachedUniqueIdString();

                if (tryExecute(handler, mcSender, splitArgs, arguments)) {
                    ++successCount;
                }
            }
            splitArgs[usernameIndex] = previousNameVal;

            ((CommandHandlerBridge) handler).bridge$setExpandedSelector(false);
        } else {
            if (tryExecute(handler, mcSender, splitArgs, arguments)) {
                ++successCount;
            }
        }

        return CommandResult.builder()
                .affectedEntities(affectedEntities)
                .successCount(successCount)
                .build();

    }

    private boolean tryExecute(CommandHandler handler, ICommandSender mcSender, String[] splitArgs, String arguments) {
        commandErrors.get().push(noError);
        try {
            return ((CommandHandlerAccessor) handler).accessor$tryExecuteCommand(mcSender, splitArgs, this.command, arguments);
        } finally {
            Throwable error = commandErrors.get().pop();
            if (error != noError) {
                throw new RuntimeException(error);
            }
        }
    }

    protected boolean throwEvent(ICommandSender sender, String[] args) throws InvocationCommandException {
        return true;
    }

    public int getPermissionLevel() {
        return this.command instanceof CommandBase ? ((CommandBase) this.command).getRequiredPermissionLevel() : -1;
    }

    public String getCommandPermission() {
        return this.owner.getId().toLowerCase() + ".command." + this.command.getName();
    }

    public PluginContainer getOwner() {
        return this.owner;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        ICommandSender sender = WrapperICommandSender.of(source);
        return this.command.checkPermission(sender.getServer(), sender);
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return getHelp(source);
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        String translation = this.command.getUsage(WrapperICommandSender.of(source));
        if (translation == null) {
            return Optional.empty();
        }
        return Optional.of((Text) Text.of(new SpongeTranslation(translation)));
    }

    @Override
    public Text getUsage(CommandSource source) {
        final ICommandSender mcSender = WrapperICommandSender.of(source);
        String usage = this.command.getUsage(mcSender);
        if (usage == null) { // Silly modders
            return Text.of();
        }

        Translation translation = SpongeImpl.getGame().getRegistry().getTranslationById(usage).get();
        if (source instanceof Player) {
            usage = translation.get(source.getLocale());
        } else {
            usage = translation.get(Locale.getDefault());
        }

        List<String> parts = new ArrayList<>(Arrays.asList(usage.split(" ")));
        parts.removeAll(Collections.singleton("/" + this.command.getName()));
        StringBuilder out = new StringBuilder();
        for (String s : parts) {
            out.append(s);
            out.append(" ");
        }
        return Text.of(out.toString().trim());
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
        if (!testPermission(source)) {
            return ImmutableList.of();
        }
        List<String> suggestions = this.command.getTabCompletions(SpongeImpl.getServer(),
                WrapperICommandSender.of(source), arguments.split(" ", -1), targetPosition == null ? null : VecHelper.toBlockPos(targetPosition));
        if (suggestions == null) {
            return ImmutableList.of();
        }
        return suggestions;
    }

    public List<String> getNames() {
        return ImmutableList.<String>builder().add(this.command.getName()).addAll(this.command.getAliases()).build();
    }

    public static void setError(Throwable error) {
        checkNotNull(error);
        Throwable old = commandErrors.get().peek();
        if (old == null) {
            throw new IllegalStateException();
        }
        if (old == noError) {
            commandErrors.get().pop();
            commandErrors.get().push(error);
        }
    }

    public boolean suppressDuplicateAlias(String alias) {
        return false;
    }
}
