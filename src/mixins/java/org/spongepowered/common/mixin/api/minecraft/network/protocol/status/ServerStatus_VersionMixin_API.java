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
package org.spongepowered.common.mixin.api.minecraft.network.protocol.status;

import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.ProtocolMinecraftVersion;
import org.spongepowered.common.SpongeMinecraftVersion;

import java.util.OptionalInt;
import net.minecraft.network.protocol.status.ServerStatus;

@Mixin(ServerStatus.Version.class)
public abstract class ServerStatus_VersionMixin_API implements MinecraftVersion, ProtocolMinecraftVersion {

    // @formatter:off
    @Shadow @Final private String name;
    @Shadow @Final private int protocol;
    // @formatter:on

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public boolean isLegacy() {
        return false;
    }

    @Override
    public OptionalInt dataVersion() {
        return OptionalInt.empty();
    }

    @Override
    public int getProtocol() {
        return this.protocol;
    }

    @Override
    public int compareTo(final MinecraftVersion o) {
        return SpongeMinecraftVersion.compare(this, o);
    }
}
