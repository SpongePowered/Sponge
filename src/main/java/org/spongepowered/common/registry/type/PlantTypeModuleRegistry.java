package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockFlower;
import org.spongepowered.api.data.type.PlantType;
import org.spongepowered.api.data.type.PlantTypes;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class PlantTypeModuleRegistry implements CatalogRegistryModule<PlantType> {

    @RegisterCatalog(PlantTypes.class)
    private final Map<String, PlantType> plantTypeMappings = new ImmutableMap.Builder<String, PlantType>()
        .put("dandelion", (PlantType) (Object) BlockFlower.EnumFlowerType.DANDELION)
        .put("poppy", (PlantType) (Object) BlockFlower.EnumFlowerType.POPPY)
        .put("blue_orchid", (PlantType) (Object) BlockFlower.EnumFlowerType.BLUE_ORCHID)
        .put("allium", (PlantType) (Object) BlockFlower.EnumFlowerType.ALLIUM)
        .put("houstonia", (PlantType) (Object) BlockFlower.EnumFlowerType.HOUSTONIA)
        .put("red_tulip", (PlantType) (Object) BlockFlower.EnumFlowerType.RED_TULIP)
        .put("orange_tulip", (PlantType) (Object) BlockFlower.EnumFlowerType.ORANGE_TULIP)
        .put("white_tulip", (PlantType) (Object) BlockFlower.EnumFlowerType.WHITE_TULIP)
        .put("pink_tulip", (PlantType) (Object) BlockFlower.EnumFlowerType.PINK_TULIP)
        .put("oxeye_daisy", (PlantType) (Object) BlockFlower.EnumFlowerType.OXEYE_DAISY)
        .build();

    @Override
    public Optional<PlantType> getById(String id) {
        return Optional.ofNullable(this.plantTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<PlantType> getAll() {
        return ImmutableList.copyOf(this.plantTypeMappings.values());
    }

}
