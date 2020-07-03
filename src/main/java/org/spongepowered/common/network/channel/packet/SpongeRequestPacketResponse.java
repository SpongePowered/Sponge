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
package org.spongepowered.common.network.channel.packet;

import org.spongepowered.api.network.channel.ChannelException;
import org.spongepowered.api.network.channel.packet.Packet;
import org.spongepowered.api.network.channel.packet.RequestPacketResponse;

import java.util.Objects;

public abstract class SpongeRequestPacketResponse<R extends Packet> implements RequestPacketResponse<R> {

    private boolean completed;

    private void checkCompleted() {
        if (this.completed) {
            throw new ChannelException("The request response was already completed.");
        }
        this.completed = true;
    }

    @Override
    public void fail(final ChannelException exception) {
        Objects.requireNonNull(exception, "exception");
        this.checkCompleted();
        this.fail0(exception);
    }

    protected abstract void fail0(ChannelException exception);

    @Override
    public void success(final R response) {
        Objects.requireNonNull(response, "response");
        this.checkCompleted();
        this.success0(response);
    }

    protected abstract void success0(R response);
}
