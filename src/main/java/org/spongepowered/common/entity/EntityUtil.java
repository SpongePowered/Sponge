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
package org.spongepowered.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.network.play.server.SPlayerAbilitiesPacket;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.accessor.entity.LivingEntityAccessor;
import org.spongepowered.common.accessor.entity.player.ServerPlayerEntityAccessor;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.world.PlatformServerWorldBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.DimensionChangeResult;
import org.spongepowered.common.world.portal.PortalHelper;
import org.spongepowered.common.world.portal.WrappedITeleporterPortalType;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class EntityUtil {

    public static final Function<PhaseContext<?>, Supplier<Optional<User>>> ENTITY_CREATOR_FUNCTION = (context) ->
        () -> Stream.<Supplier<Optional<User>>>builder()
            .add(() -> context.getSource(User.class))
            .add(context::getNotifier)
            .add(context::getCreator)
            .build()
            .map(Supplier::get)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

    private EntityUtil() {
    }

    public static DimensionChangeResult<Entity> invokePortalTo(final Entity entity, final PortalType portal, final DimensionType dimensionType) {

        ServerWorld originalToWorld = entity.getServer().getWorld(dimensionType);
        ServerWorld toWorld = originalToWorld;

        final ChangeEntityWorldEvent.Pre event = PlatformHooks.getInstance().getEventHooks().callChangeEntityWorldEventPre(entity, toWorld);
        if (event == null || event.isCancelled() || ((WorldBridge) event.getDestinationWorld()).bridge$isFake()) {
            return new DimensionChangeResult<>(entity, false, false);
        }

        toWorld = (ServerWorld) event.getDestinationWorld();

        final ServerLocation previousLocation = ((org.spongepowered.api.entity.Entity) entity).getServerLocation();
        final ServerWorld fromWorld = (ServerWorld) entity.getEntityWorld();

        Entity result = entity;
        if (portal instanceof WrappedITeleporterPortalType) {
            // Use platform teleporter hook
            result = ((WrappedITeleporterPortalType) portal).getTeleporter().bridge$placeEntity(entity, fromWorld, toWorld, entity.rotationYaw,
                    PortalHelper
                    .createVanillaEntityPortalLogic(entity, entity.getPositionVec(), fromWorld, toWorld, portal));

            if (result == null) {
                return new DimensionChangeResult<>(entity, false, true);
            }

            final ServerLocation currentLocation = ((org.spongepowered.api.entity.Entity) entity).getServerLocation();
            if (previousLocation.getWorld() == currentLocation.getWorld() && previousLocation.getBlockPosition().equals(currentLocation.getBlockPosition())) {
                return new DimensionChangeResult<>(entity, false, false);
            }
        } else if (!portal.teleport((org.spongepowered.api.entity.Entity) entity, ServerLocation.of((org.spongepowered.api.world.server.ServerWorld)
                fromWorld, VecHelper.toVector3d(entity.getPositionVector())), true)) {
            return new DimensionChangeResult<>(entity, false, false);
        }

        // Sponge Start - Call platform event hook after changing dimensions
        PlatformHooks.getInstance().getEventHooks().callChangeEntityWorldEventPost(result, fromWorld, originalToWorld);
        // Sponge End

        return new DimensionChangeResult<>(result, true, false);
    }

    public static DimensionChangeResult<ServerPlayerEntity> invokePortalTo(final ServerPlayerEntity player, final PortalType portal,
        final DimensionType dimensionType) {

        ServerWorld originalToWorld = player.getServer().getWorld(dimensionType);
        ServerWorld toWorld = originalToWorld;

        final ChangeEntityWorldEvent.Pre event = PlatformHooks.getInstance().getEventHooks().callChangeEntityWorldEventPre(player, toWorld);
        if (event == null || event.isCancelled()) {
            return new DimensionChangeResult<>(player, false, false);
        }

        toWorld = (ServerWorld) event.getDestinationWorld();

        ((ServerPlayerEntityAccessor) player).accessor$setInvulnerableDimensionChange(true);

        final ServerLocation previousLocation = ((ServerPlayer) player).getServerLocation();
        final ServerWorld fromWorld = player.getServerWorld();
        final DimensionType fromDimensionType = fromWorld.getDimension().getType();

        if (portal instanceof WrappedITeleporterPortalType) {
            // Use platform teleporter hook
            ((WrappedITeleporterPortalType) portal).getTeleporter().bridge$placeEntity(player, fromWorld, toWorld, player.rotationYaw, PortalHelper
                    .createVanillaPlayerPortalLogic(player, player.getPositionVec(), fromWorld, toWorld, portal));

            final ServerLocation currentLocation = ((ServerPlayer) player).getServerLocation();
            if (previousLocation.getWorld() == currentLocation.getWorld() && previousLocation.getBlockPosition().equals(currentLocation.getBlockPosition())) {
                return new DimensionChangeResult<>(player, false, false);
            }
        } else if (!portal.teleport((org.spongepowered.api.entity.Entity) player, ServerLocation.of((org.spongepowered.api.world.server.ServerWorld)
                fromWorld, VecHelper.toVector3d(player.getPositionVector())), true)) {
            return new DimensionChangeResult<>(player, false, false);
        }

        final boolean isVanillaPortal = portal instanceof WrappedITeleporterPortalType && ((WrappedITeleporterPortalType) portal).getTeleporter()
                .bridge$isVanilla();

        // Only show the credits if coming from Vanilla's The End to the default dimension
        if (fromDimensionType == DimensionType.THE_END && toWorld.getDimension().getType() == DimensionType.OVERWORLD && isVanillaPortal) {
            player.detach();
            player.getServerWorld().removePlayer(player);
            if (!player.queuedEndExit) {
                player.queuedEndExit = true;
                player.connection.sendPacket(new SChangeGameStatePacket(4, ((ServerPlayerEntityAccessor) player).accessor$getSeenCredits() ?
                        0.0F : 1.0F));
                ((ServerPlayerEntityAccessor) player).accessor$setSeenCredits(true);
            }

            return new DimensionChangeResult<>(player, true, false);
        }

        EntityUtil.performPostChangePlayerWorldLogic(player, fromWorld, originalToWorld, toWorld, true);

        return new DimensionChangeResult<>(player, true, false);
    }

    public static void performPostChangePlayerWorldLogic(final ServerPlayerEntity player, final ServerWorld fromWorld,
            final ServerWorld originalToWorld, final ServerWorld toWorld, final boolean isPortal) {
        // Sponge Start - Send any platform dimension data
        ((ServerPlayerEntityBridge) player).bridge$sendDimensionData(player.connection.netManager, toWorld.dimension.getType());
        // Sponge End
        WorldInfo worldinfo = toWorld.getWorldInfo();
        // We send dimension change for portals before loading chunks
        if (!isPortal) {
            // Sponge Start - Allow the platform to handle how dimension changes are sent down
            ((ServerPlayerEntityBridge) player).bridge$sendChangeDimension(toWorld.dimension.getType(), WorldInfo.byHashing(worldinfo.getSeed()),
                    worldinfo.getGenerator(), player.interactionManager.getGameType());
        }
        player.dimension = toWorld.dimension.getType();
        // Sponge End
        player.connection.sendPacket(new SServerDifficultyPacket(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
        final PlayerList playerlist = player.getServer().getPlayerList();
        playerlist.updatePermissionLevel(player);

        // Sponge Start - Have the platform handle removing the entity from the world. Move this to after the event call so
        //                that we do not remove the player from the world unless we really have teleported..
        ((PlatformServerWorldBridge) fromWorld).bridge$removeEntity(player, true);
        ((PlatformEntityBridge) player).bridge$revive();
        // Sponge End

        player.setWorld(toWorld);
        toWorld.addRespawnedPlayer(player);
        if (isPortal) {
            ((ServerPlayerEntityAccessor) player).accessor$func_213846_b(toWorld);
        }
        player.interactionManager.setWorld(toWorld);
        player.connection.sendPacket(new SPlayerAbilitiesPacket(player.abilities));
        playerlist.sendWorldInfo(player, toWorld);
        playerlist.sendInventory(player);

        for (EffectInstance effectinstance : player.getActivePotionEffects()) {
            player.connection.sendPacket(new SPlayEntityEffectPacket(player.getEntityId(), effectinstance));
        }

        if (isPortal) {
            player.connection.sendPacket(new SPlaySoundEventPacket(1032, BlockPos.ZERO, 0, false));
        }

        ((ServerWorldBridge) fromWorld).bridge$getBossBarManager().onPlayerLogout(player);
        ((ServerWorldBridge) toWorld).bridge$getBossBarManager().onPlayerLogin(player);

        ((ServerPlayerEntityAccessor) player).accessor$setLastExperience(-1);
        ((ServerPlayerEntityAccessor) player).accessor$setLastHealth(-1.0f);
        ((ServerPlayerEntityAccessor) player).accessor$setLastFoodLevel(-1);

        if (!isPortal) {
            player.connection.setPlayerLocation(player.getPosX(), player.getPosY(), player.getPosZ(), player.rotationYaw, player.rotationPitch);
            player.connection.captureCurrentPosition();
        }

        // Sponge Start - Call platform event hook after changing dimensions
        PlatformHooks.getInstance().getEventHooks().callChangeEntityWorldEventPost(player, fromWorld, originalToWorld);
        // Sponge End
    }

    public static boolean isEntityDead(final net.minecraft.entity.Entity entity) {
        if (entity instanceof LivingEntity) {
            final LivingEntity base = (LivingEntity) entity;
            return base.getHealth() <= 0 || base.deathTime > 0 || ((LivingEntityAccessor) entity).accessor$getDead();
        }
        return entity.removed;
    }

    public static boolean processEntitySpawnsFromEvent(final SpawnEntityEvent event, final Supplier<Optional<User>> entityCreatorSupplier) {
        boolean spawnedAny = false;
        for (final org.spongepowered.api.entity.Entity entity : event.getEntities()) {
            // Here is where we need to handle the custom items potentially having custom entities
            spawnedAny = processEntitySpawn(entity, entityCreatorSupplier);
        }
        return spawnedAny;
    }

    public static boolean processEntitySpawnsFromEvent(final PhaseContext<?> context, final SpawnEntityEvent destruct) {
        return processEntitySpawnsFromEvent(destruct, ENTITY_CREATOR_FUNCTION.apply(context));
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean processEntitySpawn(final org.spongepowered.api.entity.Entity entity, final Supplier<Optional<User>> supplier) {
        final Entity minecraftEntity = (Entity) entity;
        if (minecraftEntity instanceof ItemEntity) {
            final ItemStack item = ((ItemEntity) minecraftEntity).getItem();
            if (!item.isEmpty()) {
                final Optional<Entity> customEntityItem = Optional.ofNullable(SpongeImplHooks.getCustomEntityIfItem(minecraftEntity));
                if (customEntityItem.isPresent()) {
                    // Bypass spawning the entity item, since it is established that the custom entity is spawned.
                    final Entity entityToSpawn = customEntityItem.get();
                    supplier.get()
                        .ifPresent(spawned -> {
                            if (entityToSpawn instanceof CreatorTrackedBridge) {
                                ((CreatorTrackedBridge) entityToSpawn).tracked$setCreatorReference(spawned);
                            }
                        });
                    if (entityToSpawn.removed) {
                        entityToSpawn.removed = false;
                    }
                    // Since forge already has a new event thrown for the entity, we don't need to throw
                    // the event anymore as sponge plugins getting the event after forge mods will
                    // have the modified entity list for entities, so no need to re-capture the entities.
                    entityToSpawn.world.addEntity(entityToSpawn);
                    return true;
                }
            }
        }

        supplier.get()
            .ifPresent(spawned -> {
                if (entity instanceof CreatorTrackedBridge) {
                    ((CreatorTrackedBridge) entity).tracked$setCreatorReference(spawned);
                }
            });
        // Allowed to call force spawn directly since we've applied creator and custom item logic already
        ((net.minecraft.world.World) entity.getWorld()).addEntity((Entity) entity);
        return true;
    }

    /**
     * A simple redirected static util method for {@link Entity#entityDropItem(ItemStack, float)}.
     * What this does is ensures that any possibly required wrapping of captured drops is performed.
     * Likewise, it ensures that the phase state is set up appropriately.
     *
     * @param entity The entity dropping the item
     * @param itemStack The itemstack to spawn
     * @param offsetY The offset y coordinate
     * @return The item entity
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    public static ItemEntity entityOnDropItem(final Entity entity, final ItemStack itemStack, final float offsetY, final double xPos, final double zPos) {
        if (itemStack.isEmpty()) {
            // Sanity check, just like vanilla
            return null;
        }
        // Now the real fun begins.
        final ItemStack item;
        final double posX = xPos;
        final double posY = entity.getPosY() + offsetY;
        final double posZ = zPos;

        // FIRST we want to throw the DropItemEvent.PRE
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        final List<ItemStackSnapshot> original = new ArrayList<>();
        original.add(snapshot);

        // Gather phase states to determine whether we're merging or capturing later
        final PhaseContext<?> phaseContext = PhaseTracker.getInstance().getPhaseContext();
        final IPhaseState<?> currentState = phaseContext.state;

        // We want to frame ourselves here, because of the two events we have to throw, first for the drop item event, then the constructentityevent.
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            // Perform the event throws first, if they return false, return null
            item = SpongeCommonEventFactory.throwDropItemAndConstructEvent(entity, posX, posY, posZ, snapshot, original, frame);

            if (item == null || item.isEmpty()) {
                return null;
            }


            // This is where we could perform item pre merging, and cancel before we create a new entity.
            // For now, we aren't performing pre merging.

            final ItemEntity entityitem = new ItemEntity(entity.world, posX, posY, posZ, item);
            entityitem.setDefaultPickupDelay();

            // FIFTH - Capture the entity maybe?
            if (((IPhaseState) currentState).spawnItemOrCapture(phaseContext, entity, entityitem)) {
                return entityitem;
            }
            // FINALLY - Spawn the entity in the world if all else didn't fail
            EntityUtil.processEntitySpawn((org.spongepowered.api.entity.Entity) entityitem, Optional::empty);
            return entityitem;
        }
    }


    /**
     * This is used to create the "dropping" motion for items caused by players. This
     * specifically was being used (and should be the correct math) to drop from the
     * player, when we do item stack captures preventing entity items being created.
     *
     * @param dropAround True if it's being "dropped around the player like dying"
     * @param player The player to drop around from
     * @param random The random instance
     * @return The motion vector
     */
    @SuppressWarnings("unused")
    private static Vector3d createDropMotion(final boolean dropAround, final PlayerEntity player, final Random random) {
        double x;
        double y;
        double z;
        if (dropAround) {
            final float f = random.nextFloat() * 0.5F;
            final float f1 = random.nextFloat() * ((float) Math.PI * 2F);
            x = -MathHelper.sin(f1) * f;
            z = MathHelper.cos(f1) * f;
            y = 0.20000000298023224D;
        } else {
            float f2 = 0.3F;
            x = -MathHelper.sin(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2;
            z = MathHelper.cos(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2;
            y = - MathHelper.sin(player.rotationPitch * 0.017453292F) * f2 + 0.1F;
            final float f3 = random.nextFloat() * ((float) Math.PI * 2F);
            f2 = 0.02F * random.nextFloat();
            x += Math.cos(f3) * f2;
            y += (random.nextFloat() - random.nextFloat()) * 0.1F;
            z += Math.sin(f3) * f2;
        }
        return new Vector3d(x, y, z);
    }


    public static boolean isUntargetable(Entity from, Entity target) {
        if (((VanishableBridge) target).bridge$isVanished() && ((VanishableBridge) target).bridge$isUntargetable()) {
            return true;
        }
        // Temporary fix for https://bugs.mojang.com/browse/MC-149563
        if (from.world != target.world) {
            return true;
        }
        return false;
    }
}
