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
package org.spongepowered.common.world.portal;

import net.minecraft.world.level.block.Portal;
import org.spongepowered.api.world.portal.PortalLogic;

import java.util.ArrayList;
import java.util.List;

public final class SpongePortalLogicBuilder implements PortalLogic.Builder {

    private List<Portal> rules = new ArrayList<>();


    @Override
    public PortalLogic.Builder reset() {
        this.rules.clear();
        return this;
    }


    @Override
    public PortalLogic.Builder addPortal(final PortalLogic.PortalExitCalculator calulator,
            final PortalLogic.PortalFinder finder,
            final PortalLogic.PortalGenerator generator) {
        var portal = new SpongeCustomPortalLogic(calulator, finder, generator);
        this.rules.add(portal);
        return this;
    }

    @Override
    public <T extends PortalLogic.PortalExitCalculator & PortalLogic.PortalFinder & PortalLogic.PortalGenerator> PortalLogic.Builder addPortal(final T logic) {
        var portal = new SpongeCustomPortalLogic(logic, logic, logic);
        this.rules.add(portal);
        return this;
    }

    @Override
    public PortalLogic build() {
        return new SpongeCompositePortalLogic(this.rules);
    }


}
