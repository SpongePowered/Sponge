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
package org.spongepowered.common.command.managed.childexception;

import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;

public class ChildCommandException extends CommandException {

    private static final long serialVersionUID = 4337314976786747479L;
    private final static Text LINE = Text.of(TextColors.RED, "----------------------");

    private final String command; // Null for a root command.
    private final CommandException primary;
    @Nullable private final ChildCommandException secondary;

    public ChildCommandException(String command, CommandException primary, @Nullable ChildCommandException secondary) {
        super(primary.getText() == null ? Text.of("Unknown error") : primary.getText(), primary.getCause(), primary.shouldIncludeUsage());
        this.command = command;
        this.primary = primary;
        this.secondary = secondary;
    }

    public String getCommand() {
        StringBuilder sb = new StringBuilder(this.command);
        if (this.secondary != null) {
            sb.append(" ").append(this.secondary.getCommand());
        }

        return sb.toString();
    }

    @Nullable
    @Override
    public Text getText() {
        Text.Builder builder = Text.builder();
        if (this.secondary != null) {
            builder.append(this.secondary.getText());
        }

        return builder.append(Text.NEW_LINE)
                .append(LINE)
                .append(Text.NEW_LINE)
                .append(Text.of(TextColors.RED, t("Exception from "), TextColors.YELLOW, "/", getCommand()))
                .append(Text.NEW_LINE)
                .append(this.primary.getText())
                .build();
    }
}
