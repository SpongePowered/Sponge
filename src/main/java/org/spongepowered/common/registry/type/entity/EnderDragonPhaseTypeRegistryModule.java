package org.spongepowered.common.registry.type.entity;

import net.minecraft.entity.boss.dragon.phase.PhaseList;
import org.spongepowered.api.entity.living.complex.dragon.phase.EnderDragonPhaseType;
import org.spongepowered.api.entity.living.complex.dragon.phase.EnderDragonPhaseTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EnderDragonPhaseTypeRegistryModule implements CatalogRegistryModule<EnderDragonPhaseType> {

    @RegisterCatalog(EnderDragonPhaseTypes.class)
    private final Map<String, EnderDragonPhaseType> phaseTypeMap = new HashMap<>();

    @Override
    public Optional<EnderDragonPhaseType> getById(String id) {
        return Optional.ofNullable(this.phaseTypeMap.get(id));
    }

    @Override
    public Collection<EnderDragonPhaseType> getAll() {
        return Collections.unmodifiableCollection(this.phaseTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        for (PhaseList<?> phaseType : PhaseList.phases) {
            this.phaseTypeMap.put(((EnderDragonPhaseType) phaseType).getId(), (EnderDragonPhaseType) phaseType);
        }
    }

    public static EnderDragonPhaseTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    static final class Holder {
        static final EnderDragonPhaseTypeRegistryModule INSTANCE = new EnderDragonPhaseTypeRegistryModule();
    }
}
