package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockDirt;
import org.spongepowered.api.data.type.DirtType;
import org.spongepowered.api.data.type.DirtTypes;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class DirtTypeRegistryModule implements CatalogRegistryModule<DirtType> {

    @RegisterCatalog(DirtTypes.class)
    private final Map<String, DirtType> dirtTypeMappings = new ImmutableMap.Builder<String, DirtType>()
        .put("dirt", (DirtType) (Object) BlockDirt.DirtType.DIRT)
        .put("coarse_dirt", (DirtType) (Object) BlockDirt.DirtType.COARSE_DIRT)
        .put("podzol", (DirtType) (Object) BlockDirt.DirtType.PODZOL)
        .build();

    @Override
    public Optional<DirtType> getById(String id) {
        return Optional.ofNullable(this.dirtTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<DirtType> getAll() {
        return ImmutableList.copyOf(this.dirtTypeMappings.values());
    }

}
