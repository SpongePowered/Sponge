package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.api.data.type.ArmorType;

import java.util.Optional;

// TODO add ArmorType back to API - IArmorMaterial/ArmorMaterial exists e138911acad1eedfaf927483804587891213a887
public class ItemStackArmorTypeProvider extends ItemStackDataProvider<ArmorType> {

    public ItemStackArmorTypeProvider() {
        super(Keys.ARMOR_TYPE);
    }

    @Override
    protected Optional<ArmorType> getFrom(ItemStack dataHolder) {
        Item item = dataHolder.getItem();
        if (item instanceof ArmorItem) {
            IArmorMaterial armor = ((ArmorItem) item).getArmorMaterial();
            if (armor instanceof ArmorType) {
                return Optional.of((ArmorType)armor);
            }
        }
        return Optional.empty();
    }
}
