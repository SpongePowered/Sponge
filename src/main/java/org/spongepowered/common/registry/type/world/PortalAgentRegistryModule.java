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
package org.spongepowered.common.registry.type.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class PortalAgentRegistryModule implements SpongeAdditionalCatalogRegistryModule<PortalAgentType> {

    public static PortalAgentRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @SuppressWarnings("serial")
    @RegisterCatalog(PortalAgentTypes.class)
    private final Map<String, PortalAgentType> portalAgentTypeMappings = new HashMap<String, PortalAgentType>() {{
        put("minecraft:default", PortalAgentTypes.DEFAULT);
    }};

    @Override
    public Optional<PortalAgentType> getById(String id) {
        return Optional.ofNullable(this.portalAgentTypeMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<PortalAgentType> getAll() {
        return ImmutableList.copyOf(this.portalAgentTypeMappings.values());
    }

    @Override
    public void registerAdditionalCatalog(PortalAgentType portalAgentType) {
        if (this.portalAgentTypeMappings.get(portalAgentType.getId()) == null) {
            this.portalAgentTypeMappings.put(portalAgentType.getId(), portalAgentType);
        }
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    private static final class Holder {
        static final PortalAgentRegistryModule INSTANCE = new PortalAgentRegistryModule();
    }
}
