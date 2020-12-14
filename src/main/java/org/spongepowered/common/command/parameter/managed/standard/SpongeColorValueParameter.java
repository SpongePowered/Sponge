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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.command.arguments.ColorArgument;
import net.minecraft.util.text.TextFormatting;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.accessor.util.text.TextFormattingAccessor;
import org.spongepowered.common.command.brigadier.argument.CatalogedArgumentParser;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SpongeColorValueParameter extends CatalogedArgumentParser<Color> {

    private final static Pattern HEX_CODE = Pattern.compile("#[0-9A-Fa-f]{6}");
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
    @NonNull
    public List<String> complete(@NonNull final CommandContext context, final String currentInput) {
        final SuggestionsBuilder builder = new SuggestionsBuilder(currentInput, 0);
        this.listSuggestions((com.mojang.brigadier.context.CommandContext<?>) context, builder);
        return builder.build().getList().stream().map(Suggestion::getText).collect(Collectors.toList());
    }

    @Override
    @NonNull
    public Optional<? extends Color> getValue(
            final Parameter.@NonNull Key<? super Color> parameterKey,
            final ArgumentReader.@NonNull Mutable reader,
            final CommandContext.@NonNull Builder context) throws ArgumentParseException {

        final ArgumentReader.Immutable state = reader.getImmutable();
        // First, is the argument type giving the correct return type?
        try {
            final TextFormatting formatting = this.colorArgumentType.parse((StringReader) reader);
            final Integer colorCode = ((TextFormattingAccessor) (Object) formatting).accessor$color();
            if (colorCode != null) {
                return Optional.of(Color.ofRgb(colorCode));
            }
        } catch (final CommandSyntaxException e) {
            // ignored
        }
        reader.setState(state);
        final String string = reader.parseUnquotedString();

        // Hex code?
        if (SpongeColorValueParameter.HEX_CODE.matcher(string).matches()) {
            // Hex code
        }

        final String[] rgb = string.split(",", 3);
        if (rgb.length == 3) {
            try {
                return Optional.of(Color.ofRgb(this.checkIntConversion(rgb[0]), this.checkIntConversion(rgb[1]), this.checkIntConversion(rgb[2])));
            } catch (final Exception e) {
                // ignored, handled below
            }
        }

        throw reader.createException(Component.text().content(
                "The color is not in one of the expected formats:").append(Component.newline())
                .append(Component.text("* Named color (such as \"black\")")).append(Component.newline())
                .append(Component.text("* Hex encoded color, starting with # (such as \"#000000\")")).append(Component.newline())
                .append(Component.text("* Comma separated RGB color, with values from 0 to 255 (such as \"0,128,255\")"))
                .build());
    }

    // The exceptions will get swallowed above.
    private int checkIntConversion(final String entry) {
        final int i;
        try {
             i = Integer.parseInt(entry);
        } catch (final Exception e) {
            throw new IllegalArgumentException();
        }
        if (i > 255 || i < 0) {
            throw new IllegalArgumentException();
        }
        return i;
    }
}
