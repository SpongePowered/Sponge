package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemFishFood;
import org.spongepowered.api.data.type.CookedFish;
import org.spongepowered.api.data.type.CookedFishes;
import org.spongepowered.common.data.type.SpongeCookedFish;
import org.spongepowered.common.registry.AdditionalRegistration;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CookedFishRegistryModule implements CatalogRegistryModule<CookedFish> {

    @RegisterCatalog(CookedFishes.class)
    private final Map<String, CookedFish> fishMap = new HashMap<>();

    @Override
    public Optional<CookedFish> getById(String id) {
        return Optional.ofNullable(this.fishMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<CookedFish> getAll() {
       return ImmutableList.copyOf(this.fishMap.values());
    }

    @Override
    public void registerDefaults() {
        for (ItemFishFood.FishType fishType : ItemFishFood.FishType.values()) {
            if (fishType.canCook()) {
                CookedFish cooked = new SpongeCookedFish(fishType.name(), fishType.name(), fishType);
                this.fishMap.put(cooked.getId().toLowerCase(), cooked);
            }
        }
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (ItemFishFood.FishType fishType : ItemFishFood.FishType.values()) {
            if (fishType.canCook() && !this.fishMap.containsKey(fishType.name().toLowerCase())) {
                CookedFish cooked = new SpongeCookedFish(fishType.name(), fishType.name(), fishType);
                this.fishMap.put(cooked.getId().toLowerCase(), cooked);
            }
        }
    }


}
