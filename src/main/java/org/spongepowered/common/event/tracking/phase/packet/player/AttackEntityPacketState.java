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
package org.spongepowered.common.event.tracking.phase.packet.player;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketState;
import org.spongepowered.common.item.util.ItemStackUtil;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class AttackEntityPacketState extends BasicPacketState {

    private final BiConsumer<CauseStackManager.StackFrame, BasicPacketContext>
        ATTACK_MODIFIER = super.getFrameModifier().andThen((frame, ctx) -> {
        frame.addContext(EventContextKeys.USED_ITEM, ctx.getItemUsedSnapshot());
        frame.addContext(EventContextKeys.USED_HAND, ctx.getHandUsed());
        final ServerboundInteractPacket useEntityPacket = ctx.getPacket();
        final ServerPlayer player = ctx.getPacketPlayer();
        final net.minecraft.world.entity.@Nullable Entity entity = useEntityPacket.getTarget(player.level);
        if (entity != null) {
            frame.pushCause(entity);
        }
        frame.pushCause(player);
    });

    @Override
    public BiConsumer<CauseStackManager.StackFrame, BasicPacketContext> getFrameModifier() {
        return this.ATTACK_MODIFIER;
    }

    @Override
    public boolean isPacketIgnored(final Packet<?> packetIn, final ServerPlayer packetPlayer) {
        final ServerboundInteractPacket useEntityPacket = (ServerboundInteractPacket) packetIn;
        // There are cases where a player is interacting with an entity that
        // doesn't exist on the server.
        final net.minecraft.world.entity.@Nullable Entity entity = useEntityPacket.getTarget(packetPlayer.level);
        return entity == null;
    }

    @Override
    public void populateContext(final ServerPlayer playerMP, final Packet<?> packet, final BasicPacketContext context) {
        context.itemUsed(ItemStackUtil.cloneDefensive(playerMP.getMainHandItem()))
            .handUsed(HandTypes.MAIN_HAND.get());
    }

    @Override
    public Supplier<SpawnType> getSpawnTypeForTransaction(
        final BasicPacketContext context, final net.minecraft.world.entity.Entity entityToSpawn
    ) {
        final ServerboundInteractPacket useEntityPacket = context.getPacket();
        final ServerPlayer player = context.getPacketPlayer();
        final net.minecraft.world.entity.@Nullable Entity entity = useEntityPacket.getTarget(player.level);
        if (entity != null && (entity.removed || entity instanceof LivingEntity && ((LivingEntity) entity).isDeadOrDying())) {
            return entityToSpawn instanceof ExperienceOrb ? SpawnTypes.EXPERIENCE : SpawnTypes.DROPPED_ITEM;
        }
        if (entityToSpawn instanceof ItemEntity) {
            return SpawnTypes.DROPPED_ITEM;
        }
        return super.getSpawnTypeForTransaction(context, entityToSpawn);
    }

    @Override
    public void unwind(final BasicPacketContext context) {
        if (!TrackingUtil.processBlockCaptures(context)) {
            final ServerboundInteractPacket useEntityPacket = context.getPacket();
            final ServerPlayer player = context.getPacketPlayer();
            final net.minecraft.world.entity.@Nullable Entity entity = useEntityPacket.getTarget(player.level);
            if (entity instanceof Entity) {
                ((Entity) entity).offer(Keys.NOTIFIER, player.getUUID());
            }
        };
    }

}
