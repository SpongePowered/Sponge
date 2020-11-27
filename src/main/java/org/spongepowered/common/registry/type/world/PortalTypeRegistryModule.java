package org.spongepowered.common.registry.type.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.PortalType;
import org.spongepowered.api.world.PortalTypes;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.world.SpongePortalType;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class PortalTypeRegistryModule implements SpongeAdditionalCatalogRegistryModule<PortalType> {

    @RegisterCatalog(PortalTypes.class)
    private final Map<String, PortalType> portalTypeMap = ImmutableMap.<String, PortalType>builder()
            .put("end", new SpongePortalType("end", "End"))
            .put("nether", new SpongePortalType("nether", "Nether"))
            .build();

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(PortalType extraCatalog) {
    }

    @Override
    public Optional<PortalType> getById(String id) {
        return Optional.ofNullable(this.portalTypeMap.get(checkNotNull(id, "PortalType ID cannot be null!")
                .toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<PortalType> getAll() {
        return this.portalTypeMap.values();
    }
}
