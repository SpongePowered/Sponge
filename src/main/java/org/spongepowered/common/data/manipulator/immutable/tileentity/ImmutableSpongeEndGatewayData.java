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
package org.spongepowered.common.data.manipulator.immutable.tileentity;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableEndGatewayData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.EndGatewayData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeEndGatewayData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public final class ImmutableSpongeEndGatewayData extends AbstractImmutableData<ImmutableEndGatewayData, EndGatewayData> implements ImmutableEndGatewayData {

    private final Vector3i exitPortal;
    private final ImmutableValue<Vector3i> exitPortalValue;
    private final boolean exactTeleport;
    private final ImmutableValue<Boolean> exactTeleportValue;
    private final long age;
    private final ImmutableValue<Long> ageValue;
    private final int teleportCooldown;
    private final ImmutableValue<Integer> teleportCooldownValue;

    public ImmutableSpongeEndGatewayData(Vector3i exitPortal, boolean exactTeleport, long age, int teleportCooldown) {
        super(ImmutableEndGatewayData.class);
        this.exitPortal = exitPortal;
        this.exitPortalValue = new ImmutableSpongeValue<>(Keys.EXIT_POSITION, Vector3i.ONE, this.exitPortal);
        this.exactTeleport = exactTeleport;
        this.exactTeleportValue = new ImmutableSpongeValue<>(Keys.EXACT_TELEPORT, false, this.exactTeleport);
        this.age = age;
        this.ageValue = new ImmutableSpongeValue<>(Keys.END_GATEWAY_AGE, 0L, this.age);
        this.teleportCooldown = teleportCooldown;
        this.teleportCooldownValue = new ImmutableSpongeValue<>(Keys.END_GATEWAY_TELEPORT_COOLDOWN, 0, this.teleportCooldown);
        registerGetters();
    }

    @Override
    protected void registerGetters() {
        this.registerKeyValue(Keys.EXIT_POSITION, this::exitPortal);
        this.registerFieldGetter(Keys.EXIT_POSITION, this::getExitPortal);
        this.registerKeyValue(Keys.EXACT_TELEPORT, this::exactTeleport);
        this.registerFieldGetter(Keys.EXACT_TELEPORT, this::getExactTeleport);
        this.registerKeyValue(Keys.END_GATEWAY_AGE, this::age);
        this.registerFieldGetter(Keys.END_GATEWAY_AGE, this::getAge);
        this.registerKeyValue(Keys.END_GATEWAY_TELEPORT_COOLDOWN, this::teleportCooldown);
        this.registerFieldGetter(Keys.END_GATEWAY_TELEPORT_COOLDOWN, this::getTeleportCooldown);
    }

    @Override
    public ImmutableValue<Vector3i> exitPortal() {
        return this.exitPortalValue;
    }

    private Vector3i getExitPortal() {
        return this.exitPortal;
    }

    @Override
    public ImmutableValue<Boolean> exactTeleport() {
        return this.exactTeleportValue;
    }

    private boolean getExactTeleport() {
        return this.exactTeleport;
    }

    @Override
    public ImmutableValue<Long> age() {
        return this.ageValue;
    }

    private long getAge() {
        return this.age;
    }

    @Override
    public ImmutableValue<Integer> teleportCooldown() {
        return this.teleportCooldownValue;
    }

    private int getTeleportCooldown() {
        return this.teleportCooldown;
    }

    @Override
    public EndGatewayData asMutable() {
        return new SpongeEndGatewayData(this.exitPortal, this.exactTeleport, this.age, this.teleportCooldown);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.EXIT_POSITION.getQuery(), this.exitPortal)
            .set(Keys.EXACT_TELEPORT.getQuery(), this.exactTeleport)
            .set(Keys.END_GATEWAY_AGE.getQuery(), this.age)
            .set(Keys.END_GATEWAY_TELEPORT_COOLDOWN.getQuery(), this.teleportCooldown);
    }

}
