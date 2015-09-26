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
package org.spongepowered.common.data.processor.value.item;

import java.util.Collections;
import java.util.Optional;

import org.spongepowered.api.GameProfile;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedPlayerData;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.SkullUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import net.minecraft.item.ItemStack;

public class ItemSkullRepresentedPlayerValueProcessor extends AbstractSpongeValueProcessor<ItemStack, GameProfile, Value<GameProfile>> {

    public ItemSkullRepresentedPlayerValueProcessor() {
        super(ItemStack.class, Keys.REPRESENTED_PLAYER);
    }

    @Override
    protected ImmutableValue<GameProfile> constructImmutableValue(GameProfile value) {
        return new ImmutableSpongeValue<GameProfile>(Keys.REPRESENTED_PLAYER, SpongeRepresentedPlayerData.NULL_PROFILE, value);
    }

    @Override
    protected Value<GameProfile> constructValue(GameProfile defaultValue) {
        return new SpongeValue<GameProfile>(Keys.REPRESENTED_PLAYER, defaultValue);
    }

    @Override
    protected Optional<GameProfile> getVal(ItemStack container) {
        return SkullUtils.getProfile(container);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (this.supports(container)) {
            final ItemStack skull = (ItemStack) container;
            final Optional<GameProfile> oldData = getVal(skull);
            if (SkullUtils.setProfile(skull, null)) {
                if (oldData.isPresent()) {
                    return DataTransactionBuilder.successReplaceResult(Collections.emptySet(),
                            Collections.singleton(constructImmutableValue(oldData.get())));
                } else {
                    return DataTransactionBuilder.successNoData();
                }
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected boolean set(ItemStack container, GameProfile value) {
        return SkullUtils.setProfile(container, value);
    }

    @Override
    protected boolean supports(ItemStack container) {
        return SkullUtils.isValidItemStack(container) && SkullUtils.getSkullType(container).equals(SkullTypes.PLAYER);
    }

}
