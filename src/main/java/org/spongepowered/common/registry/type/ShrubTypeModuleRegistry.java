package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockTallGrass;
import org.spongepowered.api.data.type.ShrubType;
import org.spongepowered.api.data.type.ShrubTypes;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class ShrubTypeModuleRegistry implements CatalogRegistryModule<ShrubType> {

    @RegisterCatalog(ShrubTypes.class)
    private final Map<String, ShrubType> shrubTypeMappings = new ImmutableMap.Builder<String, ShrubType>()
        .put("dead_bush", (ShrubType) (Object) BlockTallGrass.EnumType.DEAD_BUSH)
        .put("tall_grass", (ShrubType) (Object) BlockTallGrass.EnumType.GRASS)
        .put("fern", (ShrubType) (Object) BlockTallGrass.EnumType.FERN)
        .build();

    @Override
    public Optional<ShrubType> getById(String id) {
        return Optional.ofNullable(this.shrubTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<ShrubType> getAll() {
        return ImmutableList.copyOf(this.shrubTypeMappings.values());
    }

}
