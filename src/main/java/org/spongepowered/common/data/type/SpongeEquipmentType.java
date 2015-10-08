package org.spongepowered.common.data.type;

import org.spongepowered.api.item.inventory.equipment.EquipmentType;

public class SpongeEquipmentType implements EquipmentType {
    private final String id;

    public SpongeEquipmentType(String id) {
        this.id = id;
    }

    public SpongeEquipmentType() {
        this("ANY");
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(obj instanceof EquipmentType) {
            if(((EquipmentType) obj).getId().equals("ANY"))
                return true;
            if(((EquipmentType) obj).getId().equals(getId()))
                return true;
        }
        return false;
    }
}
