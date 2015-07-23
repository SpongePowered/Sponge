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
package org.spongepowered.common.mixin.core.ban;

import net.minecraft.server.management.BanEntry;
import net.minecraft.server.management.IPBanEntry;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.asm.mixin.Mixin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

@Mixin(IPBanEntry.class)
public abstract class MixinIPBanEntry extends BanEntry implements Ban.Ip {

    public MixinIPBanEntry(Object p_i46334_1_, Date startDate, String banner, Date endDate, String banReason) {
        super(p_i46334_1_, startDate, banner, endDate, banReason);
    }

    @Override
    public InetAddress getAddress() {
        try {
            return InetAddress.getByName((String) this.value);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Error parsing ban address!", e);
        }
    }
}
