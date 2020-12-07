package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class ProjectileData {

    private ProjectileData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ProjectileEntity.class)
                .create(Keys.SHOOTER)
                .get(h -> (ProjectileSource) h.func_234616_v_())
                .set((h, v) -> h.setShooter((Entity) v));
    }
    // @formatter:on
}
