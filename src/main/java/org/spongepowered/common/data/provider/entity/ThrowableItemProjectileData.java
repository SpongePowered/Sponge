package org.spongepowered.common.data.provider.entity;

import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.item.util.ItemStackUtil;

public class ThrowableItemProjectileData {

    private ThrowableItemProjectileData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ThrowableItemProjectile.class)
                    .create(Keys.ITEM_STACK_SNAPSHOT)
                        .get(h -> ItemStackUtil.snapshotOf(h.getItem()))
                        .setAnd((h, v) -> {
                            h.setItem(ItemStackUtil.fromSnapshotToNative(v));

                            return true;
                        });
    }
    // @formatter:on

}
