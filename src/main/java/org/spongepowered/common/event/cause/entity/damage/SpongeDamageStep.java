/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.event.cause.entity.damage;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageStep;
import org.spongepowered.api.event.cause.entity.damage.DamageStepHistory;
import org.spongepowered.api.event.cause.entity.damage.DamageStepType;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

public final class SpongeDamageStep implements DamageStep {
    private static final Logger LOGGER = LogManager.getLogger();

    private final SpongeDamageTracker tracker;
    private final DamageStepType type;
    private final @Nullable SpongeDamageStep parent;
    private final @Nullable Consumer<CauseStackManager.StackFrame> frameModifier;
    private final DamageModifier.@Nullable Function damageFunction;

    private Cause cause;
    private boolean skipped;

    private OptionalDouble damageBeforeChildren = OptionalDouble.empty();
    private OptionalDouble damageBeforeSelf = OptionalDouble.empty();
    private OptionalDouble damageAfterSelf = OptionalDouble.empty();
    private OptionalDouble damageAfterChildren = OptionalDouble.empty();

    private List<SpongeDamageStep> childrenBefore;
    private List<SpongeDamageStep> childrenAfter;

    public SpongeDamageStep(final SpongeDamageTracker tracker, final DamageStepType type, final Object[] causes) {
        this.tracker = tracker;
        this.parent = null;
        this.type = type;
        this.frameModifier = (frame) -> {
            SpongeDamageTracker.generateCauseFor(tracker.source, frame);
            for (Object cause : causes) {
                frame.pushCause(cause);
            }
        };
        this.damageFunction = null;
    }

    public SpongeDamageStep(final SpongeDamageStep parent, final DamageModifier modifier) {
        this.tracker = parent.tracker;
        this.parent = parent;
        this.type = modifier.type();
        this.frameModifier = modifier.frameModifier().orElse(null);
        this.damageFunction = modifier.damageFunction().orElse(null);
    }

    void populateChildren() {
        this.populateChildren(new HashSet<>());
    }

    private void populateChildren(final Set<DamageStepType> parentTypes) {
        parentTypes.add(this.type);

        final ImmutableList.Builder<SpongeDamageStep> before = ImmutableList.builder();
        for (final DamageModifier modifier : this.tracker.preEvent().modifiersBefore(this.type)) {
            if (parentTypes.contains(modifier.type())) {
                LOGGER.warn("Modifier {} is supposed to be a child before step {} but this would cause a cycle so it will be ignored.", modifier, this);
            } else {
                before.add(new SpongeDamageStep(this, modifier));
            }
        }
        this.childrenBefore = before.build();

        final ImmutableList.Builder<SpongeDamageStep> after = ImmutableList.builder();
        for (final DamageModifier modifier : this.tracker.preEvent().modifiersAfter(this.type)) {
            if (parentTypes.contains(modifier.type())) {
                LOGGER.warn("Modifier {} is supposed to be a child after step {} but this would cause a cycle so it will be ignored.", modifier, this);
            } else {
                after.add(new SpongeDamageStep(this, modifier));
            }
        }
        this.childrenAfter = after.build();

        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        try (final CauseStackManager.StackFrame frame = this.frameModifier == null ? null : phaseTracker.pushCauseFrame()) {
            if (frame != null) {
                try {
                    this.frameModifier.accept(frame);
                } catch (final Throwable t) {
                    LOGGER.error("Failed to apply frame modifier of step {}", this, t);
                }
            }

            this.cause = phaseTracker.currentCause();

            for (final SpongeDamageStep child : this.childrenBefore) {
                child.populateChildren(parentTypes);
            }
            for (final SpongeDamageStep child : this.childrenAfter) {
                child.populateChildren(parentTypes);
            }
        }

        parentTypes.remove(this.type);
    }

    @Override
    public DamageStepType type() {
        return this.type;
    }

    @Override
    public Cause cause() {
        return this.cause;
    }

    @Override
    public boolean isSkipped() {
        return this.skipped;
    }

    @Override
    public void setSkipped(boolean skipped) {
        if (this.damageAfterSelf.isPresent()) {
            throw new IllegalStateException("Step can only be skipped before occurring");
        }
        this.skipped = skipped;
    }

    @Override
    public OptionalDouble damageBeforeChildren() {
        return this.damageBeforeChildren;
    }

    @Override
    public OptionalDouble damageBeforeSelf() {
        return this.damageBeforeSelf;
    }

    @Override
    public OptionalDouble damageAfterSelf() {
        return this.damageAfterSelf;
    }

    @Override
    public OptionalDouble damageAfterChildren() {
        return this.damageAfterChildren;
    }

    @Override
    public DamageStepHistory history() {
        return this.tracker;
    }

    @Override
    public Optional<DamageStep> parent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public List<DamageStep> childrenBefore() {
        return (List) this.childrenBefore;
    }

    @Override
    public List<DamageStep> childrenAfter() {
        return (List) this.childrenAfter;
    }

    @Override
    public String toString() {
        // don't add the parent to avoid infinite recursion
        return new StringJoiner(", ", SpongeDamageStep.class.getSimpleName() + "[", "]")
            .add("type=" + this.type)
            .add("cause=" + this.cause)
            .add("frameModifier=" + this.frameModifier)
            .add("damageFunction=" + this.damageFunction)
            .add("skipped=" + this.skipped)
            .add("damageBeforeChildren=" + this.damageBeforeChildren)
            .add("damageBeforeSelf=" + this.damageBeforeSelf)
            .add("damageAfterSelf=" + this.damageAfterSelf)
            .add("damageAfterChildren=" + this.damageAfterChildren)
            .add("childrenBefore=" + this.childrenBefore)
            .add("childrenAfter=" + this.childrenAfter)
            .toString();
    }

    public double applyChildrenBefore(double damage) {
        if (this.damageBeforeChildren.isPresent()) {
            throw new IllegalStateException();
        }

        this.damageBeforeChildren = OptionalDouble.of(damage);
        for (final SpongeDamageStep child : this.childrenBefore) {
            damage = child.apply(damage);
        }
        this.damageBeforeSelf = OptionalDouble.of(damage);
        return damage;
    }

    public double applyChildrenAfter(double damage) {
        if (this.damageAfterSelf.isPresent() || this.damageBeforeSelf.isEmpty()) {
            throw new IllegalStateException();
        }

        if (this.skipped) {
            damage = this.damageBeforeSelf.getAsDouble();
        }

        this.damageAfterSelf = OptionalDouble.of(damage);
        for (final SpongeDamageStep child : this.childrenAfter) {
            damage = child.apply(damage);
        }
        this.damageAfterChildren = OptionalDouble.of(damage);
        return damage;
    }

    public double apply(double damage) {
        damage = this.applyChildrenBefore(damage);
        if (!this.skipped && this.damageFunction != null) {
            try {
                damage = this.damageFunction.modify(this, damage);
            } catch (final Throwable t) {
                LOGGER.error("Failed to apply damage function of step {}", this, t);
            }
        }
        damage = this.applyChildrenAfter(damage);
        return damage;
    }
}
