package org.spongepowered.common.mixin.core.network.play.server;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SPacketChunkData.class)
public abstract class MixinSPacketChunkData {

    @Shadow private int chunkX;

    @Shadow private int chunkZ;

    @Redirect(method = "<init>(Lnet/minecraft/world/chunk/Chunk;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/SPacketChunkData;extractChunkData(Lnet/minecraft/network/PacketBuffer;Lnet/minecraft/world/chunk/Chunk;ZI)I"))
    private int onExtractChunkData(SPacketChunkData this$0, PacketBuffer buf, Chunk chunkIn, boolean writeSkylight, int changedSectionFilter) {
        try {
            return this$0.extractChunkData(buf, chunkIn, writeSkylight, changedSectionFilter);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Exception creating chunk packet for chunk at '%s %s'!", this.chunkX, this.chunkZ), e);
        }
    }

}
