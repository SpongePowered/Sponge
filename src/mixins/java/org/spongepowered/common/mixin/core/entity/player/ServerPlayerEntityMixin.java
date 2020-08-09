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
package org.spongepowered.common.mixin.core.entity.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.TicketType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.teleport.MovementTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.user.UserManager;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.bridge.world.PlatformITeleporterBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.entity.TeleportContext;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.portal.WrappedITeleporterPortalType;

import java.util.Optional;

import javax.annotation.Nullable;

// See also: SubjectMixin_API and SubjectMixin
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin implements SubjectBridge, ServerPlayerEntityBridge {

    @Shadow public ServerPlayNetHandler connection;

    @Shadow public abstract net.minecraft.world.server.ServerWorld shadow$getServerWorld();

    private final User impl$user = this.impl$getUserObjectOnConstruction();
    private @Nullable GameProfile impl$previousGameProfile;

    @Override
    public @Nullable GameProfile bridge$getPreviousGameProfile() {
        return this.impl$previousGameProfile;
    }

    @Override
    public void bridge$setPreviousGameProfile(final @Nullable GameProfile gameProfile) {
        this.impl$previousGameProfile = gameProfile;
    }

    @Override
    public String bridge$getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_USER;
    }

    private User impl$getUserObjectOnConstruction() {
        final UserManager service = SpongeCommon.getGame().getServer().getUserManager();
        if (this.impl$isFake) {
            return this.bridge$getUserObject();
        }
        // Emnsure that the game profile is up to date.
        return ((SpongeUserManager) service).forceRecreateUser((GameProfile) this.shadow$getGameProfile());
    }

    @Override
    public User bridge$getUserObject() {
        return this.impl$user;
    }

    @Override
    public User bridge$getUser() {
        return this.impl$user;
    }

    @Override
    public Optional<User> bridge$getBackingUser() {
        // may be null during initialization, mainly used to avoid potential stack overflow with #bridge$getUserObject
        return Optional.of(this.impl$user);
    }

    // TODO: this, properly.
    @Override
    public boolean bridge$isVanished() {
        return false;
    }

    @Override
    public Tristate bridge$permDefault(final String permission) {
        return Tristate.FALSE;
    }

    /*
    @Inject(method = "markPlayerActive()V", at = @At("HEAD"))
    private void impl$onPlayerActive(final CallbackInfo ci) {
        ((ServerPlayNetHandlerBridge) this.connection).bridge$resendLatestResourcePackRequest();
    }
*/

    // Used to restore original item received in a packet after canceling an event
    private ItemStack impl$packetItem = ItemStack.EMPTY;

    @Override
    public void bridge$setPacketItem(final ItemStack itemstack) {
        this.impl$packetItem = itemstack;
    }

    @Override
    public boolean bridge$setLocation(final ServerLocation location) {
        if (this.removed || ((WorldBridge) location.getWorld()).bridge$isFake()) {
            return false;
        }

        try (final TeleportContext context = EntityPhase.State.TELEPORT.createPhaseContext(PhaseTracker.SERVER)) {
            context.player().buildAndSwitch();

            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(SpongeCommon.getActivePlugin());
                frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);

                net.minecraft.world.server.ServerWorld destinationWorld = (net.minecraft.world.server.ServerWorld) location.getWorld();

                if (this.shadow$getServerWorld() != destinationWorld) {
                    final ChangeEntityWorldEvent.Pre event = SpongeEventFactory.createChangeEntityWorldEventPre(frame.getCurrentCause(),
                            (org.spongepowered.api.entity.Entity) this, (ServerWorld) this.shadow$getServerWorld(), location.getWorld(),
                            location.getWorld());
                    if (SpongeCommon.postEvent(event) && ((WorldBridge) event.getDestinationWorld()).bridge$isFake()) {
                        return false;
                    }

                    final ChangeEntityWorldEvent.Reposition repositionEvent =
                            SpongeEventFactory.createChangeEntityWorldEventReposition(frame.getCurrentCause(),
                                    (org.spongepowered.api.entity.Entity) this, (ServerWorld) this.shadow$getServerWorld(),
                                    VecHelper.toVector3d(this.shadow$getPositionVector()), location.getPosition(), event.getOriginalDestinationWorld(),
                                    location.getPosition(), event.getDestinationWorld());

                    if (SpongeCommon.postEvent(event)) {
                        return false;
                    }

                    destinationWorld = (net.minecraft.world.server.ServerWorld) event.getDestinationWorld();

                    this.posX = repositionEvent.getDestinationPosition().getX();
                    this.posY = repositionEvent.getDestinationPosition().getY();
                    this.posZ = repositionEvent.getDestinationPosition().getZ();
                } else {
                    final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(frame.getCurrentCause(),
                            (org.spongepowered.api.entity.Entity) this, VecHelper.toVector3d(this.shadow$getPositionVector()),
                            location.getPosition(), location.getPosition());
                    if (SpongeCommon.postEvent(event)) {
                        return false;
                    }

                    this.posX = event.getDestinationPosition().getX();
                    this.posY = event.getDestinationPosition().getY();
                    this.posZ = event.getDestinationPosition().getZ();
                }

                final ChunkPos chunkPos = new ChunkPos((int) this.posX >> 4, (int) this.posZ >> 4);
                destinationWorld.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkPos, 1, ((ServerPlayerEntity) (Object) this).getEntityId());
                ((ServerPlayerEntity) (Object) this).stopRiding();

                if (this.shadow$getServerWorld() != destinationWorld) {
                    EntityUtil.performPostChangePlayerWorldLogic((ServerPlayerEntity) (Object) this, this.shadow$getServerWorld(),
                            (net.minecraft.world.server.ServerWorld) location.getWorld(), destinationWorld);
                } else {
                    this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
                    this.connection.captureCurrentPosition();
                }
            }

            return true;
        }
    }

    /**
     * @author Zidane
     * @reason Call to EntityUtil to handle dimension changes
     */
    @Nullable
    @Overwrite
    public Entity changeDimension(DimensionType destination) {
        if (this.shadow$getEntityWorld().isRemote || this.removed) {
            return (ServerPlayerEntity) (Object) this;
        }

        try (final TeleportContext ignored = EntityPhase.State.TELEPORT.createPhaseContext(PhaseTracker.SERVER).player().worldChange().buildAndSwitch()) {

            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                final WrappedITeleporterPortalType portalType = new WrappedITeleporterPortalType((PlatformITeleporterBridge) this.shadow$getServer()
                        .getWorld(destination).getDefaultTeleporter(), null);

                frame.pushCause(this);
                frame.pushCause(portalType);
                frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PORTAL);

                EntityUtil.invokePortalTo((ServerPlayerEntity) (Object) this, portalType, destination);
                return (ServerPlayerEntity) (Object) this;
            }
        }
    }
}
