package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.spongepowered.api.text.selector.SelectorType;
import org.spongepowered.api.text.selector.SelectorTypes;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;
import org.spongepowered.common.text.selector.SpongeSelectorType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class SelectorTypeRegistryModule implements CatalogRegistryModule<SelectorType> {

    @RegisterCatalog(SelectorTypes.class)
    private final Map<String, SelectorType> selectorMappings = Maps.newHashMap();

    @Override
    public Optional<SelectorType> getById(String id) {
        return Optional.ofNullable(this.selectorMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<SelectorType> getAll() {
        return ImmutableList.copyOf(this.selectorMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.selectorMappings.put("all_players", new SpongeSelectorType("a"));
        this.selectorMappings.put("all_entities", new SpongeSelectorType("e"));
        this.selectorMappings.put("nearest_player", new SpongeSelectorType("p"));
        this.selectorMappings.put("random", new SpongeSelectorType("r"));
    }
}
