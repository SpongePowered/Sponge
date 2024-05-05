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
package org.spongepowered.common.raid;


import org.spongepowered.api.entity.living.monster.raider.Raider;
import org.spongepowered.api.raid.Raid;
import org.spongepowered.api.raid.RaidWave;
import org.spongepowered.common.accessor.world.entity.raid.RaidAccessor;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public final class SpongeRaidWave implements RaidWave {

    private final net.minecraft.world.entity.raid.Raid raid;
    private final int waveId;

    public SpongeRaidWave(final net.minecraft.world.entity.raid.Raid raid, final int waveId) {
        this.raid = raid;
        this.waveId = waveId;
    }

    @Override
    public Raid raid() {
        return (Raid) this.raid;
    }

    @Override
    public boolean isBonus() {
        /* i509VCB: For clarification bonus waves occur when the Bad Omen level is greater than 1.
         * A Wave cannot be final and bonus at the same time, so we check if the wave is final and work from there.
         * If the wave is not a final wave, the bonus waves occur after the final wave.
         * If our wave was before the final wave, the amount of normal raids (which is set by the difficulty) would be greater than our wave's id.
         */
        if (this.isFinal() || this.waveId < ((RaidAccessor) this.raid).accessor$numGroups()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isFinal() {
        // The final wave is the last wave before any other extra waves caused by a level of Bad Omen which is higher than 1.
        // We can determine if our wave is the final wave if the amount of normal waves (discussed up in the detail comments for isBonus) is equal to our wave's id.
        return this.waveId == ((RaidAccessor) this.raid).accessor$numGroups();
    }

    @Override
    public Optional<Raider> leader() {
        return Optional.ofNullable((Raider) this.raid.getLeader(this.waveId));
    }

    @Override
    public boolean addRaider(Raider raider, boolean addToRaidHealth) {
        Objects.requireNonNull(raider, "Raider cannot be null.");
        return this.raid.addWaveMob(this.waveId, (net.minecraft.world.entity.raid.Raider) raider, addToRaidHealth);
    }

    @Override
    public boolean removeRaider(Raider raider) {
        Objects.requireNonNull(raider, "Raider cannot be null.");
        if (raider.raidWave().isPresent() && this.equals(raider.raidWave().get().get())) {
            this.raid.removeFromRaid((net.minecraft.world.entity.raid.Raider) raider, true);
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpongeRaidWave) {
            SpongeRaidWave other = (SpongeRaidWave) obj;
            // Minecraft Tracks it's raids via an ID which is handled by the RaidManager.
            // Each world has it's own raid manager so we have to verify that the world the raids are in is also equal.
            if (this.waveId == other.waveId && this.raid.getLevel() == other.raid.getLevel() && this.raid.getId() == other.raid.getId()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeRaidWave.class.getSimpleName() + "[", "]")
                .add("raid=" + this.raid)
                .add("wave=" + this.waveId)
                .toString();
    }
}
