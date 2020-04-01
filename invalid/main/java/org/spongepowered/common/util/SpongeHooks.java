package org.spongepowered.common.util;

public class SpongeHooks {


    public static void refreshActiveConfigs() {
        for (final BlockType blockType : BlockTypeRegistryModule.getInstance().getAll()) {
            if (blockType instanceof CollisionCapabilityBridge) {
                ((CollisionCapabilityBridge) blockType).collision$requiresCollisionsCacheRefresh(true);
            }
            if (blockType instanceof TrackableBridge) {
                ((BlockBridge) blockType).bridge$initializeTrackerState();
            }
        }
        for (final BlockEntityType tileEntityType : TileEntityTypeRegistryModule.getInstance().getAll()) {
            ((SpongeTileEntityType) tileEntityType).initializeTrackerState();
        }
        for (final EntityType entityType : EntityTypeRegistryModule.getInstance().getAll()) {
            ((SpongeEntityType) entityType).initializeTrackerState();
        }

        for (final org.spongepowered.api.world.server.ServerWorld apiWorld : SpongeImpl.getWorldManager().getWorlds()) {
            final ServerWorld world = (ServerWorld) apiWorld;
            final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
            // Reload before updating world config cache
            configAdapter.load();
            ((ServerWorldBridge) world).bridge$updateConfigCache();
            for (final Entity entity : ((ServerWorldAccessor) world).accessor$getEntitiesById().values()) {
                if (entity instanceof ActivationCapabilityBridge) {
                    ((ActivationCapabilityBridge) entity).activation$requiresActivationCacheRefresh(true);
                }
                if (entity instanceof CollisionCapabilityBridge) {
                    ((CollisionCapabilityBridge) entity).collision$requiresCollisionsCacheRefresh(true);
                }
                if (entity instanceof TrackableBridge) {
                    ((TrackableBridge) entity).bridge$refreshTrackerStates();
                }
            }
            for (final TileEntity tileEntity : world.loadedTileEntityList) {
                if (tileEntity instanceof ActivationCapabilityBridge) {
                    ((ActivationCapabilityBridge) tileEntity).activation$requiresActivationCacheRefresh(true);
                }
                if (tileEntity instanceof TrackableBridge) {
                    ((TrackableBridge) tileEntity).bridge$refreshTrackerStates();
                }
            }
        }
        ConfigTeleportHelperFilter.invalidateCache();
    }


}
