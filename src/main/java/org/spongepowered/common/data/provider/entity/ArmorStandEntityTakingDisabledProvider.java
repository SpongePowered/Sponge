package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.item.ArmorStandEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.entity.item.ArmorStandEntityAccessor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ArmorStandEntityTakingDisabledProvider extends GenericMutableDataProvider<ArmorStandEntity, Set<EquipmentType>> {

    public ArmorStandEntityTakingDisabledProvider() {
        super(Keys.ARMOR_STAND_TAKING_DISABLED);
    }

    @Override
    protected Optional<Set<EquipmentType>> getFrom(ArmorStandEntity dataHolder) {
        // include all chunk
        final int disabled = ((ArmorStandEntityAccessor) dataHolder).accessor$getDisabledSlots();
        final int resultantChunk = ((disabled >> 16) & 0b1111_1111) | (disabled & 0b1111_1111);

        final Set<EquipmentType> val = new HashSet<>();

        if ((resultantChunk & (1 << 1)) != 0) val.add(EquipmentTypes.BOOTS);
        if ((resultantChunk & (1 << 2)) != 0) val.add(EquipmentTypes.LEGGINGS);
        if ((resultantChunk & (1 << 3)) != 0) val.add(EquipmentTypes.CHESTPLATE);
        if ((resultantChunk & (1 << 4)) != 0) val.add(EquipmentTypes.HEADWEAR);

        return Optional.of(val);
    }

    @Override
    protected boolean set(ArmorStandEntity dataHolder, Set<EquipmentType> value) {
        int chunk = 0;

        // try and keep the all chunk empty
        int disabledSlots = ((ArmorStandEntityAccessor) dataHolder).accessor$getDisabledSlots();
        final int allChunk = disabledSlots & 0b1111_1111;
        if (allChunk != 0) {
            disabledSlots |= (allChunk << 16);
            disabledSlots ^= 0b1111_1111;
            ((ArmorStandEntityAccessor) dataHolder).accessor$setDisabledSlots(disabledSlots);
        }

        if (value.contains(EquipmentTypes.BOOTS)) chunk |= 1 << 1;
        if (value.contains(EquipmentTypes.LEGGINGS)) chunk |= 1 << 2;
        if (value.contains(EquipmentTypes.CHESTPLATE)) chunk |= 1 << 3;
        if (value.contains(EquipmentTypes.HEADWEAR)) chunk |= 1 << 4;

        disabledSlots |= (chunk << 8);
        ((ArmorStandEntityAccessor) dataHolder).accessor$setDisabledSlots(disabledSlots);

        return true;
    }
}
