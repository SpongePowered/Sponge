package org.spongepowered.common.data.processor.common;

import com.google.common.collect.Iterables;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySkull;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.common.Sponge;

public class SkullUtils {

    /**
     * There's not really a meaningful default value for this, since it's a CatalogType.
     * However, the Vanilla give command defaults the skeleton type (index 0), so it's used as
     * the default here.
     */
    public static final SkullType DEFAULT_TYPE = SkullTypes.SKELETON;

    public static boolean supportsObject(Object object) {
        return object instanceof TileEntitySkull || isValidItemStack(object);
    }

    public static SkullType getSkullType(int skullType) {
        return Iterables.get(Sponge.getSpongeRegistry().skullTypeMappings.values(), skullType);
    }

    public static boolean isValidItemStack(Object container) {
        return container instanceof ItemStack && ((ItemStack) container).getItem().equals(Items.skull);
    }

    public static SkullType getSkullType(TileEntitySkull tileEntitySkull) {
        return SkullUtils.getSkullType(tileEntitySkull.getSkullType());
    }

    public static void setSkullType(TileEntitySkull tileEntitySkull, int skullType) {
        tileEntitySkull.setType(skullType);
        tileEntitySkull.markDirty();
        tileEntitySkull.getWorld().markBlockForUpdate(tileEntitySkull.getPos());
    }

    public static SkullType getSkullType(ItemStack itemStack) {
        return SkullUtils.getSkullType(itemStack.getMetadata());
    }

}
