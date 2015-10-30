package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockWall;
import org.spongepowered.api.data.type.WallType;
import org.spongepowered.api.data.type.WallTypes;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class WallTypeRegistryModule implements CatalogRegistryModule<WallType> {

    @RegisterCatalog(WallTypes.class)
    private final Map<String, WallType> wallTypeMappings = new ImmutableMap.Builder<String, WallType>()
        .put("normal", (WallType) (Object) BlockWall.EnumType.NORMAL)
        .put("mossy", (WallType) (Object) BlockWall.EnumType.MOSSY)
        .build();

    @Override
    public Optional<WallType> getById(String id) {
        return Optional.ofNullable(this.wallTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<WallType> getAll() {
        return ImmutableList.copyOf(this.wallTypeMappings.values());
    }

}
