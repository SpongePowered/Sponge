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

import static net.minecraft.util.EntitySelectors.field_180132_d;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.explosive.FusedExplosive;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.bridge.world.ForgeITeleporterBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.entity.EntityUtil.EntityTrace;
import org.spongepowered.common.bridge.world.TeleporterBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.WorldProviderBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.entity.InvokingTeleporterContext;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.EntityAccessor;
import org.spongepowered.common.mixin.core.entity.EntityLivingBaseAccessor;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.world.WorldManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public final class EntityUtil {

    public static final Function<PhaseContext<?>, Supplier<Optional<User>>> ENTITY_CREATOR_FUNCTION = (context) ->
        () -> Stream.<Supplier<Optional<User>>>builder()
            .add(() -> context.getSource(User.class))
            .add(context::getNotifier)
            .add(context::getOwner)
            .build()
            .map(Supplier::get)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

    private EntityUtil() {
    }

    @SuppressWarnings("Guava") private static final Predicate<Entity> TRACEABLE = Predicates.and(field_180132_d,
      entity -> entity != null && entity.func_70067_L());

    @Nullable
    public static Entity transferEntityToWorld(final Entity entity, @Nullable MoveEntityEvent.Teleport event,
        @Nullable WorldServer toWorld,  @Nullable final ForgeITeleporterBridge teleporter, final boolean recreate) {

        if (entity.field_70170_p.field_72995_K || entity.field_70128_L) {
            return null;
        }

        org.spongepowered.api.entity.Entity sEntity = (org.spongepowered.api.entity.Entity) entity;
        final EntityBridge mEntity = (EntityBridge) entity;
        final Transform<World> fromTransform = sEntity.getTransform();
        final WorldServer fromWorld = (WorldServer) fromTransform.getExtent();

        fromWorld.field_72984_F.func_76320_a("changeDimension");

        boolean loadChunks = true;

        // use the world from event
        final Transform<World> toTransform;

        // Assume portal
        if (event == null) {
            if (toWorld == null || teleporter == null) {
                return null;
            }

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
                final InvokingTeleporterContext context = createInvokingTeleporterPhase(entity, toWorld, teleporter)) {

                if (!context.getDidPort()) {
                    return entity;
                }

                frame.pushCause(context.getTeleporter());
                frame.pushCause(entity);

                // TODO All of this code assumes that all teleports outside of the API are portals...need to re-think this and somehow correctly
                // TODO determine what it truly is for API 8
                frame.addContext(EventContextKeys.TELEPORT_TYPE, TeleportTypes.PORTAL);

                event = SpongeEventFactory.createMoveEntityEventTeleportPortal(frame.getCurrentCause(), fromTransform, context.getExitTransform(),
                    context.getTeleporter(), sEntity, true, true);

                if (SpongeImpl.postEvent(event)) {
                    // Mods may cancel this event in order to run custom transfer logic
                    // We need to make sure to only rollback if they completely changed the world
                    if (event.getFromTransform().getExtent() != sEntity.getTransform().getExtent()) {
                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.func_77272_a(chunkPosition.getX(), chunkPosition.getZ()));
                        }

                        context.getCapturedBlockSupplier().restoreOriginals();

                        mEntity.bridge$setLocationAndAngles(fromTransform);
                    }

                    return null;
                } else {
                    toTransform = event.getToTransform();
                    toWorld = (WorldServer) toTransform.getExtent();

                    // Handle plugins setting changing to a different world than the teleporter said so. This is considered a cancellation
                    if (context.getExitTransform().getExtent() != toTransform.getExtent()) {
                        event.setCancelled(true);

                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.func_77272_a(chunkPosition.getX(), chunkPosition.getZ()));
                        }

                        context.getCapturedBlockSupplier().restoreOriginals();

                        mEntity.bridge$setLocationAndAngles(toTransform);
                        return null;
                    }

                    // If we don't use the portal agent clear out the portal blocks that were created
                    if (!((MoveEntityEvent.Teleport.Portal) event).getUsePortalAgent()) {
                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.func_77272_a(chunkPosition.getX(), chunkPosition.getZ()));
                        }
                        context.getCapturedBlockSupplier().restoreOriginals();
                    } else {

                        // Unwind PhaseTracker captured blocks here, the actual position placement of the entity is common code below
                        if (teleporter instanceof TeleporterBridge && !context.getCapturedBlockSupplier().isEmpty() && !TrackingUtil
                            .processBlockCaptures(context)) {
                            // Transactions were rolled back, the portal wasn't made. We need to bomb the dimension change and clear portal cache
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.func_77272_a(chunkPosition.getX(), chunkPosition.getZ()));

                            return null;
                        }
                    }

                    // If we got here, the event went completely through and the portal logic will load chunks on the other end, don't reload chunks
                    loadChunks = false;
                }
            }
            // Make sure no one else besides me is stupid enough to pass a cancelled event to this....
        } else if (event.isCancelled()) {
            return null;
        } else {
            toTransform = event.getToTransform();
            toWorld = (WorldServer) toTransform.getExtent();
        }

        fromWorld.field_72984_F.func_76318_c("reloading");
        final Entity toReturn;

        if (recreate) {
            toReturn = EntityList.func_191304_a(entity.getClass(), toWorld);
            sEntity = (org.spongepowered.api.entity.Entity) toReturn;
            if (toReturn == null) {
                return entity;
            }

            ((EntityAccessor) toReturn).accessor$CopyDataFromOldEntity(entity);
        } else {
            toReturn = entity;
        }

        if (!event.getKeepsVelocity()) {
            toReturn.field_70159_w = 0;
            toReturn.field_70181_x = 0;
            toReturn.field_70179_y = 0;
        }

        if (loadChunks) {
            final Vector3i toChunkPosition = toTransform.getLocation().getChunkPosition();
            toWorld.func_72863_F().func_186028_c(toChunkPosition.getX(), toChunkPosition.getZ());
        }

        fromWorld.field_72984_F.func_76320_a("moving");
        ((EntityBridge) toReturn).bridge$setLocationAndAngles(toTransform);
        fromWorld.field_72984_F.func_76319_b();

        try (final PhaseContext<?> ignored = EntityPhase.State.CHANGING_DIMENSION.createPhaseContext().setTargetWorld(toWorld).buildAndSwitch()) {
            if (recreate) {
                final boolean flag = toReturn.field_98038_p;
                toReturn.field_98038_p = true;
                toWorld.func_72838_d(toReturn);
                toReturn.field_98038_p = flag;
                toWorld.func_72866_a(toReturn, false);
            } else {
                toWorld.func_72838_d(toReturn);
                toWorld.func_72866_a(toReturn, false);
            }
        }

        // Fix Vanilla bug where primed minecart TNTs don't keep state through a portal
        if (toReturn instanceof EntityMinecartTNT) {
            if (((FusedExplosive) sEntity).isPrimed()) {
                toReturn.field_70170_p.func_72960_a(toReturn, (byte) 10);
            }
        }

        entity.field_70128_L = true;
        fromWorld.field_72984_F.func_76319_b();
        fromWorld.func_82742_i();
        toWorld.func_82742_i();
        fromWorld.field_72984_F.func_76319_b();

        return toReturn;
    }

    @Nullable
    public static EntityPlayerMP transferPlayerToWorld(final EntityPlayerMP player, @Nullable MoveEntityEvent.Teleport event,
        @Nullable WorldServer toWorld,  @Nullable final ForgeITeleporterBridge teleporter) {

        if (player.field_70170_p.field_72995_K || player.field_70128_L) {
            return null;
        }

        final PlayerList playerList = SpongeImpl.getServer().func_184103_al();
        final Player sPlayer = (Player) player;
        final Transform<World> fromTransform = sPlayer.getTransform();
        final WorldServer fromWorld = (WorldServer) fromTransform.getExtent();

        fromWorld.field_72984_F.func_76320_a("changeDimension");

        // use the world from event
        final Transform<World> toTransform;

        // Assume portal
        if (event == null) {
            if (toWorld == null || teleporter == null) {
                return null;
            }

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
                final InvokingTeleporterContext context = createInvokingTeleporterPhase(player, toWorld, teleporter)) {

                if (!context.getDidPort()) {
                    return null;
                }

                frame.pushCause(context.getTeleporter());
                frame.pushCause(player);

                // TODO All of this code assumes that all teleports outside of the API are portals...need to re-think this and somehow correctly
                // TODO determine what it truly is for API 8
                frame.addContext(EventContextKeys.TELEPORT_TYPE, TeleportTypes.PORTAL);

                event = SpongeEventFactory.createMoveEntityEventTeleportPortal(frame.getCurrentCause(), fromTransform, context.getExitTransform(),
                    context.getTeleporter(), sPlayer, true, true);

                if (SpongeImpl.postEvent(event)) {
                    // Mods may cancel this event in order to run custom transfer logic
                    // We need to make sure to only rollback if they completely changed the world
                    if (event.getFromTransform().getExtent() != sPlayer.getTransform().getExtent()) {
                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.func_77272_a(chunkPosition.getX(), chunkPosition.getZ()));
                        }

                        context.getCapturedBlockSupplier().restoreOriginals();

                        ((EntityBridge) player).bridge$setLocationAndAngles(fromTransform);
                    }

                    return null;
                } else {
                    toTransform = event.getToTransform();
                    toWorld = (WorldServer) toTransform.getExtent();

                    // Handle plugins setting changing to a different world than the teleporter said so. This is considered a cancellation
                    if (context.getExitTransform().getExtent() != toTransform.getExtent()) {
                        event.setCancelled(true);

                        if (teleporter instanceof TeleporterBridge) {
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.func_77272_a(chunkPosition.getX(), chunkPosition.getZ()));
                        }

                        context.getCapturedBlockSupplier().restoreOriginals();

                        ((EntityBridge) player).bridge$setLocationAndAngles(toTransform);
                        return null;
                    }

                    // If we don't use the portal agent clear out the portal blocks that
                    if (!((MoveEntityEvent.Teleport.Portal) event).getUsePortalAgent()) {
                        final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                        if (teleporter instanceof TeleporterBridge) {
                            ((TeleporterBridge) teleporter).bridge$removePortalPositionFromCache(ChunkPos.func_77272_a(chunkPosition.getX(), chunkPosition.getZ()));
                        }
                        context.getCapturedBlockSupplier().restoreOriginals();
                    } else {

                        // Unwind PhaseTracker captured blocks here, the actual position placement of the entity is common code below
                        if (teleporter instanceof TeleporterBridge && !context.getCapturedBlockSupplier().isEmpty() && !TrackingUtil
                            .processBlockCaptures(context)) {
                            // Transactions were rolled back, the portal wasn't made. We need to bomb the dimension change and clear portal cache
                            final Vector3i chunkPosition = context.getExitTransform().getLocation().getChunkPosition();
                            ((TeleporterBridge) teleporter)
                                .bridge$removePortalPositionFromCache(ChunkPos.func_77272_a(chunkPosition.getX(), chunkPosition.getZ()));

                            return null;
                        }
                    }
                }
            }
            // Make sure no one else besides me is stupid enough to pass a cancelled event to this....
        } else if (event.isCancelled()) {
            return null;
        } else {
            toTransform = event.getToTransform();
            toWorld = (WorldServer) toTransform.getExtent();
        }

        final int dimensionId;

        if (!((EntityPlayerMPBridge) player).bridge$usesCustomClient()) {

            // Check if the world we're going to matches our provider type. if so, we need to send a fake respawn packet to clear chunks
            final int fromClientDimId = WorldManager.getClientDimensionId(player, fromWorld);
            final int toClientDimId = WorldManager.getClientDimensionId(player, toWorld);

            if (fromClientDimId == toClientDimId) {
                final int fakeDimId;
                switch (fromClientDimId) {
                    case 1:
                        fakeDimId = -1;
                        break;
                    case 0:
                        fakeDimId = 1;
                        break;
                    default:
                        fakeDimId = 0;
                        break;
                }

                player.field_71135_a.func_147359_a(new SPacketRespawn(fakeDimId, toWorld.func_175659_aa(), toWorld.func_175624_G(),
                        player.field_71134_c.func_73081_b()));
            }

            dimensionId = toClientDimId;
        } else {
            // We're a custom client, their problem to handle the client provider
            WorldManager.sendDimensionRegistration(player, toWorld.field_73011_w);

            dimensionId = ((WorldServerBridge) toWorld).bridge$getDimensionId();
        }

        player.field_71135_a.func_147359_a(new SPacketRespawn(dimensionId, toWorld.func_175659_aa(), toWorld.func_175624_G(),
            player.field_71134_c.func_73081_b()));

        player.field_71093_bK = ((WorldServerBridge) toWorld).bridge$getDimensionId(); // If a Vanilla client, dimensionId could be a provider id.
        player.func_70029_a(toWorld);

        playerList.func_187243_f(player);

        fromWorld.func_72973_f(player);
        player.field_70128_L = false;

        final Vector3d position = toTransform.getPosition();
        player.func_70012_b(position.getX(), position.getY(), position.getZ(), (float) toTransform.getYaw(), (float) toTransform.getPitch());

        try (final PhaseContext<?> ignored = EntityPhase.State.CHANGING_DIMENSION.createPhaseContext().setTargetWorld(toWorld).buildAndSwitch()) {
            toWorld.func_72838_d(player);
            toWorld.func_72866_a(player, false);
        }

        // preparePlayer
        fromWorld.func_184164_w().func_72695_c(player);
        toWorld.func_184164_w().func_72683_a(player);

        final Vector3i toChunkPosition = toTransform.getLocation().getChunkPosition();
        toWorld.func_72863_F().func_186025_d(toChunkPosition.getX(), toChunkPosition.getZ());

        if (event instanceof MoveEntityEvent.Teleport.Portal) {
            CriteriaTriggers.field_193134_u.func_193143_a(player, fromWorld.field_73011_w.func_186058_p(), toWorld.field_73011_w.func_186058_p());

            if (fromWorld.field_73011_w.func_186058_p() == DimensionType.NETHER && toWorld.field_73011_w.func_186058_p() == DimensionType.OVERWORLD
                && player.func_193106_Q() != null) {
                CriteriaTriggers.field_193131_B.func_193168_a(player, player.func_193106_Q());
            }
        }
        //

        player.field_71135_a.func_147364_a(player.field_70165_t, player.field_70163_u, player.field_70161_v, player.field_70177_z, player.field_70125_A);

        player.field_71134_c.func_73080_a(toWorld);
        playerList.func_72354_b(player, toWorld);
        playerList.func_72385_f(player);

        player.field_71135_a.func_147359_a(new SPacketPlayerAbilities(player.field_71075_bZ));

        for (final PotionEffect potioneffect : player.func_70651_bq()) {
            player.field_71135_a.func_147359_a(new SPacketEntityEffect(player.func_145782_y(), potioneffect));
        }

        // Fix MC-88179: on non-death SPacketRespawn, also resend attributes
        final AttributeMap attributemap = (AttributeMap) player.func_110140_aT();
        final Collection<IAttributeInstance> watchedAttribs = attributemap.func_111160_c();
        if (!watchedAttribs.isEmpty()) {
            player.field_71135_a.func_147359_a(new SPacketEntityProperties(player.func_145782_y(), watchedAttribs));
        }

        player.field_71135_a.func_147359_a(new SPacketServerDifficulty(toWorld.func_175659_aa(), toWorld.func_72912_H().func_176123_z()));
        player.field_71135_a.func_147359_a(new SPacketEntityStatus(player, toWorld.func_82736_K().func_82766_b(DefaultGameRules.REDUCED_DEBUG_INFO) ?
            (byte) 22 : 23));

        if (!event.getKeepsVelocity()) {
            player.field_70159_w = 0;
            player.field_70181_x = 0;
            player.field_70179_y = 0;
        }

        SpongeImplHooks.handlePostChangeDimensionEvent(player, fromWorld, toWorld);

        return player;
    }

    // Teleporter code is extremely stupid
    private static InvokingTeleporterContext createInvokingTeleporterPhase(final Entity entity, WorldServer toWorld, ForgeITeleporterBridge teleporter) {
        SpongeImplHooks.registerPortalAgentType(teleporter);

        final MinecraftServer mcServer = SpongeImpl.getServer();
        final org.spongepowered.api.entity.Entity sEntity = (org.spongepowered.api.entity.Entity) entity;
        final Transform<World> fromTransform = sEntity.getTransform();
        final WorldServer fromWorld = ((WorldServer) entity.field_70170_p);

        int toDimensionId = ((WorldServerBridge) toWorld).bridge$getDimensionId();

        // Entering End Portal in End goes to Overworld in Vanilla
        if (toDimensionId == 1 && fromWorld.field_73011_w instanceof WorldProviderEnd) {
            toDimensionId = 0;
        }

        toWorld = mcServer.func_71218_a(toDimensionId);

        final Map<String, String> portalAgents =
            ((WorldInfoBridge) fromWorld.func_72912_H()).bridge$getConfigAdapter().getConfig().getWorld().getPortalAgents();
        final String worldName;

        // Check if we're to use a different teleporter for this world
        if (teleporter.getClass().getName().equals("net.minecraft.world.Teleporter")) {
            worldName = portalAgents.get("minecraft:default_" + toWorld.field_73011_w.func_186058_p().func_186065_b().toLowerCase(Locale.ENGLISH));
        } else {
            worldName = portalAgents.get("minecraft:" + teleporter.getClass().getSimpleName());
        }

        if (worldName != null) {
            for (final WorldProperties properties : Sponge.getServer().getAllWorldProperties()) {
                if (properties.getWorldName().equalsIgnoreCase(worldName)) {
                    final Optional<World> spongeWorld = Sponge.getServer().loadWorld(properties);
                    if (spongeWorld.isPresent()) {
                        toWorld = (WorldServer) spongeWorld.get();
                        teleporter = (ForgeITeleporterBridge) toWorld.func_85176_s();
                        if (teleporter instanceof TeleporterBridge) {
                            if (!((fromWorld.field_73011_w.func_177495_o() || toWorld.field_73011_w.func_177495_o()))) {
                                ((TeleporterBridge) teleporter).bridge$setNetherPortalType(false);
                            }
                        }
                    }
                }
            }
        }

        fromWorld.field_72984_F.func_76320_a("reposition");
        final Transform<World> toTransform = getPortalExitTransform(entity, fromWorld, toWorld);
        fromWorld.field_72984_F.func_76319_b();

        // Portals create blocks and the PhaseTracker is known to capture blocks..
        final InvokingTeleporterContext context = EntityPhase.State.INVOKING_TELEPORTER.createPhaseContext()
            .setTargetWorld(toWorld)
            .setTeleporter((PortalAgent) teleporter)
            .setExitTransform(toTransform)
            .buildAndSwitch();

        if (!(fromWorld.field_73011_w instanceof WorldProviderEnd)) {

            // Only place entity in portal if one of the following are true :
            // 1. The teleporter is custom. (not vanilla)
            // 2. The last known portal vec is known. (Usually set after block collision)
            // 3. The entity is traveling to end from a non-end world.
            // Note: We must always use placeInPortal to support mods.
            if (!teleporter.bridge$isVanilla() || entity.func_181014_aG() != null || toWorld.field_73011_w instanceof WorldProviderEnd) {
                // In Forge, the entity dimension is already set by this point.
                // To maintain compatibility with Forge mods, we temporarily
                // set the entity's dimension to the current target dimension
                // when calling Teleporter#bridge$placeEntity.

                Vector3d position = toTransform.getPosition();
                entity.func_70012_b(position.getX(), position.getY(), position.getZ(), (float) toTransform.getYaw(),
                    (float) toTransform.getPitch());

                fromWorld.field_72984_F.func_76320_a("placing");
                if (!teleporter.bridge$isVanilla() || toWorld.field_73011_w instanceof WorldProviderEnd) {
                    // Have to assume mod teleporters or end -> overworld always port. We set this state for nether ports in
                    // TeleporterMixin#bridge$placeEntity
                    context.setDidPort(true);
                }

                teleporter.bridge$placeEntity(toWorld, entity, (float) fromTransform.getRotation().getY());

                fromWorld.field_72984_F.func_76319_b();

                context.setExitTransform(sEntity.getTransform().setExtent((World) toWorld));

                // Roll back Entity transform
                position = fromTransform.getPosition();
                entity.func_70012_b(position.getX(), position.getY(), position.getZ(), (float) fromTransform.getYaw(),
                    (float) fromTransform.getPitch());
            }
        } else {
            context.setDidPort(true);
        }

        return context;
    }

    private static Transform<World> getPortalExitTransform(final Entity entity, final WorldServer fromWorld, final WorldServer toWorld) {
        final WorldProvider fromWorldProvider = fromWorld.field_73011_w;
        final WorldProvider toWorldProvider = toWorld.field_73011_w;

        double x;
        final double y;
        double z;

        final Transform<World> transform;

        if (toWorldProvider instanceof WorldProviderEnd) {
            final BlockPos coordinate = toWorld.func_180504_m();
            x = coordinate.func_177958_n();
            y = coordinate.func_177956_o();
            z = coordinate.func_177952_p();
        } else if (fromWorldProvider instanceof WorldProviderEnd && toWorldProvider instanceof WorldProviderSurface) {
            final BlockPos coordinate = toWorld.func_175694_M();
            x = coordinate.func_177958_n();
            y = coordinate.func_177956_o();
            z = coordinate.func_177952_p();
        }
        else {

            final double moveFactor = ((WorldProviderBridge) fromWorldProvider).bridge$getMovementFactor() / ((WorldProviderBridge) toWorldProvider).bridge$getMovementFactor();

            x = MathHelper.func_151237_a(entity.field_70165_t * moveFactor, toWorld.func_175723_af().func_177726_b() + 16.0D, toWorld.func_175723_af().func_177728_d() - 16.0D);
            y = entity.field_70163_u;
            z = MathHelper.func_151237_a(entity.field_70161_v * moveFactor, toWorld.func_175723_af().func_177736_c() + 16.0D, toWorld.func_175723_af().func_177733_e() - 16.0D);
            entity.field_70177_z = 90f;
            entity.field_70125_A = 0f;

            x = (double) MathHelper.func_76125_a((int) x, -29999872, 29999872);
            z = (double) MathHelper.func_76125_a((int) z, -29999872, 29999872);
        }

        transform = new Transform<>((World) toWorld, new Vector3d(x, y, z), new Vector3d(entity.field_70125_A, entity.field_70177_z, 0f));

        return transform;
    }

    public static boolean isEntityDead(final net.minecraft.entity.Entity entity) {
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase base = (EntityLivingBase) entity;
            return base.func_110143_aJ() <= 0 || base.field_70725_aQ > 0 || ((EntityLivingBaseAccessor) entity).accessor$isLivingDead();
        }
        return entity.field_70128_L;
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(final Entity entityIn, final Location<World> location) {
        final Transform<World> fromTransform = ((org.spongepowered.api.entity.Entity) entityIn).getTransform();
        final Transform<World> toTransform = fromTransform.setLocation(location).setRotation(new Vector3d(entityIn.field_70125_A, entityIn.field_70177_z, 0));
        return handleDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform);
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(final Entity entityIn, final double posX, final double posY, final double posZ, final float yaw, final float pitch) {
        final Transform<World> fromTransform = ((org.spongepowered.api.entity.Entity) entityIn).getTransform();
        final Transform<World> toTransform = fromTransform.setPosition(new Vector3d(posX, posY, posZ)).setRotation(new Vector3d(pitch, yaw, 0));
        return handleDisplaceEntityTeleportEvent(entityIn, fromTransform, toTransform);
    }

    public static MoveEntityEvent.Teleport handleDisplaceEntityTeleportEvent(
        final Entity entityIn, final Transform<World> fromTransform, final Transform<World> toTransform) {

        // Use origin world to get correct cause
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entityIn);

            final MoveEntityEvent.Teleport event = SpongeEventFactory.createMoveEntityEventTeleport(Sponge.getCauseStackManager().getCurrentCause(),
                fromTransform, toTransform, (org.spongepowered.api.entity.Entity) entityIn, false);
            SpongeImpl.postEvent(event);
            return event;
        }
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
        if (minecraftEntity instanceof EntityItem) {
            final ItemStack item = ((EntityItem) minecraftEntity).func_92059_d();
            if (!item.func_190926_b()) {
                final Optional<Entity> customEntityItem = Optional.ofNullable(SpongeImplHooks.getCustomEntityIfItem(minecraftEntity));
                if (customEntityItem.isPresent()) {
                    // Bypass spawning the entity item, since it is established that the custom entity is spawned.
                    final Entity entityToSpawn = customEntityItem.get();
                    supplier.get()
                        .ifPresent(spawned -> {
                            if (entityToSpawn instanceof OwnershipTrackedBridge) {
                                ((OwnershipTrackedBridge) entityToSpawn).tracked$setOwnerReference(spawned);
                            }
                        });
                    if (entityToSpawn.field_70128_L) {
                        entityToSpawn.field_70128_L = false;
                    }
                    // Since forge already has a new event thrown for the entity, we don't need to throw
                    // the event anymore as sponge plugins getting the event after forge mods will
                    // have the modified entity list for entities, so no need to re-capture the entities.
                    ((WorldServerBridge) entityToSpawn.field_70170_p).bridge$forceSpawnEntity(entityToSpawn);
                    return true;
                }
            }
        }

        supplier.get()
            .ifPresent(spawned -> {
                if (entity instanceof OwnershipTrackedBridge) {
                    ((OwnershipTrackedBridge) entity).tracked$setOwnerReference(spawned);
                }
            });
        // Allowed to call force spawn directly since we've applied creator and custom item logic already
        ((WorldServerBridge) entity.getWorld()).bridge$forceSpawnEntity((Entity) entity);
        return true;
    }

    static final class EntityTrace {

        Entity entity;
        Vec3d location;
        double distance;

        EntityTrace(final double entityDistance) {
            this.distance = entityDistance;
        }

        RayTraceResult asRayTraceResult() {
            return new RayTraceResult(this.entity, this.location);
        }
    }

    public static RayTraceResult rayTraceFromEntity(final Entity source, final double traceDistance, final float partialTicks, final boolean includeEntities) {
        final RayTraceResult blockRay = EntityUtil.rayTraceFromEntity(source, traceDistance, partialTicks);

        if (!includeEntities) {
            return blockRay;
        }

        final Vec3d traceStart = EntityUtil.getPositionEyes(source, partialTicks);
        final double blockDistance = (blockRay != null) ? blockRay.field_72307_f.func_72438_d(traceStart) : traceDistance;
        final EntityTrace trace = new EntityTrace(blockDistance);

        final Vec3d lookDir = source.func_70676_i(partialTicks).func_186678_a(traceDistance);
        final Vec3d traceEnd = traceStart.func_178787_e(lookDir);

        final AxisAlignedBB boundingBox = source.func_174813_aQ();
        final AxisAlignedBB traceBox = boundingBox.func_72321_a(lookDir.field_72450_a, lookDir.field_72448_b, lookDir.field_72449_c);
        final List<Entity> entities = source.field_70170_p.func_175674_a(source, traceBox.func_72314_b(1.0F, 1.0F, 1.0F), EntityUtil.TRACEABLE);
        for (final Entity entity : entities) {
            final AxisAlignedBB entityBB = entity.func_174813_aQ().func_186662_g(entity.func_70111_Y());
            final RayTraceResult entityRay1 = entityBB.func_72327_a(traceStart, traceEnd);

            if (entityBB.func_72318_a(traceStart)) {
                if (trace.distance >= 0.0D) {
                    trace.entity = entity;
                    trace.location = entityRay1 == null ? traceStart : entityRay1.field_72307_f;
                    trace.distance = 0.0D;
                }
                continue;
            }

            if (entityRay1 == null) {
                continue;
            }

            final double distanceToEntity = traceStart.func_72438_d(entityRay1.field_72307_f);

            if (distanceToEntity < trace.distance || trace.distance == 0.0D) {
                if (entity.func_184208_bv() == source.func_184208_bv()) {
                    if (trace.distance == 0.0D) {
                        trace.entity = entity;
                        trace.location = entityRay1.field_72307_f;
                    }
                } else {
                    trace.entity = entity;
                    trace.location = entityRay1.field_72307_f;
                    trace.distance = distanceToEntity;
                }
            }
        }

        if (trace.entity != null && (trace.distance < blockDistance || blockRay == null)) {
            return trace.asRayTraceResult();
        }

        return blockRay;
    }

    @Nullable
    public static RayTraceResult rayTraceFromEntity(final Entity source, final double traceDistance, final float partialTicks) {
        final Vec3d traceStart = EntityUtil.getPositionEyes(source, partialTicks);
        final Vec3d lookDir = source.func_70676_i(partialTicks).func_186678_a(traceDistance);
        final Vec3d traceEnd = traceStart.func_178787_e(lookDir);
        return source.field_70170_p.func_147447_a(traceStart, traceEnd, false, false, true);
    }

    private static Vec3d getPositionEyes(final Entity entity, final float partialTicks)
    {
        if (partialTicks == 1.0F)
        {
            return new Vec3d(entity.field_70165_t, entity.field_70163_u + entity.func_70047_e(), entity.field_70161_v);
        }

        final double interpX = entity.field_70169_q + (entity.field_70165_t - entity.field_70169_q) * partialTicks;
        final double interpY = entity.field_70167_r + (entity.field_70163_u - entity.field_70167_r) * partialTicks + entity.func_70047_e();
        final double interpZ = entity.field_70166_s + (entity.field_70161_v - entity.field_70166_s) * partialTicks;
        return new Vec3d(interpX, interpY, interpZ);
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
    public static EntityItem entityOnDropItem(final Entity entity, final ItemStack itemStack, final float offsetY, final double xPos, final double zPos) {
        if (itemStack.func_190926_b()) {
            // Sanity check, just like vanilla
            return null;
        }
        // Now the real fun begins.
        final ItemStack item;
        final double posX = xPos;
        final double posY = entity.field_70163_u + offsetY;
        final double posZ = zPos;

        // FIRST we want to throw the DropItemEvent.PRE
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);
        final List<ItemStackSnapshot> original = new ArrayList<>();
        original.add(snapshot);

        // Gather phase states to determine whether we're merging or capturing later
        final PhaseContext<?> phaseContext = PhaseTracker.getInstance().getCurrentContext();
        final IPhaseState<?> currentState = phaseContext.state;

        // We want to frame ourselves here, because of the two events we have to throw, first for the drop item event, then the constructentityevent.
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            // Perform the event throws first, if they return false, return null
            item = SpongeCommonEventFactory.throwDropItemAndConstructEvent(entity, posX, posY, posZ, snapshot, original, frame);

            if (item == null || item.func_190926_b()) {
                return null;
            }


            // This is where we could perform item pre merging, and cancel before we create a new entity.
            // For now, we aren't performing pre merging.

            final EntityItem entityitem = new EntityItem(entity.field_70170_p, posX, posY, posZ, item);
            entityitem.func_174869_p();

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
    private static Vector3d createDropMotion(final boolean dropAround, final EntityPlayer player, final Random random) {
        double x;
        double y;
        double z;
        if (dropAround) {
            final float f = random.nextFloat() * 0.5F;
            final float f1 = random.nextFloat() * ((float) Math.PI * 2F);
            x = -MathHelper.func_76126_a(f1) * f;
            z = MathHelper.func_76134_b(f1) * f;
            y = 0.20000000298023224D;
        } else {
            float f2 = 0.3F;
            x = -MathHelper.func_76126_a(player.field_70177_z * 0.017453292F) * MathHelper.func_76134_b(player.field_70125_A * 0.017453292F) * f2;
            z = MathHelper.func_76134_b(player.field_70177_z * 0.017453292F) * MathHelper.func_76134_b(player.field_70125_A * 0.017453292F) * f2;
            y = - MathHelper.func_76126_a(player.field_70125_A * 0.017453292F) * f2 + 0.1F;
            final float f3 = random.nextFloat() * ((float) Math.PI * 2F);
            f2 = 0.02F * random.nextFloat();
            x += Math.cos(f3) * f2;
            y += (random.nextFloat() - random.nextFloat()) * 0.1F;
            z += Math.sin(f3) * f2;
        }
        return new Vector3d(x, y, z);
    }

    @SuppressWarnings("unchecked")
    public static Optional<EntityType> fromNameToType(final String name) {
        // EntityList includes all forge mods with *unedited* entity names
        final Class<?> clazz = SpongeImplHooks.getEntityClass(new ResourceLocation(name));
        if(clazz == null) {
            return Optional.empty();
        }

        return Optional.of(EntityTypeRegistryModule.getInstance().getForClass((Class<? extends Entity>) clazz));
    }

    @SuppressWarnings("unchecked")
    public static Optional<EntityType> fromLocationToType(final ResourceLocation location) {
        final Class<?> clazz = SpongeImplHooks.getEntityClass(location);
        if (clazz == null) {
            return Optional.empty();
        }
        return Optional.of(EntityTypeRegistryModule.getInstance().getForClass((Class<? extends Entity>) clazz));
    }

    public static boolean isUntargetable(Entity from, Entity target) {
        if (((VanishableBridge) target).bridge$isVanished() && ((VanishableBridge) target).bridge$isUntargetable()) {
            return true;
        }
        // Temporary fix for https://bugs.mojang.com/browse/MC-149563
        if (from.field_70170_p != target.field_70170_p) {
            return true;
        }
        return false;
    }

}
