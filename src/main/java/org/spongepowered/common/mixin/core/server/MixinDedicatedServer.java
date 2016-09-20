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
package org.spongepowered.common.mixin.core.server;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.net.InetSocketAddress;
import java.util.Optional;

@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer extends MinecraftServer {

    @Shadow private boolean guiIsEnabled;

    public MixinDedicatedServer() {
        super(null, null, null, null, null, null, null);
    }

    public Optional<InetSocketAddress> getBoundAddress() {
        return Optional.of(new InetSocketAddress(getServerHostname(), getServerPort()));
    }

    /**
     * @author Zidane - April 20th, 2015
     * @reason At the time of writing, this turns off the default Minecraft Server GUI that exists in non-headless environment.
     * Reasoning: The GUI console can easily consume a sizable chunk of each CPU core (20% or more is common) on the computer being ran on and has
     * been proven to cause quite a bit of latency issues.
     */
    @Overwrite
    public void setGuiEnabled() {
        //MinecraftServerGui.createServerGui(this);
        this.guiIsEnabled = false;
    }

    @Inject(method = "systemExitNow", at = @At("HEAD"))
    public void postGameStoppingEvent(CallbackInfo ci) {
        SpongeImpl.postShutdownEvents();
    }

    /**
     * @author zml - March 9th, 2016
     * @author blood - July 7th, 2016 - Add cause tracker handling for throwing pre change block checks
     * @author gabizou - July 7th, 2016 - Update for 1.10's cause tracking changes
     *
     * @reason Change spawn protection to take advantage of Sponge permissions. Rather than affecting only the default world like vanilla, this
     * will apply to any world. Additionally, fire a spawn protection event
     */
    @Overwrite
    @Override
    public boolean isBlockProtected(net.minecraft.world.World worldIn, BlockPos pos, EntityPlayer playerIn) {
        final WorldServer worldServer = (WorldServer) worldIn;
        // Mods such as ComputerCraft and Thaumcraft check this method before attempting to set a blockstate.
        final CauseTracker causeTracker = ((IMixinWorldServer) worldServer).getCauseTracker();
        final PhaseData peek = causeTracker.getCurrentPhaseData();
        final IPhaseState phaseState = peek.state;
        if (phaseState == null || !phaseState.isInteraction()) {
            final Cause.Builder builder = Cause.source(playerIn);
            peek.context.getOwner().ifPresent(builder::owner);

            if (!(phaseState.getPhase().appendPreBlockProtectedCheck(builder, phaseState, peek.context, causeTracker))) {
                peek.context.getNotifier().ifPresent(builder::notifier);
            }

            Location<World> location = new Location<>((World) worldIn, pos.getX(), pos.getY(), pos.getZ());
            ChangeBlockEvent.Pre event = SpongeEventFactory.createChangeBlockEventPre(builder.build(), ImmutableList.of(location), (World) worldIn);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                return true;
            }
        }

        BlockPos spawnPoint = worldIn.getSpawnPoint();
        int protectionRadius = getSpawnProtectionSize();

        return protectionRadius > 0
               && Math.max(Math.abs(pos.getX() - spawnPoint.getX()), Math.abs(pos.getZ() - spawnPoint.getZ())) <= protectionRadius
               && !((Player) playerIn).hasPermission("minecraft.spawn-protection.override");
    }

}
