package org.spongepowered.common.event.tracking.phase.entity;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.Optional;
import java.util.function.Supplier;

public class EntityDeathContext extends EntityContext<EntityDeathContext> {
    private DamageSource damageSource;

    EntityDeathContext(
        IPhaseState<? extends EntityDeathContext> state) {
        super(state);
    }

    /**
     * Double checks the last state on the stack to verify that the drop phase
     * is not needing to be cleaned up. Since the state is only entered during
     * drops, it needs to be properly cleaned up from the stack.
     */
    @Override
    public void close() {
        PhaseData currentPhaseData = PhaseTracker.getInstance().getCurrentPhaseData();
        // The current phase data may not be this phase data, which would ultimately
        // lead to having to close off another on top of this one.
        if (!currentPhaseData.context.equals(this)) {
            // at most we should be at a depth of 1.
            // should attempt to complete the existing one to wrap up, before exiting fully.
            PhaseTracker.getInstance().completePhase(currentPhaseData.state);
        }
        super.close();
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }

    public EntityDeathContext setDamageSource(DamageSource damageSource) {
        this.damageSource = damageSource;
        return this;
    }

    @Override
    public PrettyPrinter printCustom(PrettyPrinter printer) {
        return super.printCustom(printer)
            .add("    - %s: %s", "DamageSource", this.damageSource);
    }

    @Override
    public EntityDeathContext source(Object owner) {
        return (EntityDeathContext) super.source(owner);
    }

    @Override
    public EntityDeathContext owner(Supplier<Optional<User>> supplier) {
        return (EntityDeathContext) super.owner(supplier);
    }

    @Override
    public EntityDeathContext owner(User owner) {
        return (EntityDeathContext) super.owner(owner);
    }

    @Override
    public EntityDeathContext notifier(Supplier<Optional<User>> supplier) {
        return (EntityDeathContext) super.notifier(supplier);
    }

    @Override
    public EntityDeathContext notifier(User notifier) {
        return (EntityDeathContext) super.notifier(notifier);
    }

    @Override
    public EntityDeathContext addBlockCaptures() {
        return (EntityDeathContext) super.addBlockCaptures();
    }

    @Override
    public EntityDeathContext addCaptures() {
        return (EntityDeathContext) super.addCaptures();
    }

    @Override
    public EntityDeathContext addEntityCaptures() {
        return (EntityDeathContext) super.addEntityCaptures();
    }

    @Override
    public EntityDeathContext addEntityDropCaptures() {
        return (EntityDeathContext) super.addEntityDropCaptures();
    }

    @Override
    public EntityDeathContext buildAndSwitch() {
        return (EntityDeathContext) super.buildAndSwitch();
    }

    @Override
    public EntityDeathContext markEmpty() {
        return (EntityDeathContext) super.markEmpty();
    }
}
