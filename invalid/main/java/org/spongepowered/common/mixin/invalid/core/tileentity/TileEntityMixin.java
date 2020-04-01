package org.spongepowered.common.mixin.invalid.core.tileentity;

import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(TileEntity.class)
public class TileEntityMixin {


    @SuppressWarnings({"rawtypes"})
    @Inject(method = "register(Ljava/lang/String;Ljava/lang/Class;)V", at = @At(value = "RETURN"))
    private static void impl$registerTileEntityClassWithSpongeRegistry(final String name, @Nullable final Class clazz, final CallbackInfo callbackInfo) {
        if (clazz != null) {
            TileEntityTypeRegistryModule.getInstance().doTileEntityRegistration(clazz, name);
        }
    }

    @Inject(method = "invalidate", at = @At("RETURN"))
    private void impl$RemoveActiveChunkOnInvalidate(final CallbackInfo ci) {
        ((ActiveChunkReferantBridge) this).bridge$setActiveChunk(null);
    }

    /**
     * Hooks into vanilla's writeToNBT to call {@link #bridge$writeToSpongeCompound}.
     * <p>
     * <p> This makes it easier for other entity mixins to override writeToNBT without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla writes to (unused because we write to SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;", at = @At("HEAD"))
    private void impl$WriteSpongeDataToCompound(final CompoundNBT compound, final CallbackInfoReturnable<CompoundNBT> ci) {
        if (!((CustomDataHolderBridge) this).bridge$getCustomManipulators().isEmpty()) {
            this.bridge$writeToSpongeCompound(this.data$getSpongeDataCompound());
        }
    }

    /**
     * Hooks into vanilla's readFromNBT to call {@link #bridge$readFromSpongeCompound}.
     * <p>
     * <p> This makes it easier for other entity mixins to override readSpongeNBT without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla reads from (unused because we read from SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void impl$ReadSpongeDataFromCompound(final CompoundNBT compound, final CallbackInfo ci) {
        if (this.data$hasSpongeCompound()) {
            this.bridge$readFromSpongeCompound(this.data$getSpongeDataCompound());
        }
    }
}
