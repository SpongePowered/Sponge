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
import com.google.common.collect.ComparisonChain;
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

    public ImmutableSpongeEndGatewayData(Vector3i exitPortal, boolean exactTeleport) {
        super(ImmutableEndGatewayData.class);
        this.exitPortal = exitPortal;
        this.exitPortalValue = new ImmutableSpongeValue<>(Keys.EXIT_PORTAL, Vector3i.ONE, this.exitPortal);
        this.exactTeleport = exactTeleport;
        this.exactTeleportValue = new ImmutableSpongeValue<>(Keys.EXACT_TELEPORT, false, this.exactTeleport);
    }

    @Override
    protected void registerGetters() {
        this.registerKeyValue(Keys.EXIT_PORTAL, this::exitPortal);
        this.registerFieldGetter(Keys.EXIT_PORTAL, this::getExitPortal);
        this.registerKeyValue(Keys.EXACT_TELEPORT, this::exactTeleport);
        this.registerFieldGetter(Keys.EXACT_TELEPORT, this::getExactTeleport);
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
    public EndGatewayData asMutable() {
        return new SpongeEndGatewayData(this.exitPortal, this.exactTeleport);
    }

    @Override
    public int compareTo(ImmutableEndGatewayData o) {
        return ComparisonChain.start()
            .compare(this.exitPortal, o.exitPortal().get())
            .compare(this.exactTeleport, o.exactTeleport().get())
            .result();
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.EXIT_PORTAL.getQuery(), this.exitPortal)
            .set(Keys.EXACT_TELEPORT.getQuery(), this.exactTeleport);
    }

}
