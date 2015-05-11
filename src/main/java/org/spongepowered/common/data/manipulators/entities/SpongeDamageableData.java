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
package org.spongepowered.common.data.manipulators.entities;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.entities.DamageableData;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.manipulators.SpongeAbstractData;

import java.lang.ref.WeakReference;

public class SpongeDamageableData extends SpongeAbstractData<DamageableData> implements DamageableData {

    private WeakReference<Living> lastAttacker;
    private double lastDamage;
    private int invulnTicks;
    private int maxInvulnTicks = 10;

    public SpongeDamageableData() {
        super(DamageableData.class);
    }

    @Override
    public Optional<Living> getLastAttacker() {
        return Optional.fromNullable(this.lastAttacker.get());
    }

    @Override
    public DamageableData setLastAttacker(Living lastAttacker) {
        this.lastAttacker = new WeakReference<Living>(checkNotNull(lastAttacker));
        return this;
    }

    @Override
    public Optional<Double> getLastDamage() {
        return this.lastAttacker.get() == null ? Optional.<Double>absent() : Optional.of(this.lastDamage);
    }

    @Override
    public DamageableData setLastDamage(double damage) {
        this.lastDamage = damage;
        return this;
    }

    @Override
    public int getInvulnerabilityTicks() {
        return this.invulnTicks;
    }

    @Override
    public DamageableData setInvulnerabilityTicks(int ticks) {
        if (ticks > this.maxInvulnTicks) {
            this.invulnTicks = this.maxInvulnTicks;
        } else {
            this.invulnTicks = ticks;
        }
        return this;
    }

    @Override
    public int getMaxInvulnerabilityTicks() {
        return this.maxInvulnTicks;
    }

    @Override
    public DamageableData setMaxInvulnerabilityTicks(int ticks) {
        checkArgument(ticks > 0);
        this.maxInvulnTicks = ticks;
        return this;
    }

    @Override
    public DamageableData copy() {
        final DamageableData copied = new SpongeDamageableData()
                .setMaxInvulnerabilityTicks(this.maxInvulnTicks)
                .setInvulnerabilityTicks(this.invulnTicks);
        if (this.lastAttacker.get() == null) {
            return copied;
        } else {
            return copied.setLastAttacker(this.lastAttacker.get()).setLastDamage(this.lastDamage);
        }
    }

    @Override
    public int compareTo(DamageableData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(of("LastAttacker"), this.lastAttacker.get() == null ? "none" : this.lastAttacker.get().getUniqueId())
                .set(of("LastDamage"), this.lastDamage)
                .set(of("InvulnerabilityTicks"), this.invulnTicks)
                .set(of("MaxInvulnerabilityTicks"), this.maxInvulnTicks);
    }
}
