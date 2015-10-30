package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemFishFood;
import org.spongepowered.api.data.type.Fish;
import org.spongepowered.api.data.type.Fishes;
import org.spongepowered.common.registry.AdditionalRegistration;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FishRegistryModule implements CatalogRegistryModule<Fish> {

    @RegisterCatalog(Fishes.class)
    private final Map<String, Fish> fishMap = new HashMap<>();

    @Override
    public Optional<Fish> getById(String id) {
        return Optional.ofNullable(this.fishMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<Fish> getAll() {
       return ImmutableList.copyOf(this.fishMap.values());
    }

    @Override
    public void registerDefaults() {
        for (ItemFishFood.FishType fishType : ItemFishFood.FishType.values()) {
            this.fishMap.put(fishType.name().toLowerCase(), (Fish) (Object) fishType);
        }
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (ItemFishFood.FishType fishType : ItemFishFood.FishType.values()) {
            if (!this.fishMap.containsValue((Fish) (Object) fishType)) {
                this.fishMap.put(fishType.name().toLowerCase(), (Fish) (Object) fishType);
            }
        }
    }


}
