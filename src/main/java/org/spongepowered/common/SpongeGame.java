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
package org.spongepowered.common;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.Game;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.common.scheduler.AsyncScheduler;

import java.nio.file.Path;

public abstract class SpongeGame implements Game {

    private final AsyncScheduler asyncScheduler = new AsyncScheduler();

    @Override
    public Path getGameDirectory() {
        return SpongeCommon.getGameDir();
    }

    @Override
    public SystemSubject getSystemSubject() {
        return (SystemSubject) SpongeCommon.getServer();
    }

    @Override
    public AsyncScheduler getAsyncScheduler() {
        return this.asyncScheduler;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("platform", this.getPlatform())
                .toString();
    }

}
