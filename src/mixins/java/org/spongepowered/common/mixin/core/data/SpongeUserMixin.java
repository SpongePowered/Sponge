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
package org.spongepowered.common.mixin.core.data;

import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;

@Mixin(value = SpongeUser.class, remap = false)
public abstract class SpongeUserMixin implements DataCompoundHolder {

    @Shadow @Nullable private CompoundNBT nbt;

    @Override
    public boolean data$hasSpongeCompound() {
        if (this.nbt == null) {
            return false;
        }
        return this.nbt.contains(Constants.Forge.FORGE_DATA);
    }

    @Override
    public CompoundNBT data$getSpongeCompound() {
        if (this.nbt == null) {
            return new CompoundNBT();
        }
        CompoundNBT forgeCompound = this.nbt.getCompound(Constants.Forge.FORGE_DATA);
        if (forgeCompound == null) { // TODO this is currently never null
            forgeCompound = new CompoundNBT();
            this.nbt.put(Constants.Forge.FORGE_DATA, forgeCompound);
        }
        return forgeCompound;
    }
}
