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
package org.spongepowered.common.data.processor.multi.entity;

import com.google.common.collect.Maps;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableCommandData;
import org.spongepowered.api.data.manipulator.mutable.CommandData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.manipulator.mutable.SpongeCommandData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.mixin.core.tileentity.CommandBlockBaseLogicAccessor;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.item.minecart.MinecartCommandBlockEntity;
import net.minecraft.tileentity.CommandBlockLogic;

public class EntityCommandDataProcessor extends AbstractEntityDataProcessor<MinecartCommandBlockEntity, CommandData, ImmutableCommandData> {

    public EntityCommandDataProcessor() {
        super(MinecartCommandBlockEntity.class);
    }

    @Override
    public Optional<CommandData> fill(final DataContainer container, final CommandData commandData) {
        if (!container.contains(
                Keys.LAST_COMMAND_OUTPUT.getQuery(), 
                Keys.SUCCESS_COUNT.getQuery(), 
                Keys.COMMAND.getQuery(), 
                Keys.TRACKS_OUTPUT.getQuery())) {
            return Optional.empty();
        }
        final Text lastCommandOutput = Text.of(container.getString(Keys.LAST_COMMAND_OUTPUT.getQuery()).get());
        final int successCount = container.getInt(Keys.SUCCESS_COUNT.getQuery()).get();
        final String command = container.getString(Keys.COMMAND.getQuery()).get();
        final boolean tracksOutput = container.getBoolean(Keys.TRACKS_OUTPUT.getQuery()).get();
        
        commandData.set(Keys.LAST_COMMAND_OUTPUT, Optional.of(lastCommandOutput));
        commandData.set(Keys.SUCCESS_COUNT, successCount);
        commandData.set(Keys.COMMAND, command);
        commandData.set(Keys.TRACKS_OUTPUT, tracksOutput);
        return Optional.of(commandData);
    }

    @Override
    public DataTransactionResult remove(final DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean doesDataExist(final MinecartCommandBlockEntity entity) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean set(final MinecartCommandBlockEntity entity, final Map<Key<?>, Object> keyValues) {
        final CommandBlockLogic logic = entity.getCommandBlockLogic();
        logic.setLastOutput(SpongeTexts.toComponent(((Optional<Text>) keyValues.get(Keys.LAST_COMMAND_OUTPUT)).orElse(Text.of())));
        ((CommandBlockBaseLogicAccessor) logic).accessor$setCommandStored((String) keyValues.get(Keys.COMMAND));
        ((CommandBlockBaseLogicAccessor) logic).accessor$setSuccessCount((int) keyValues.get(Keys.SUCCESS_COUNT));
        logic.setTrackOutput((boolean) keyValues.get(Keys.TRACKS_OUTPUT));
        entity.tick();
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(final MinecartCommandBlockEntity entity) {
        final CommandBlockLogic logic = entity.getCommandBlockLogic();
        final Map<Key<?>, Object> values = Maps.newHashMapWithExpectedSize(4);
        final Optional<Text> lastCommandOutput = logic.getLastOutput() != null ? Optional.of(SpongeTexts.toText(logic.getLastOutput())) : Optional.empty();
        values.put(Keys.LAST_COMMAND_OUTPUT, lastCommandOutput);
        values.put(Keys.COMMAND, logic.getCommand());
        values.put(Keys.SUCCESS_COUNT, logic.getSuccessCount());
        values.put(Keys.TRACKS_OUTPUT, logic.shouldTrackOutput());
        return values;
    }

    @Override
    protected CommandData createManipulator() {
        return new SpongeCommandData();
    }

}
