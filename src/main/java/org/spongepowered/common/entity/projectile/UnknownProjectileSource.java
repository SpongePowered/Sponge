package org.spongepowered.common.entity.projectile;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

public final class UnknownProjectileSource implements ProjectileSource {

    public static final UnknownProjectileSource UNKNOWN = new UnknownProjectileSource();

    private UnknownProjectileSource() {
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass) {
        return Optional.empty();
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass, Vector3d velocity) {
        return Optional.empty();
    }

    @Override
    public <T extends Projectile> Optional<T> launchToTarget(Class<T> projectileClass, Entity target) {
        return Optional.empty();
    }
}
