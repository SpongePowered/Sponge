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
package org.spongepowered.common.command.parameter.value;

import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.impl.PatternMatchingValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedSelectorParsers;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeImpl;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class PlayerValueParameter extends PatternMatchingValueParameter implements CatalogedValueParameter {

    private final String id;
    private final String name;

    public PlayerValueParameter(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    protected Iterable<String> getChoices(Cause cause) {
        return Arrays.asList(SpongeImpl.getServer().getPlayerList().getOnlinePlayerNames());
    }

    @Override
    protected Object getValue(String choice) throws IllegalArgumentException {
        EntityPlayerMP ret = SpongeImpl.getServer().getPlayerList().getPlayerByUsername(choice);
        if (ret == null) {
            throw new IllegalArgumentException("Input value " + choice + " was not a player");
        }
        return ret;
    }

    @Override
    protected Text noChoicesError(String unformattedPattern) {
        return t("The input \"%s\" did not match any online players", unformattedPattern);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<?> parseSelector(Cause cause, String selector, CommandContext context, Function<Text, ArgumentParseException> errorFunction)
            throws ArgumentParseException {
        return CatalogedSelectorParsers.PLAYERS.parseSelector(cause, selector, context, errorFunction);
    }
}
