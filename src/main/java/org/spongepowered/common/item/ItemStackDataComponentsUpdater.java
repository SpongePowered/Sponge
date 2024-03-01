package org.spongepowered.common.item;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.Constants;

class ItemStackDataComponentsUpdater implements DataContentUpdater {

    public static final DataContentUpdater INSTANCE = new ItemStackDataComponentsUpdater();

    @Override
    public int inputVersion() {
        return Constants.ItemStack.Data.DUPLICATE_MANIPULATOR_DATA_VERSION;
    }

    @Override
    public int outputVersion() {
        return Constants.ItemStack.Data.DATA_COMPONENTS;
    }

    @Override
    public DataView update(final DataView content) {
        final int count = content.getInt(Constants.ItemStack.V2.COUNT).get();
        final String type = content.getString(Constants.ItemStack.V2.TYPE).get();

        final DataContainer updated = DataContainer.createNew();
        updated.set(Constants.ItemStack.TYPE, type);
        updated.set(Constants.ItemStack.COUNT, count);

        final DataContainer components = DataContainer.createNew();
        content.getInt(Constants.ItemStack.V2.DAMAGE_VALUE).ifPresent(dmg -> components.set(Constants.ItemStack.DAMAGE, dmg));
        content.getView(Constants.Sponge.UNSAFE_NBT).ifPresent(unsafe -> {
            // TODO unsafe contains the entire old nbt tag
            // TODO update plugin/sponge data
            // TODO apply ItemStackComponentizationFix for vanilla data
            components.set(Constants.ItemStack.CUSTOM_DATA, unsafe);
        });

        updated.set(Constants.ItemStack.COMPONENTS, components);

        return updated;
    }
}
