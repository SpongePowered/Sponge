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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.minecart.MinecartCommandBlockEntity;
import net.minecraft.util.text.ITextComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.tileentity.CommandBlockLogicAccessor;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;

public class MinecartCommandBlockEntityLastCommandOutputProvider extends GenericMutableDataProvider<MinecartCommandBlockEntity, Text> {

    public MinecartCommandBlockEntityLastCommandOutputProvider() {
        super(Keys.LAST_COMMAND_OUTPUT);
    }

    @Override
    protected Optional<Text> getFrom(MinecartCommandBlockEntity dataHolder) {
        @Nullable final ITextComponent component = ((CommandBlockLogicAccessor) dataHolder.getCommandBlockLogic()).accessor$getLastOutput();
        return component == null ? Optional.empty() : Optional.of(SpongeTexts.toText(component));
    }

    @Override
    protected boolean set(MinecartCommandBlockEntity dataHolder, Text value) {
        dataHolder.getCommandBlockLogic().setLastOutput(SpongeTexts.toComponent(value));
        return true;
    }

    @Override
    protected boolean removeFrom(MinecartCommandBlockEntity dataHolder) {
        dataHolder.getCommandBlockLogic().setLastOutput(null);
        return true;
    }
}
