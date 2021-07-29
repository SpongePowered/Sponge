package org.spongepowered.common.command.exception;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.api.command.CommandResult;

/**
 * Used as a vehicle to transfer an error result down to the command
 * manager from a Brig supported layer.
 */
public final class SpongeCommandResultException extends CommandSyntaxException {

    private final static net.minecraft.network.chat.Component EMPTY = new TextComponent("");
    private final CommandResult result;

    public SpongeCommandResultException(final CommandResult result) {
        super(new SimpleCommandExceptionType(SpongeCommandResultException.EMPTY), SpongeCommandResultException.EMPTY);
        this.result = result;
    }

    // We're never going to use this.
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public CommandResult result() {
        return this.result;
    }

}
