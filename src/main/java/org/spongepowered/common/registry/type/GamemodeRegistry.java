package org.spongepowered.common.registry.type;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.common.registry.CatalogRegistry;
import org.spongepowered.common.registry.Registration;

import java.util.Collection;
import java.util.Optional;

@Registration(Registration.Phase.INIT)
public class GamemodeRegistry implements CatalogRegistry<GameMode> {

    @Override
    public Optional<GameMode> getById(String id) {
        return null;
    }

    @Override
    public Collection<GameMode> getAll() {
        return ImmutableList.copyOf((GameMode[]) (Object[]) WorldSettings.GameType.values());
    }

    @Override
    public void registerValues() {

    }
}
