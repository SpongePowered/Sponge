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
package org.spongepowered.common.data.processor.value.entity;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;

import java.util.Set;

public class UserBansValueProcessor implements ValueProcessor<Set<Ban.User>, SetValue<Ban.User>> {

    @Override
    public Key<? extends BaseValue<Set<Ban.User>>> getKey() {
        return Keys.USER_BANS;
    }

    @Override
    public Optional<Set<Ban.User>> getValueFromContainer(ValueContainer<?> container) {
        if (container instanceof User) {
            return Optional.of((Set<Ban.User>) Sponge.getGame().getServiceManager().provide(BanService.class).get()
                    .getBansFor((User) container));
        }
        return Optional.absent();
    }

    @Override
    public Optional<SetValue<Ban.User>> getApiValueFromContainer(ValueContainer<?> container) {
        if (supports(container)) {
            return Optional
                    .<SetValue<Ban.User>>of(new SpongeSetValue<Ban.User>(getKey(), getValueFromContainer(container)
                            .get()));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof User;
    }

    @Override
    public DataTransactionResult transform(ValueContainer<?> container,
            Function<Set<Ban.User>, Set<Ban.User>> function) {
        return null;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, BaseValue<?> value) {
        return null;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Set<Ban.User> value) {
        return null;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return null;
    }
}
