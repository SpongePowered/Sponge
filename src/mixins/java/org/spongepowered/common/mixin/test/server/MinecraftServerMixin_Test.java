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
package org.spongepowered.common.mixin.test.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.applaunch.test.TestGameAccess;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin_Test extends ReentrantBlockableEventLoop<TickTask> implements MinecraftServerBridge {

    // @formatter:off
    @Shadow private volatile boolean running;
    @Shadow private boolean stopped;

    @Shadow protected abstract boolean shadow$initServer() throws IOException;
    @Shadow public abstract void shadow$stopServer();
    @Shadow public abstract void shadow$tickServer(BooleanSupplier haveTime);
    // @formatter:on

    public MinecraftServerMixin_Test(final String name) {
        super(name);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(final CallbackInfo ci) {
        TestGameAccess.setup(this::impl$stop);
    }

    /**
     * @author Yeregorix
     * @reason Stay on the same thread and do not tick the server.
     */
    @Inject(method = "spin", at = @At("HEAD"), cancellable = true)
    private static void spin(final Function<Thread, ? extends MinecraftServer> supplier, CallbackInfoReturnable<MinecraftServer> cir) {
        final MinecraftServer server = supplier.apply(Thread.currentThread());
        try {
            ((MinecraftServerMixin_Test) (Object) server).shadow$initServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cir.setReturnValue(server);
    }

    private void impl$stop() {
        if (this.running) {
            this.running = false;
            this.stopped = true;
            this.shadow$stopServer();
        }
    }

    /**
     * @author Yeregorix
     * @reason Stops the server instead of the loop.
     */
    @Overwrite
    public void halt(final boolean join) {
        this.impl$stop();
    }

    /**
     * @author Yeregorix
     * @reason The server is not ticking.
     */
    @Overwrite
    public void waitUntilNextTick() {
        this.runAllTasks();
    }

    /**
     * @author Yeregorix
     * @reason We have time to run all optional tasks.
     */
    @Overwrite
    private boolean haveTime() {
        return true;
    }

    @Override
    public void bridge$tickServer(final int ticks) {
        for (int i = 0; i < ticks; i++) {
            this.shadow$tickServer(this::haveTime);
            this.waitUntilNextTick();
        }
    }
}
