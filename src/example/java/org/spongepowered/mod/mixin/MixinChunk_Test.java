package org.spongepowered.common.mixin.core.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * This is an example mixin of attempting to apply changes post sponge application, in the event another
 * mod wishes to modify certain light methods and operations that Sponge would otherwise re-inject in a
 * central method (such as setBlockState). Since Sponge adds new methods, these injections must have a
 * higher priority (note the priority), and use of {@link Dynamic} is required. Likewise, since the two
 * differences are in the target methods for injections, {@link org.spongepowered.asm.mixin.injection.Group}
 * can be used to validate that at least one of the pair of injections will succeed.
 */
@Mixin(value = Chunk.class, priority = 111111)
public abstract class MixinChunk_Test {

    @Shadow public abstract void generateSkylightMap();


    @Group(name = "SetBlockGenerateSkylight", min = 1, max = 1)
    @Redirect(
        method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/Chunk;generateSkylightMap()V"
        ),
        expect = 0
    )
    private void vanillaRedirectSkymapGenerationForLightHook(Chunk chunk) {
        // fooo this is vanilla
        System.err.println("this is vanilla!");
    }



    @Group(name = "SetBlockGenerateSkylight", min = 1, max = 1)
    @Dynamic
    @Redirect(
        method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/state/IBlockState;Lorg/spongepowered/api/world/BlockChangeFlag;)Lnet/minecraft/block/state/IBlockState;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/chunk/Chunk;generateSkylightMap()V"
        ),
        remap = false,
        expect = 0
    )
    private void spongeRedirectSkymapGenerationForLightHook(Chunk chunk) {
        this.generateSkylightMap();
        System.err.print("this is sponge!!");
    }

    @Dynamic
    @ModifyVariable(
        method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/state/IBlockState;Lorg/spongepowered/api/world/BlockChangeFlag;)Lnet/minecraft/block/state/IBlockState;",
        at = @At(
            value = "LOAD",
            opcode = 0
        ),
        index = 14,
        name = "requiresNewLightCalculations",
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lorg/spongepowered/common/event/tracking/context/MultiBlockCaptureSupplier;prune(Lorg/spongepowered/api/block/BlockSnapshot;)V"
            ),
            to = @At(
                value = "INVOKE",
                target = "Lorg/spongepowered/common/SpongeImplHooks;getBlockLightOpacity(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)I",
                ordinal = 1

            )
        ),
        print = true
    )
    private boolean redirectIfNotEquals(boolean generateSkylight) {
        return false;
    }

}
