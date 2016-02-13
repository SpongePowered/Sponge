package org.spongepowered.common.event.tracking;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.World;

import java.util.List;

import javax.annotation.Nullable;

public interface ISpawnablePhase {

    @Nullable
    SpawnEntityEvent createEventPostPrcess(Cause cause, CauseTracker causeTracker, List<EntitySnapshot> entitySnapshots);

    @Nullable
    default SpawnEntityEvent createEntityEvent(Cause cause, CauseTracker causeTracker) {
        World world = causeTracker.getWorld();
        List<Entity> capturedEntities = causeTracker.getCapturedEntities();
        List<Transaction<BlockSnapshot>> invalidTransactions = causeTracker.getInvalidTransactions();
        final Tuple<List<EntitySnapshot>, Cause> listCauseTuple =
                TrackingHelper.processSnapshotsForSpawning(cause, world, capturedEntities, invalidTransactions);
        List<EntitySnapshot> entitySnapshots = listCauseTuple.getFirst();
        cause = listCauseTuple.getSecond();
        if (entitySnapshots.isEmpty()) {
            return null;
        }
        return createEventPostPrcess(cause, causeTracker, entitySnapshots);
    }
}
