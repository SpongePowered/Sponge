package org.spongepowered.common.mixin.core.tileentity;

import static org.spongepowered.api.data.DataQuery.of;

import net.minecraft.tileentity.TileEntityLockableLoot;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TileEntityLockableLoot.class)
public abstract class MixinTileEntityLockableLoot extends MixinTileEntityLockable {

    @Shadow protected String fld_000857_o; // customName

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        if (this.fld_000857_o != null) {
            container.set(of("CustomName"), this.fld_000857_o);
        }
        return container;
    }

}
