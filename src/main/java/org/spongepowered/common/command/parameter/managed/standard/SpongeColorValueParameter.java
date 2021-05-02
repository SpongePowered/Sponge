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
package org.spongepowered.common.command.parameter.managed.standard;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.ColorArgument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.accessor.ChatFormattingAccessor;
import org.spongepowered.common.command.SpongeCommandCompletion;
import org.spongepowered.common.command.brigadier.argument.ResourceKeyedArgumentValueParser;
import org.spongepowered.common.util.Constants;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SpongeColorValueParameter extends ResourceKeyedArgumentValueParser<Color> {

    private final static Component EXCEPTION_MESSAGE = Component.text().content(
            "The color is not in one of the expected formats:").append(Component.newline())
            .append(Component.text("* Named color (such as \"black\")")).append(Component.newline())
            .append(Component.text("* Hex encoded color consisting of six digits (such as \"000000\")"))
            .append(Component.newline())
            .append(Component.text("* Hex encoded color, surrounded by double quotes, starting with # and consisting of six digits (such as \"#000000\")"))
            .append(Component.newline())
            .append(Component.text("* Comma separated RGB color, surrounded by double quotes, with values from 0 to 255 (such as \"0,128,255\")"))
            .build();
    private final static Pattern HEX_CODE = Pattern.compile("(#?)(?<colorcode>[0-9A-Fa-f]{6})");
    private final static Collection<String> CHAT_FORMATTING_NAMES = Arrays.stream(ChatFormatting.values())
            .filter(x -> x.isColor())
            .map(x -> x.getName().toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());
    private final ColorArgument colorArgumentType = ColorArgument.color();

    public SpongeColorValueParameter(final ResourceKey key) {
        super(key);
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(
            final com.mojang.brigadier.context.CommandContext<?> context,
            final SuggestionsBuilder builder) {

        return this.colorArgumentType.listSuggestions(context, builder);
    }

    @Override
    public @NonNull Optional<? extends Color> parseValue(
            final @NonNull CommandCause cause, final ArgumentReader.@NonNull Mutable reader) throws ArgumentParseException {

        final ArgumentReader.Immutable state = reader.immutable();
        // First, is the argument type giving the correct return type?
        try {
            final ChatFormatting formatting = this.colorArgumentType.parse((StringReader) reader);
            final Integer colorCode = ((ChatFormattingAccessor) (Object) formatting).accessor$color();
            if (colorCode != null) {
                return Optional.of(Color.ofRgb(colorCode));
            }
            // "reset" slips through the net here.
            throw reader.createException(Component.text().content(String.format("%s is not a valid color", formatting.getName())).build());
        } catch (final CommandSyntaxException e) {
            // ignored
        }
        reader.setState(state);
        // Hex codes and comma separated RGB values require commas, so we need to parse the string,
        // rather than use the unquoted string the ColorArgument does.
        final String string = reader.parseString();

        // Hex code (with optional #)
        final Matcher matcher = SpongeColorValueParameter.HEX_CODE.matcher(string);
        if (matcher.matches()) {
            try {
                return Optional.of(Color.ofRgb(Integer.parseInt(matcher.group("colorcode"), 16)));
            } catch (final NumberFormatException ex) {
                // handled below
            }
        }

        final String[] rgb = string.split(",", 3);
        if (rgb.length == 3) {
            final Optional<Color> result = this.checkIntConversion(rgb);
            if (result.isPresent()) {
                return result;
            }
        }

        throw reader.createException(SpongeColorValueParameter.EXCEPTION_MESSAGE);
    }

    @Override
    public List<CommandCompletion> complete(final CommandCause context, final String currentInput) {
        return SpongeColorValueParameter.CHAT_FORMATTING_NAMES
                .stream()
                .filter(x -> x.startsWith(currentInput.toLowerCase(Locale.ROOT)))
                .map(SpongeCommandCompletion::new)
                .collect(Collectors.toList());
    }

    private Optional<Color> checkIntConversion(final String[] entry) {
        try {
            final int r = Integer.parseInt(entry[0]);
            final int g = Integer.parseInt(entry[1]);
            final int b = Integer.parseInt(entry[2]);
            if (this.isInRange(r) && this.isInRange(g) && this.isInRange(b)) {
                return Optional.of(Color.ofRgb(r, g, b));
            }
        } catch (final Exception e) {
            // ignored
        }
        return Optional.empty();
    }

    private boolean isInRange(final int i) {
        return i >= 0 && i <= 255;
    }

    // Enforce the requirement for a quoted string
    @Override
    public List<ArgumentType<?>> getClientCompletionArgumentType() {
        return Collections.singletonList(Constants.Command.STANDARD_STRING_ARGUMENT_TYPE);
    }

}
