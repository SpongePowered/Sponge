package org.spongepowered.common.data.type;

import org.spongepowered.api.item.inventory.equipment.EquipmentTypeWorn;

public class SpongeEquipmentTypeWorn extends SpongeEquipmentType implements EquipmentTypeWorn {

    public SpongeEquipmentTypeWorn(String id) {
        super(id);
    }

    public SpongeEquipmentTypeWorn() {
        this("WORN");
    }

    @Override
    public boolean equals(Object obj) {
        if(super.equals(obj))
            return true;
        if(obj instanceof SpongeEquipmentTypeWorn) {
            if(((EquipmentTypeWorn) obj).getId().equals("WORN"))
                return true;
        }
        return false;
    }
}
