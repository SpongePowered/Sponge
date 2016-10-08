package org.spongepowered.common.mixin.core.block;

import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.service.permission.SpongePermissionService;

import javax.annotation.Nullable;

@Mixin(BlockCommandBlock.class)
public abstract class MixinBlockCommandBlock {

    @Inject(method = "onBlockActivated", at = @At(value = "RETURN", ordinal = 1))
    public void onOpenCommandBlockFailed(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        // In Vanilla, the command block will never even open, since the client will do the permission check.
        // However, when a plugin provides a permission service, we have to force the op level to 0 on the client, since the server can't tell the client to open it.
        // If the server-side permission check fails, we need to forcibly close it, since it will already have opened on the client.
        if (!worldIn.isRemote && !(Sponge.getServiceManager().provideUnchecked(PermissionService.class) instanceof SpongePermissionService)) {
            // CommandBlock GUI opens solely on the client, we need to force it close on cancellation
            ((EntityPlayerMP) playerIn).connection.sendPacket(new SPacketCloseWindow(0));
        }
    }

}
