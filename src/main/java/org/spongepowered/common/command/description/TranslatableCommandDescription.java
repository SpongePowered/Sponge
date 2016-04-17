package org.spongepowered.common.command.description;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.command.CommandDescription;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.annotation.TranslatableDescription;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.TextFunction;

import java.util.Optional;

import javax.annotation.Nullable;

public class TranslatableCommandDescription implements CommandDescription {

    protected final TextFunction function;
    @Nullable protected final String description;
    @Nullable protected final String help;
    protected final String usage;

    @SuppressWarnings("ConstantConditions")
    public TranslatableCommandDescription(TextFunction function, TranslatableDescription description) {
        this(
                function,
                fromString(description.description()),
                fromString(description.help()),
                fromString(description.usage())
        );
    }

    @Nullable
    private static String fromString(String string) {
        return string.isEmpty() ? null : string;
    }

    public TranslatableCommandDescription(TextFunction function, @Nullable String description, @Nullable String help, String usage) {
        this.function = checkNotNull(function, "function");
        this.description = description;
        this.help = help;
        this.usage = checkNotNull(usage, "usage");
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        if (this.description == null) {
            return Optional.empty();
        }

        return Optional.of(this.function.apply(source, this.description));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        if (this.help == null) {
            return Optional.empty();
        }

        return Optional.of(this.function.apply(source, this.help));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return this.function.apply(source, this.usage);
    }

}
