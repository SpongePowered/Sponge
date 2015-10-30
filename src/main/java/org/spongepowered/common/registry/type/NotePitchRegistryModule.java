package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.type.NotePitch;
import org.spongepowered.api.data.type.NotePitches;
import org.spongepowered.common.data.type.SpongeNotePitch;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;
import org.spongepowered.common.registry.RegistryHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NotePitchRegistryModule implements CatalogRegistryModule<NotePitch> {

    private final Map<String, NotePitch> notePitchMap = new HashMap<>();

    @Override
    public Optional<NotePitch> getById(String id) {
        return Optional.ofNullable(this.notePitchMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<NotePitch> getAll() {
        return ImmutableList.copyOf(this.notePitchMap.values());
    }

    @Override
    public void registerDefaults() {
        RegistryHelper.mapFields(NotePitches.class, input -> {
            NotePitch pitch = new SpongeNotePitch((byte) this.notePitchMap.size(), input);
            this.notePitchMap.put(input.toLowerCase(), pitch);
            return pitch;
        });
    }
}
