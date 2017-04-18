package org.spongepowered.common.data.util;

import org.spongepowered.api.data.DataAlreadyRegisteredException;
import org.spongepowered.api.data.DataRegistrationNotFoundException;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.common.data.SpongeManipulatorRegistry;

public class LegacyCustomDataClassContentUpdater implements DataContentUpdater{

    @Override
    public int getInputVersion() {
        return DataVersions.Data.CLASS_BASED_CUSTOM_DATA;
    }

    @Override
    public int getOutputVersion() {
        return DataVersions.Data.CUSTOM_DATA_WITH_DATA_IDS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public DataView update(DataView content) {
        final String className = content.getString(DataQueries.DATA_CLASS).get();

        SpongeManipulatorRegistry.getInstance().getRegistrationForLegacyId(className).orElseThrow(() -> new DataRegistrationNotFoundException(className));
        content.remove(DataQueries.DATA_CLASS);
        return null;
    }
}
