package org.spongepowered.common.accessor.world.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickNextTickData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

@Mixin(ServerTickList.class)
public interface ServerTickListAccessor<T> {

    @Accessor("alreadyTicked") List<TickNextTickData<T>> accessor$alreadyTicked();

    @Accessor("tickNextTickList") TreeSet<TickNextTickData<T>> accessor$tickNextTickList();

    @Accessor("tickNextTickSet") Set<TickNextTickData<T>> accessor$tickNextTickSet();

    @Accessor("currentlyTicking") Queue<TickNextTickData<T>> accessor$currentlyTicking();

    @Accessor("level") ServerLevel accessor$level();
}
