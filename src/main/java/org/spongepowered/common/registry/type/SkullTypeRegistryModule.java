package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.common.data.type.SpongeSkullType;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SkullTypeRegistryModule implements CatalogRegistryModule<SkullType> {

    @RegisterCatalog(SkullTypes.class)
    private final Map<String, SkullType> skullTypeMap = new HashMap<>();

    @Override
    public Optional<SkullType> getById(String id) {
        return Optional.ofNullable(this.skullTypeMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<SkullType> getAll() {
        return ImmutableList.copyOf(this.skullTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        this.skullTypeMap.put("skeleton", new SpongeSkullType((byte) 0, "skeleton"));
        this.skullTypeMap.put("wither_skeleton", new SpongeSkullType((byte)1, "wither_skeleton"));
        this.skullTypeMap.put("zombie", new SpongeSkullType((byte) 2, "zombie"));
        this.skullTypeMap.put("player", new SpongeSkullType((byte) 3, "player"));
        this.skullTypeMap.put("creeper", new SpongeSkullType((byte) 4, "creeper"));
    }
}
