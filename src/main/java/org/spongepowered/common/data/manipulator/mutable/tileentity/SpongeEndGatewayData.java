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
package org.spongepowered.common.data.manipulator.mutable.tileentity;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableEndGatewayData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.EndGatewayData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeEndGatewayData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public final class SpongeEndGatewayData extends AbstractData<EndGatewayData, ImmutableEndGatewayData> implements EndGatewayData {

    private Vector3i exitPortal;
    private boolean exactTeleport;
    private long age;
    private int teleportCooldown;

    public SpongeEndGatewayData() {
        this(Vector3i.ONE, false, 0L, 0);
    }

    public SpongeEndGatewayData(Vector3i exitPortal, boolean exactTeleport, long age, int teleportCooldown) {
        super(EndGatewayData.class);
        this.exitPortal = exitPortal;
        this.exactTeleport = exactTeleport;
        this.age = age;
        this.teleportCooldown = teleportCooldown;
        this.registerGettersAndSetters();
    }

    @Override
    protected void registerGettersAndSetters() {
        this.registerKeyValue(Keys.EXIT_POSITION, this::exitPosition);
        this.registerFieldGetter(Keys.EXIT_POSITION, this::getExitPortal);
        this.registerFieldSetter(Keys.EXIT_POSITION, this::setExitPortal);
        this.registerKeyValue(Keys.EXACT_TELEPORT, this::exactTeleport);
        this.registerFieldGetter(Keys.EXACT_TELEPORT, this::getExactTeleport);
        this.registerFieldSetter(Keys.EXACT_TELEPORT, this::setExactTeleport);
        this.registerKeyValue(Keys.END_GATEWAY_AGE, this::age);
        this.registerFieldGetter(Keys.END_GATEWAY_AGE, this::getAge);
        this.registerFieldSetter(Keys.END_GATEWAY_AGE, this::setAge);
        this.registerKeyValue(Keys.END_GATEWAY_TELEPORT_COOLDOWN, this::teleportCooldown);
        this.registerFieldGetter(Keys.END_GATEWAY_TELEPORT_COOLDOWN, this::getTeleportCooldown);
        this.registerFieldSetter(Keys.END_GATEWAY_TELEPORT_COOLDOWN, this::setTeleportCooldown);
    }

    @Override
    public Value<Vector3i> exitPosition() {
        return new SpongeValue<>(Keys.EXIT_POSITION, Vector3i.ONE, this.exitPortal);
    }

    private Vector3i getExitPortal() {
        return this.exitPortal;
    }

    private void setExitPortal(Vector3i exitPortal) {
        this.exitPortal = exitPortal;
    }

    @Override
    public Value<Boolean> exactTeleport() {
        return new SpongeValue<>(Keys.EXACT_TELEPORT, false, this.exactTeleport);
    }

    private boolean getExactTeleport() {
        return this.exactTeleport;
    }

    private void setExactTeleport(boolean exactTeleport) {
        this.exactTeleport = exactTeleport;
    }

    @Override
    public Value<Long> age() {
        return new SpongeValue<>(Keys.END_GATEWAY_AGE, 0L, this.age);
    }

    private long getAge() {
        return this.age;
    }

    private void setAge(long age) {
        this.age = age;
    }

    @Override
    public Value<Integer> teleportCooldown() {
        return new SpongeValue<>(Keys.END_GATEWAY_TELEPORT_COOLDOWN, 0, this.teleportCooldown);
    }

    private int getTeleportCooldown() {
        return this.teleportCooldown;
    }

    private void setTeleportCooldown(int teleportCooldown) {
        this.teleportCooldown = teleportCooldown;
    }

    @Override
    public EndGatewayData copy() {
        return new SpongeEndGatewayData(this.exitPortal, this.exactTeleport, this.age, this.teleportCooldown);
    }

    @Override
    public ImmutableEndGatewayData asImmutable() {
        return new ImmutableSpongeEndGatewayData(this.exitPortal, this.exactTeleport, this.age, this.teleportCooldown);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.EXIT_POSITION.getQuery(), this.exitPortal)
            .set(Keys.EXACT_TELEPORT.getQuery(), this.exactTeleport)
            .set(Keys.END_GATEWAY_TELEPORT_COOLDOWN.getQuery(), this.teleportCooldown);
    }

}
