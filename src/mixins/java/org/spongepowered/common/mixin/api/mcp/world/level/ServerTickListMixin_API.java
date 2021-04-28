package org.spongepowered.common.mixin.api.mcp.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.TickPriority;
import org.spongepowered.api.Engine;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.api.scheduler.ScheduledUpdateList;
import org.spongepowered.api.scheduler.TaskPriority;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.level.TickNextTickDataBridge;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mixin(ServerTickList.class)
public abstract class ServerTickListMixin_API<T> implements ScheduledUpdateList<T> {

    @Shadow @Final protected Predicate<T> ignore;
    @Shadow @Final private ServerLevel level;
    @Shadow @Final private Queue<TickNextTickData<T>> currentlyTicking;
    @Shadow @Final private Set<TickNextTickData<T>> tickNextTickSet;

    @Shadow public abstract boolean shadow$hasScheduledTick(BlockPos param0, T param1);
    @Shadow protected abstract void shadow$addTickData(TickNextTickData<T> data);

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public ScheduledUpdate<T> schedule(
        int x, int y, int z, T target, Duration delay, TaskPriority priority
    ) {
        final long tickDelay = Ticks.ofWallClockSeconds((Engine) this.level.getServer(), (int) delay.getSeconds()).ticks();
        final TickNextTickData<T> scheduledUpdate = new TickNextTickData<>(new BlockPos(x, y, z), target, tickDelay + this.level.getGameTime(), (TickPriority) (Object) priority);
        if (!this.ignore.test(target)) {
            ((TickNextTickDataBridge<T>) scheduledUpdate).bridge$createdByList((ServerTickList<T>) (Object) this);
            this.shadow$addTickData(scheduledUpdate);
        }
        return (ScheduledUpdate<T>) scheduledUpdate;
    }

    @Override
    public boolean isScheduled(int x, int y, int z, T target) {
        return this.shadow$hasScheduledTick(new BlockPos(x, y, z), target);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<? extends ScheduledUpdate<T>> scheduledAt(int x, int y, int z) {
        if (!this.currentlyTicking.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableCollection(this.tickNextTickSet.stream()
            .filter(data -> data.pos.getX() == x && data.pos.getZ() == z && data.pos.getY() == y)
            .map(data -> (ScheduledUpdate<T>) data)
            .collect(Collectors.toList()));
    }
}
