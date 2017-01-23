package org.spongepowered.common.registry.type.tileentity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.tileentity.TileEntityStructure;
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.api.data.type.StructureModes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class StructureModeRegistryModule implements CatalogRegistryModule<StructureMode> {

    @RegisterCatalog(StructureModes.class)
    public final Map<String, StructureMode> structureModes = Maps.newHashMap();

    @Override
    public void registerDefaults() {
        this.structureModes.put("corner", (StructureMode) (Object) TileEntityStructure.Mode.CORNER);
        this.structureModes.put("data", (StructureMode) (Object) TileEntityStructure.Mode.DATA);
        this.structureModes.put("load", (StructureMode) (Object) TileEntityStructure.Mode.LOAD);
        this.structureModes.put("save", (StructureMode) (Object) TileEntityStructure.Mode.SAVE);
    }

    @Override
    public Optional<StructureMode> getById(String id) {
        return Optional.ofNullable(this.structureModes.get(checkNotNull(id, "id").toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<StructureMode> getAll() {
        return ImmutableSet.copyOf(this.structureModes.values());
    }

    @AdditionalRegistration
    public void customRegistration() {
        for (TileEntityStructure.Mode mode : TileEntityStructure.Mode.values()) {
            String name = mode.name().toLowerCase(Locale.ENGLISH);
            if (!this.structureModes.containsKey(name)) {
                this.structureModes.put(name, (StructureMode) (Object) mode);
            }
        }
    }

}
