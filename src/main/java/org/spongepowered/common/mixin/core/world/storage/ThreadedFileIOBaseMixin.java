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
package org.spongepowered.common.mixin.core.world.storage;

import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;

import java.util.List;

@Mixin(ThreadedFileIOBase.class)
public abstract class ThreadedFileIOBaseMixin {

    @Shadow @Final private List<IThreadedFileIO> threadedIOQueue;
    @Shadow private volatile long savedIOCounter;
    @Shadow private volatile boolean isThreadWaiting;

    @Redirect(method = "processQueue", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;sleep(J)V", ordinal = 0))
    public void onProcessQueueThreadSleep(long millis) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getGeneral().getFileIOThreadSleep()) {
            return;
        }

        try {
            Thread.sleep(this.isThreadWaiting ? 0L : 2L); // changed from 10L to 2L
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
