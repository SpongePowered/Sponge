package org.spongepowered.common.event.tracking;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.CaptureType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class MoveToPhases {

    static void handleEntityDestruct(Cause cause, EntityPlayerMP player, Packet<?> packetIn, World minecraftWorld) {
        if (player != null && packetIn instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet = (C02PacketUseEntity) packetIn;
            if (packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
                net.minecraft.entity.Entity entity = packet.getEntityFromWorld(minecraftWorld);
                if (entity != null && entity.isDead && !(entity instanceof EntityLivingBase)) {
                    Player spongePlayer = (Player) player;
                    MessageChannel originalChannel = spongePlayer.getMessageChannel();

                    DestructEntityEvent event = SpongeEventFactory.createDestructEntityEvent(cause, originalChannel, Optional.of(originalChannel),
                            Optional.empty(), Optional.empty(), (Entity) entity);
                    SpongeImpl.getGame().getEventManager().post(event);
                    event.getMessage().ifPresent(text -> event.getChannel().ifPresent(channel -> channel.send(text)));

                    StaticMixinHelper.lastDestroyedEntityId = entity.getEntityId();
                }
            }
        }
    }

    static Cause handleKill(Cause cause, EntityPlayerMP player, Packet<?> packetIn) {
        if (player != null && packetIn instanceof C01PacketChatMessage) {
            C01PacketChatMessage chatPacket = (C01PacketChatMessage) packetIn;
            if (chatPacket.getMessage().contains("kill")) {
                if (!cause.contains(player)) {
                    cause = cause.with(NamedCause.of("Player", player));
                }
                StaticMixinHelper.destructItemDrop = true;
            }
        }
        return cause;
    }

    static void handleInventoryEvents(EntityPlayerMP player, Packet<?> packetIn, Container container) {
        if (player != null && player.getHealth() > 0 && container != null) {
            if (packetIn instanceof C10PacketCreativeInventoryAction && !StaticMixinHelper.ignoreCreativeInventoryPacket) {
                SpongeCommonEventFactory.handleCreativeClickInventoryEvent(Cause.of(NamedCause.source(player)), player,
                        (C10PacketCreativeInventoryAction) packetIn);
            } else {
                SpongeCommonEventFactory.handleInteractInventoryOpenCloseEvent(Cause.of(NamedCause.source(player)), player, packetIn);
                if (packetIn instanceof C0EPacketClickWindow) {
                    SpongeCommonEventFactory.handleClickInteractInventoryEvent(Cause.of(NamedCause.source(player)), player,
                            (C0EPacketClickWindow) packetIn);
                }
            }
        }
    }

    static void handleToss(@Nullable EntityPlayerMP playerMP, Packet<?> packet) {
        if (playerMP != null && packet instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging digPacket = (C07PacketPlayerDigging) packet;
            if (digPacket.getStatus() == C07PacketPlayerDigging.Action.DROP_ITEM) {
                StaticMixinHelper.destructItemDrop = false;
            }
        }
    }

    static MutablePair<BlockSnapshot, Transaction<BlockSnapshot>> handleEvents(CauseTracker causeTracker, BlockSnapshot originalBlockSnapshot, IBlockState currentState, IBlockState newState, Block block, BlockPos pos, int flags, Transaction<BlockSnapshot> transaction, LinkedHashMap<Vector3i, Transaction<BlockSnapshot>> populatorSnapshotList) {
        if (causeTracker.isCapturingTerrainGen()) {
            if (StaticMixinHelper.runningGenerator != null) {
                originalBlockSnapshot = causeTracker.getMixinWorld().createSpongeBlockSnapshot(currentState, currentState.getBlock().getActualState(currentState,
                        causeTracker.getMinecraftWorld(), pos), pos, flags);

                if (causeTracker.getCapturedPopulators().get(StaticMixinHelper.runningGenerator) == null) {
                    causeTracker.getCapturedPopulators().put(StaticMixinHelper.runningGenerator, new LinkedHashMap<>());
                }

                ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.POPULATE;
                transaction = new Transaction<>(originalBlockSnapshot, originalBlockSnapshot.withState((BlockState) newState));
                populatorSnapshotList = causeTracker.getCapturedPopulators().get(StaticMixinHelper.runningGenerator);
                populatorSnapshotList.put(transaction.getOriginal().getPosition(), transaction);
            }
        } else if (!(((IMixinMinecraftServer) MinecraftServer.getServer()).isPreparingChunks())) {
            originalBlockSnapshot = causeTracker.getMixinWorld().createSpongeBlockSnapshot(currentState, currentState.getBlock().getActualState(currentState,
                    causeTracker.getMinecraftWorld(), pos), pos, flags);

            if (StaticMixinHelper.runningGenerator != null) {
                if (causeTracker.getCapturedPopulators().get(StaticMixinHelper.runningGenerator) == null) {
                    causeTracker.getCapturedPopulators().put(StaticMixinHelper.runningGenerator, new LinkedHashMap<>());
                }

                ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.POPULATE;
                transaction = new Transaction<>(originalBlockSnapshot, originalBlockSnapshot.withState((BlockState) newState));
                populatorSnapshotList = causeTracker.getCapturedPopulators().get(StaticMixinHelper.runningGenerator);
                populatorSnapshotList.put(transaction.getOriginal().getPosition(), transaction);
            } else if (causeTracker.getPhases().peek() == BlockPhase.State.BLOCK_DECAY) {
                // Only capture final state of decay, ignore the rest
                if (block == Blocks.air) {
                    ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.DECAY;
                    causeTracker.getCapturedSpongeBlockSnapshots().add(originalBlockSnapshot);
                }
            } else if (block == Blocks.air) {
                ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.BREAK;
                causeTracker.getCapturedSpongeBlockSnapshots().add(originalBlockSnapshot);
            } else if (block != currentState.getBlock()) {
                ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.PLACE;
                causeTracker.getCapturedSpongeBlockSnapshots().add(originalBlockSnapshot);
            } else {
                ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.MODIFY;
                causeTracker.getCapturedSpongeBlockSnapshots().add(originalBlockSnapshot);
            }
        }
        return new MutablePair<>(originalBlockSnapshot, transaction);
    }

    static void handlePostPlayerBlockEvent(World minecraftWorld, @Nullable CaptureType captureType, List<Transaction<BlockSnapshot>> transactions) {
        if (StaticMixinHelper.packetPlayer == null) {
            return;
        }

        if (captureType == CaptureType.BREAK) {
            // Let the client know the blocks still exist
            for (Transaction<BlockSnapshot> transaction : transactions) {
                BlockSnapshot snapshot = transaction.getOriginal();
                BlockPos pos = VecHelper.toBlockPos(snapshot.getPosition());
                StaticMixinHelper.packetPlayer.playerNetServerHandler.sendPacket(new S23PacketBlockChange(minecraftWorld, pos));

                // Update any tile entity data for this block
                net.minecraft.tileentity.TileEntity tileentity = minecraftWorld.getTileEntity(pos);
                if (tileentity != null) {
                    Packet<?> pkt = tileentity.getDescriptionPacket();
                    if (pkt != null) {
                        StaticMixinHelper.packetPlayer.playerNetServerHandler.sendPacket(pkt);
                    }
                }
            }
        } else if (captureType == CaptureType.PLACE) {
            TrackingHelper.sendItemChangeToPlayer(StaticMixinHelper.packetPlayer);
        }
    }
}
