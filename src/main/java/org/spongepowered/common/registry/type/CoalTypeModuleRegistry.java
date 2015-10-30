package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.type.CoalType;
import org.spongepowered.api.data.type.CoalTypes;
import org.spongepowered.common.item.SpongeCoalType;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class CoalTypeModuleRegistry implements CatalogRegistryModule<CoalType> {

    @RegisterCatalog(CoalTypes.class)
    public final Map<String, CoalType> coaltypeMappings = Maps.newHashMap();

    @Override
    public Optional<CoalType> getById(String id) {
        return Optional.ofNullable(this.coaltypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<CoalType> getAll() {
        return ImmutableList.copyOf(this.coaltypeMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.coaltypeMappings.put("coal", new SpongeCoalType(0, "COAL"));
        this.coaltypeMappings.put("charcoal", new SpongeCoalType(1, "CHARCOAL"));
    }
}
