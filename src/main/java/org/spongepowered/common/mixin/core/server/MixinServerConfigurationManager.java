package org.spongepowered.common.mixin.core.server;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;
import java.util.UUID;

@Mixin(ServerConfigurationManager.class)
public class MixinServerConfigurationManager {

    @Shadow public Map<UUID, EntityPlayerMP> uuidToPlayerMap;

    private Scoreboard scoreboard;

    @Inject(method = "recreatePlayerEntity", at = @At("HEAD"))
    public void onRecreatePlayerEntity(EntityPlayerMP playerIn, int dimension, boolean conqueredEnd, CallbackInfoReturnable<EntityPlayerMP> cir) {
        this.scoreboard = ((Player) playerIn).getScoreboard();
    }

    @Inject(method = "recreatePlayerEntity", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onRecreatePlayerEntityReturn(EntityPlayerMP playerIn, int dimension, boolean conqueredEnd, CallbackInfoReturnable<EntityPlayerMP> cir, World world, BlockPos blockpos, boolean flag1, Object object, EntityPlayerMP entityplayermp1, WorldServer worldserver, BlockPos blockpos1 ) {
        ((Player) entityplayermp1).setScoreboard(this.scoreboard);
        this.scoreboard = null;
    }

    @Surrogate
    public void onRecreatePlayerEntityReturn(EntityPlayerMP playerIn, int dimension, boolean conqueredEnd, CallbackInfoReturnable<EntityPlayerMP> cir) {
        ((Player) this.uuidToPlayerMap.get(playerIn.getUniqueID())).setScoreboard(this.scoreboard);
        this.scoreboard = null;
    }

}
