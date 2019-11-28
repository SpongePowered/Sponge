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
package org.spongepowered.common.event.tracking.phase.general;

import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.core.world.ExplosionAccessor;

import javax.annotation.Nullable;

public final class ExplosionContext extends GeneralPhaseContext<ExplosionContext> {

    private Explosion explosion;

    public ExplosionContext() {
        super(GeneralPhase.State.EXPLOSION);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    ExplosionContext populateFromCurrentState() {
        final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();
        ((IPhaseState) context.state).appendContextPreExplosion(this, context);
        return this;
    }

    public ExplosionContext potentialExplosionSource(final ServerWorld worldServer, @Nullable final Entity entityIn) {
        if (entityIn != null) {
            this.source(entityIn);
        } else {
            this.source(worldServer);
        }
        return this;
    }
    
    public ExplosionContext explosion(final Explosion explosion) {
        this.explosion = explosion;
        return this;
    }
    
    public Explosion getExplosion() {
        return this.explosion;
    }

    public org.spongepowered.api.world.explosion.Explosion getSpongeExplosion() {
        return (org.spongepowered.api.world.explosion.Explosion) this.explosion;
    }

    @Override
    public PrettyPrinter printCustom(final PrettyPrinter printer, final int indent) {
        final String s = String.format("%1$" + indent + "s", "");
        return super.printCustom(printer, indent)
            .add(s + "- %s: %s", "Explosion", this.explosion);
    }

    @Override
    protected void reset() {
        super.reset();
        this.explosion = null;
    }

    @Override
    protected boolean isRunaway(final PhaseContext<?> phaseContext) {
        if (phaseContext.getClass() != ExplosionContext.class) {
            return false;
        }
        final ExplosionAccessor otherExplosion = (ExplosionAccessor) ((ExplosionContext) phaseContext).explosion;
        final ExplosionAccessor thisExplosion = (ExplosionAccessor) this.explosion;

        return otherExplosion.accessor$getWorld() == thisExplosion.accessor$getWorld()
               && otherExplosion.accessor$getX() == thisExplosion.accessor$getX()
               && otherExplosion.accessor$getY() == thisExplosion.accessor$getY()
               && otherExplosion.accessor$getZ() == thisExplosion.accessor$getZ();
    }
}
