package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.EnumDifficulty;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.common.registry.AdditionalRegistration;
import org.spongepowered.common.registry.CatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DifficultyRegistryModule implements CatalogRegistryModule<Difficulty> {

    private final Map<String, Difficulty> difficultyMappings = new HashMap<>();

    @Override
    public Optional<Difficulty> getById(String id) {
        return Optional.ofNullable(this.difficultyMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<Difficulty> getAll() {
        return ImmutableList.copyOf(this.difficultyMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.difficultyMappings.put("peaceful", (Difficulty) (Object) EnumDifficulty.PEACEFUL);
        this.difficultyMappings.put("easy", (Difficulty) (Object) EnumDifficulty.EASY);
        this.difficultyMappings.put("normal", (Difficulty) (Object) EnumDifficulty.NORMAL);
        this.difficultyMappings.put("hard", (Difficulty) (Object) EnumDifficulty.HARD);
    }

    @AdditionalRegistration
    public void additional() {
        for (EnumDifficulty difficulty : EnumDifficulty.values()) {
            if (!this.difficultyMappings.containsValue((Difficulty) (Object) difficulty)) {
                this.difficultyMappings.put(difficulty.name(), (Difficulty) (Object) difficulty);
            }
        }
    }
}
