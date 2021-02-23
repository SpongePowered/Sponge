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
package org.spongepowered.common.mixin.ipforward.server.dedicated;

import net.minecraft.server.dedicated.DedicatedServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.applaunch.config.common.IpForwardingCategory;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;

@Mixin(DedicatedServer.class)
public class DedicatedServerMixin_IpForward {

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "initServer", at = @At(value = "INVOKE_STRING", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;)V",
                                            args = "ldc=**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!"))
    private void ipForward$logEnabled(final CallbackInfoReturnable<Boolean> ci) {
        final IpForwardingCategory.Mode mode = SpongeConfigs.getCommon().get().ipForwarding.mode;
        if (mode != IpForwardingCategory.Mode.NONE) {
            DedicatedServerMixin_IpForward.LOGGER.warn("Sponge is delegating authentication to a proxy using the {} method, placing the server itself into offline mode.", mode);
            DedicatedServerMixin_IpForward.LOGGER.warn("Consult your proxy's documentation for advice on how to ensure this is configured securely.");
        }
    }

}
