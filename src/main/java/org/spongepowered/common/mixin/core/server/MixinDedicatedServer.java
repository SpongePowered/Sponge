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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.PropertyManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.net.InetSocketAddress;
import java.util.Optional;

@Mixin(DedicatedServer.class)
public abstract class MixinDedicatedServer extends MinecraftServer implements Server {

    @Shadow public abstract String getHostname();
    @Shadow public abstract int getPort();

    @Shadow private boolean guiIsEnabled;

    public MixinDedicatedServer() {
        super(null, null, null, null, null, null, null);
    }

    @Override
    public Optional<InetSocketAddress> getBoundAddress() {
        //noinspection ConstantConditions
        if (this.getHostname() == null) {
            return Optional.empty();
        }
        return Optional.of(new InetSocketAddress(this.getHostname(), this.getPort()));
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
    private void postGameStoppingEvent(CallbackInfo ci) {
        SpongeImpl.postShutdownEvents();
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/PropertyManager;getIntProperty(Ljava/lang/String;I)I"))
    private int fixWrongDefaultDifficulty(PropertyManager propertyManager, String key, int defaultValue) {
        if ("difficulty".equalsIgnoreCase(key)) {
            return propertyManager.getIntProperty(key, WorldInfo.DEFAULT_DIFFICULTY.getDifficultyId());
        }

        return propertyManager.getIntProperty(key, defaultValue);
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
        // Mods such as ComputerCraft and Thaumcraft check this method before attempting to set a blockstate.
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final PhaseData peek = phaseTracker.getCurrentPhaseData();
        final IPhaseState<?> phaseState = peek.state;
        if (!phaseState.isInteraction()) {
            // TODO BLOCK_PROTECTED flag
            if (SpongeCommonEventFactory.callChangeBlockEventPre((IMixinWorldServer) worldIn, pos, playerIn).isCancelled()) {
                return true;
            }
        }

        BlockPos spawnPoint = worldIn.getSpawnPoint();
        int protectionRadius = this.getSpawnProtectionSize();

        return protectionRadius > 0
               && Math.max(Math.abs(pos.getX() - spawnPoint.getX()), Math.abs(pos.getZ() - spawnPoint.getZ())) <= protectionRadius
               && !((Player) playerIn).hasPermission("minecraft.spawn-protection.override");
    }

}
