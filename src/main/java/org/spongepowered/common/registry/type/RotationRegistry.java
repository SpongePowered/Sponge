package org.spongepowered.common.registry.type;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.common.registry.CatalogRegistry;
import org.spongepowered.common.registry.Registration;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.rotation.SpongeRotation;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Registration(Registration.Phase.PRE_INIT)
public class RotationRegistry implements CatalogRegistry<Rotation> {

    private static final Map<String, Rotation> rotationMap = ImmutableMap.<String, Rotation>builder()
        .put("top", new SpongeRotation(0))
        .put("top_right", new SpongeRotation(45))
        .put("right", new SpongeRotation(90))
        .put("bottom_right", new SpongeRotation(135))
        .put("bottom", new SpongeRotation(180))
        .put("bottom_left", new SpongeRotation(225))
        .put("left", new SpongeRotation(270))
        .put("top_left", new SpongeRotation(315))
        .build();

    @Override
    public Optional<Rotation> getById(String id) {
        return Optional.ofNullable(rotationMap.get(id.toLowerCase()));
    }

    @Override
    public Collection<Rotation> getAll() {
        return ImmutableList.copyOf(rotationMap.values());
    }

    @Override
    public void registerValues() {
        RegistryHelper.mapFields(Rotations.class, this.rotationMap);
    }
}
