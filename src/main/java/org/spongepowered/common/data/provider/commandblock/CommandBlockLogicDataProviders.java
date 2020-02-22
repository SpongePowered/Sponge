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
package org.spongepowered.common.data.provider.commandblock;

import net.minecraft.tileentity.CommandBlockLogic;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistry;
import org.spongepowered.common.data.provider.DataProviderRegistryBuilder;
import org.spongepowered.common.mixin.accessor.tileentity.CommandBlockLogicAccessor;

import java.util.function.Function;

public final class CommandBlockLogicDataProviders<T> extends DataProviderRegistryBuilder {

    private final Class<T> holderType;
    private final Function<T, CommandBlockLogic> logicProvider;

    public CommandBlockLogicDataProviders(DataProviderRegistry registry, Class<T> holderType,
            Function<T, CommandBlockLogic> logicProvider) {
        super(registry);
        this.holderType = holderType;
        this.logicProvider = logicProvider;
    }

    @Override
    public void register() {
        register(this.holderType, Keys.COMMAND,
                (accessor) -> this.logicProvider.apply(accessor).getCommand(),
                (accessor, value) -> ((CommandBlockLogicAccessor) this.logicProvider.apply(accessor)).accessor$setCommandStored(value));

        register(this.holderType, Keys.SUCCESS_COUNT,
                (accessor) -> this.logicProvider.apply(accessor).getSuccessCount(),
                (accessor, value) -> ((CommandBlockLogicAccessor) this.logicProvider.apply(accessor)).accessor$setSuccessCount(value));

        register(this.holderType, Keys.TRACKS_OUTPUT,
                (accessor) -> this.logicProvider.apply(accessor).shouldReceiveErrors(),
                (accessor, value) -> this.logicProvider.apply(accessor).setTrackOutput(value));

        register(new CommandBlockLogicLastCommandOutputProvider<>(this.holderType, this.logicProvider));
    }
}
