package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.common.data.type.SpongeSkullType;
import org.spongepowered.common.data.type.SpongeTreeType;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TreeTypeRegistryModule implements CatalogRegistryModule<TreeType> {

    @RegisterCatalog(TreeTypes.class)
    private final Map<String, TreeType> skullTypeMap = new HashMap<>();

    @Override
    public Optional<TreeType> getById(String id) {
        return Optional.ofNullable(this.skullTypeMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<TreeType> getAll() {
        return ImmutableList.copyOf(this.skullTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        this.skullTypeMap.put("oak", new SpongeTreeType((byte) 0, "oak"));
        this.skullTypeMap.put("spruce", new SpongeTreeType((byte)1, "spruce"));
        this.skullTypeMap.put("birch", new SpongeTreeType((byte) 2, "birch"));
        this.skullTypeMap.put("jungle", new SpongeTreeType((byte) 3, "jungle"));
        this.skullTypeMap.put("acacia", new SpongeTreeType((byte) 4, "acacia"));
    }
}
