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

import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.command.parameter.ArgumentParseException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.parameter.token.CommandArgs;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class DataContainerValueParameter implements CatalogedValueParameter {

    @Override
    public List<String> complete(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        return Lists.newArrayList();
    }

    @Override
    public String getId() {
        return "sponge:data_container";
    }

    @Override
    public String getName() {
        return "Data Container parameter";
    }

    @Override
    public Optional<?> getValue(Cause cause, CommandArgs args, CommandContext context) throws ArgumentParseException {
        String argument = args.rawArgsFromCurrentPosition();
        Callable<BufferedReader> reader = () -> new BufferedReader(new StringReader(argument));

        ConfigurationLoader<? extends ConfigurationNode> loader = HoconConfigurationLoader.builder()
                .setSource(reader).build();
        ConfigurationNode node;
        try {
            node = loader.load();
        } catch (IOException ex) {
            // Not HOCON. Try JSON?
            loader = GsonConfigurationLoader.builder().setSource(reader).build();
            try {
                node = loader.load();
            } catch (IOException e) {
                throw args.createError(Text.of("Node parsing failed: ", ex.getMessage()));
            }
        }

        while (args.hasNext()) {
            args.next();
        }

        return Optional.of(DataTranslators.CONFIGURATION_NODE.translate(node));
    }
}
