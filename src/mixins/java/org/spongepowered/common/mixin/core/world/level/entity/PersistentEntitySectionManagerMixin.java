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
package org.spongepowered.common.mixin.core.world.level.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.math.vector.Vector3d;

@Mixin(PersistentEntitySectionManager.class)
public class PersistentEntitySectionManagerMixin {

    @Inject(
        method = "addEntity(Lnet/minecraft/world/level/entity/EntityAccess;Z)Z",
        at = @At("HEAD")
    )
    private void impl$throwEntityConstructedEvent(
        final EntityAccess entity, final boolean callCreateCallback,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        if (!(entity instanceof final EntityBridge bridge)) {
            return;
        }
        if (!bridge.bridge$isConstructing()) {
            return;
        }
        bridge.bridge$fireConstructors();
        if (!(entity instanceof final Entity mcEntity)) {
            return;
        }
        if (!(mcEntity.level() instanceof final ServerWorld sw)) {
            return;
        }

        final Vec3 position = mcEntity.position();
        final ServerLocation location = ServerLocation.of(sw, position.x(), position.y(), position.z());
        final Vec2 rotationVector = mcEntity.getRotationVector();
        final Vector3d rotation = new Vector3d(rotationVector.x, rotationVector.y, 0);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.SERVER.pushCauseFrame()) {
            frame.pushCause(entity);
            final Event construct = SpongeEventFactory.createConstructEntityEventPost(frame.currentCause(), (org.spongepowered.api.entity.Entity) entity, location, rotation,
                ((EntityType<?>) mcEntity.getType()));
            SpongeCommon.post(construct);
        }
    }
}
