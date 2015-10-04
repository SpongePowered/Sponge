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
package org.spongepowered.common.mixin.core.entity.explosive;

import static com.google.common.base.Preconditions.checkArgument;
import static org.spongepowered.api.data.DataQuery.of;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.core.entity.MixinEntity;

import java.util.Optional;

@NonnullByDefault
@Mixin(EntityTNTPrimed.class)
public abstract class MixinEntityTNTPrimed extends MixinEntity implements PrimedTNT {

    @Shadow private int fuse;
    @Shadow private EntityLivingBase tntPlacedBy;
    @Shadow public abstract void explode();


    private void setFuse(int fuse) {
        checkArgument(fuse >= 0);
        this.fuse = fuse;
    }

    @Override
    public boolean validateRawData(DataContainer container) {
        boolean doesSuper = super.validateRawData(container);
        return doesSuper && container.contains(of("Fuse"));
    }

    @Override
    public void setRawData(DataContainer container) throws InvalidDataException {
        super.setRawData(container);
        try {
            setFuse(container.getInt(of("Fuse")).get());
        } catch (Exception e) {
            throw new InvalidDataException("Couldn't parse raw data", e);
        }
    }

    @Override
    public void detonate() {
        this.setDead();
        this.explode();
    }

    @Override
    public Optional<Living> getDetonator() {
        return Optional.ofNullable((Living) this.tntPlacedBy);
    }
}
