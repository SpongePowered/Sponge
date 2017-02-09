package org.spongepowered.common.mixin.core.netty;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;

@Mixin(targets = "io/netty/channel/AbstractChannelHandlerContext")
public abstract class MixinAbstractChannelHandlerContext {

    @Inject(method = "invokeExceptionCaught", at = @At(value = "INVOKE", target = "Lio/netty/util/internal/logging/InternalLogger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onPrintException(Throwable cause, CallbackInfo ci, Throwable t) {
        SpongeImpl.getLogger().error("The following exception was thrown from a netty handler's exceptionCaught() method:", t);
    }
}
