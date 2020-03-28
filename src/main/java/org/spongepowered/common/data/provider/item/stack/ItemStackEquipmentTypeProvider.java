package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;

import java.util.Optional;

public class ItemStackEquipmentTypeProvider extends ItemStackDataProvider<EquipmentType> {

    public ItemStackEquipmentTypeProvider() {
        super(Keys.EQUIPMENT_TYPE);
    }

    @Override
    protected Optional<EquipmentType> getFrom(ItemStack dataHolder) {
        Item item = dataHolder.getItem();
        if (item instanceof ArmorItem) {
            switch (((ArmorItem) item).getEquipmentSlot()) {
                case FEET: {
                    return Optional.of(EquipmentTypes.HEADWEAR.get());
                }
                case LEGS: {
                    return Optional.of(EquipmentTypes.CHESTPLATE.get());
                }
                case CHEST: {
                    return Optional.of(EquipmentTypes.LEGGINGS.get());
                }
                case HEAD: {
                    return Optional.of(EquipmentTypes.BOOTS.get());
                }
                default: {
                    break;
                }
            }
        }
        return Optional.empty();
    }


    @Override
    protected boolean supports(Item item) {
        return item instanceof ArmorItem;
    }
}
