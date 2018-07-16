package org.spongepowered.common.registry.type.item;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.api.item.ItemGroup;
import org.spongepowered.api.item.ItemGroups;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ItemGroupRegistryModule implements CatalogRegistryModule<ItemGroup> {

    @RegisterCatalog(ItemGroups.class)
    private final Map<String, ItemGroup> itemGroupMap = new HashMap<>();

    @Override
    public Optional<ItemGroup> getById(String id) {
        String key = checkNotNull(id).toLowerCase(Locale.ENGLISH);
        return Optional.ofNullable(this.itemGroupMap.get(key));
    }

    @Override
    public Collection<ItemGroup> getAll() {
        return Collections.unmodifiableCollection(this.itemGroupMap.values());
    }

    @Override
    public void registerDefaults() {
        this.itemGroupMap.put("brewing", (ItemGroup) CreativeTabs.BREWING);
        this.itemGroupMap.put("building_blocks", (ItemGroup) CreativeTabs.BUILDING_BLOCKS);
        this.itemGroupMap.put("combat", (ItemGroup) CreativeTabs.COMBAT);
        this.itemGroupMap.put("decorations", (ItemGroup) CreativeTabs.DECORATIONS);
        this.itemGroupMap.put("food", (ItemGroup) CreativeTabs.FOOD);
        this.itemGroupMap.put("materials", (ItemGroup) CreativeTabs.MATERIALS);
        this.itemGroupMap.put("misc", (ItemGroup) CreativeTabs.MISC);
        this.itemGroupMap.put("redstone", (ItemGroup) CreativeTabs.REDSTONE);
        this.itemGroupMap.put("tools", (ItemGroup) CreativeTabs.TOOLS);
        this.itemGroupMap.put("transportation", (ItemGroup) CreativeTabs.TRANSPORTATION);
    }
}
