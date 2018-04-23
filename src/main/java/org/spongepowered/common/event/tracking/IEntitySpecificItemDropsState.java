package org.spongepowered.common.event.tracking;

public interface IEntitySpecificItemDropsState<C extends PhaseContext<C>> extends IPhaseState<C> {

    @Override
    default boolean tracksEntitySpecificDrops() {
        return true;
    }


}
