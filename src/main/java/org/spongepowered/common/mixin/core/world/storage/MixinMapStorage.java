package org.spongepowered.common.mixin.core.world.storage;

import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;

@Mixin(MapStorage.class)
public abstract class MixinMapStorage {

    @Shadow @Final private ISaveHandler saveHandler;

    // When we have a null saveHandler, we're not in the 'real' Overworld
    // MapStorage

    @Inject(method = "getOrLoadData", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    public void onAddToLoadedData(CallbackInfoReturnable<WorldSavedData> ci) {
        if (this.saveHandler != null && !SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            SpongeImpl.getLogger().error("Attempted to call MapStorage#getOrLoadData from off the main thread!", new Exception("Dummy exception"));
        }
    }

    @Inject(method = "setData", at = @At(value = "HEAD"))
    public void onSetData(CallbackInfo ci) {
        if (this.saveHandler != null && !SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            SpongeImpl.getLogger().error("Attempted to call MapStorage#setData from off the main thread!", new Exception("Dummy exception"));
        }
    }

}
