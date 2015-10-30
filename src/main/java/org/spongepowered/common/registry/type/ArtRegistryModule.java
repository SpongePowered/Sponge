package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.entity.item.EntityPainting;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.data.type.Arts;
import org.spongepowered.common.registry.AdditionalRegistration;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class ArtRegistryModule implements CatalogRegistryModule<Art> {

    @RegisterCatalog(Arts.class)
    private final Map<String, Art> artMappings = Maps.newHashMap();

    @Override
    public Optional<Art> getById(String id) {
        return Optional.ofNullable(this.artMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<Art> getAll() {
        return ImmutableList.copyOf(this.artMappings.values());
    }

    @Override
    public void registerDefaults() {
        for (EntityPainting.EnumArt art : EntityPainting.EnumArt.values()) {
            this.artMappings.put(((Art) (Object) art).getId().toLowerCase(), (Art) (Object) art);
        }
    }

    @AdditionalRegistration
    public void registerAdditionals() {
        for (EntityPainting.EnumArt art : EntityPainting.EnumArt.values()) {
            if (!this.artMappings.containsValue((Art) (Object) art)) {
                this.artMappings.put(((Art) (Object) art).getId().toLowerCase(), (Art) (Object) art);
            }
        }
    }
}
