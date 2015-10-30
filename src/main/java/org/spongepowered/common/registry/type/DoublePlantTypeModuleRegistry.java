package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockDoublePlant;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class DoublePlantTypeModuleRegistry implements CatalogRegistryModule<DoublePlantType> {

    @RegisterCatalog(DoublePlantTypes.class)
    private final Map<String, DoublePlantType> doublePlantMappings = new ImmutableMap.Builder<String, DoublePlantType>()
        .put("sunflower", (DoublePlantType) (Object) BlockDoublePlant.EnumPlantType.SUNFLOWER)
        .put("syringa", (DoublePlantType) (Object) BlockDoublePlant.EnumPlantType.SYRINGA)
        .put("grass", (DoublePlantType) (Object) BlockDoublePlant.EnumPlantType.GRASS)
        .put("fern", (DoublePlantType) (Object) BlockDoublePlant.EnumPlantType.FERN)
        .put("rose", (DoublePlantType) (Object) BlockDoublePlant.EnumPlantType.ROSE)
        .put("paeonia", (DoublePlantType) (Object) BlockDoublePlant.EnumPlantType.PAEONIA)
        .build();

    @Override
    public Optional<DoublePlantType> getById(String id) {
        return Optional.ofNullable(this.doublePlantMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<DoublePlantType> getAll() {
        return ImmutableList.copyOf(this.doublePlantMappings.values());
    }

}
