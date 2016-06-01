package org.spongepowered.common.registry.type.data;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class HandTypeRegistryModule implements SpongeAdditionalCatalogRegistryModule<HandType> {

    public static HandTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(HandTypes.class)
    private final Map<String, HandType> handTypeMap = new HashMap<>();

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(HandType extraCatalog) {
        throw new UnsupportedOperationException("Cannot register additional HandTypes!!!");
    }

    @Override
    public Optional<HandType> getById(String id) {
        return Optional.ofNullable(this.handTypeMap.get(checkNotNull(id, "Id cannot be null").toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<HandType> getAll() {
        return ImmutableSet.copyOf(this.handTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        for (EnumHand enumHand : EnumHand.values()) {
            this.handTypeMap.put(enumHand.name().toLowerCase(Locale.ENGLISH), (HandType) (Object) enumHand);
        }
    }

    HandTypeRegistryModule() {
    }

    static final class Holder {
        static final HandTypeRegistryModule INSTANCE = new HandTypeRegistryModule();
    }
}
