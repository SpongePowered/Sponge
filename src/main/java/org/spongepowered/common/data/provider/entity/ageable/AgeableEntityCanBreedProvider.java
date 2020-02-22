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
package org.spongepowered.common.data.provider.entity.ageable;

import net.minecraft.entity.AgeableEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.OptBool;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class AgeableEntityCanBreedProvider extends GenericMutableDataProvider<AgeableEntity, Boolean> {

    public AgeableEntityCanBreedProvider() {
        super(Keys.CAN_BREED);
    }

    @Override
    protected Optional<Boolean> getFrom(AgeableEntity dataHolder) {
        return OptBool.of(dataHolder.getGrowingAge() == 0);
    }

    @Override
    protected boolean set(AgeableEntity dataHolder, Boolean value) {
        if (dataHolder.getGrowingAge() < 0) {
            return false;
        }
        if (value) {
            dataHolder.setGrowingAge(0);
        } else {
            dataHolder.setGrowingAge(6000);
        }
        return true;
    }
}
