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
package org.spongepowered.common.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import net.minecraft.world.Teleporter;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.common.SpongeCatalogType;
import org.spongepowered.common.interfaces.world.IMixinITeleporter;

public final class SpongePortalAgentType extends SpongeCatalogType implements PortalAgentType {

    private final Class<? extends IMixinITeleporter> portalAgentClass;

    public SpongePortalAgentType(String id, String name, Class<? extends IMixinITeleporter> portalAgentClass) {
        super(CatalogKey.resolve(id), name);
        this.portalAgentClass = checkNotNull(portalAgentClass, "The class was null! The id was: " + id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends PortalAgent> getPortalAgentClass() {
        return (Class<? extends PortalAgent>) this.portalAgentClass;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("class", this.portalAgentClass.getName());
    }
}
