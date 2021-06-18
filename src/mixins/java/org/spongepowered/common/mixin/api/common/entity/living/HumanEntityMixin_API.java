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
package org.spongepowered.common.mixin.api.common.entity.living;

import com.mojang.authlib.GameProfile;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.mixin.api.minecraft.world.entity.PathfinderMobMixin_API;
import java.util.Objects;
import java.util.UUID;

@Mixin(value = HumanEntity.class, remap = false)
public abstract class HumanEntityMixin_API extends PathfinderMobMixin_API implements Human {

    @Shadow private GameProfile fakeProfile;

    @Override
    public String name() {
        return this.fakeProfile.getName();
    }

    @Override
    public boolean useSkinFor(UUID minecraftAccount) {
        Objects.requireNonNull(minecraftAccount);

        return ((HumanEntity) (Object) this).getOrLoadSkin(minecraftAccount);
    }

    @Override
    public boolean useSkinFor(String minecraftAccount) {
        Objects.requireNonNull(minecraftAccount);

        return ((HumanEntity) (Object) this).getOrLoadSkin(minecraftAccount);
    }
}
