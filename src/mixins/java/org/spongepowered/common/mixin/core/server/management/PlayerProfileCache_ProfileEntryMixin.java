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
package org.spongepowered.common.mixin.core.server.management;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.server.management.PlayerProfileCache_ProfileEntryBridge;
import org.spongepowered.common.profile.SpongeGameProfile;

@Mixin(targets = "net.minecraft.server.management.PlayerProfileCache$ProfileEntry")
public abstract class PlayerProfileCache_ProfileEntryMixin implements PlayerProfileCache_ProfileEntryBridge {

    @Shadow @Final private com.mojang.authlib.GameProfile gameProfile;

    private boolean impl$isFull;
    private boolean impl$isSigned;

    private volatile @Nullable GameProfile impl$basic;
    private volatile @Nullable GameProfile impl$fullSigned;
    private volatile @Nullable GameProfile impl$fullUnsigned;

    @Override
    public void bridge$setIsFull(final boolean full) {
        this.impl$isFull = full;
    }

    @Override
    public void bridge$setSigned(final boolean signed) {
        this.impl$isSigned = signed;
    }

    @Override
    public void bridge$set(final GameProfile profile, final boolean full, final boolean signed) {
        if (full) {
            if (signed) {
                this.impl$fullSigned = profile;
            } else {
                this.impl$fullUnsigned = profile;
            }
        } else {
            this.impl$basic = profile;
        }
        this.impl$isSigned = signed;
        this.impl$isFull = full;
    }

    @Override
    public GameProfile bridge$getBasic() {
        GameProfile basic = this.impl$basic;
        if (basic != null) {
            return basic;
        }
        basic = this.impl$basic = SpongeGameProfile.basicOf(this.gameProfile);
        return basic;
    }

    @Override
    public @Nullable GameProfile bridge$getFull(final boolean signed) {
        if (!this.impl$isFull) {
            return null;
        }
        GameProfile full = signed ? this.impl$fullSigned : this.impl$fullUnsigned;
        if (full != null) {
            return full;
        }
        if (signed) {
            if (!this.impl$isSigned) {
                return null;
            }
            full = this.impl$fullSigned = SpongeGameProfile.of(this.gameProfile);
        } else {
            final GameProfile fullSigned = this.impl$fullSigned;
            full = this.impl$fullUnsigned = SpongeGameProfile.unsignedOf(
                    fullSigned == null ? SpongeGameProfile.of(this.gameProfile) : fullSigned);
        }
        return full;
    }
}
