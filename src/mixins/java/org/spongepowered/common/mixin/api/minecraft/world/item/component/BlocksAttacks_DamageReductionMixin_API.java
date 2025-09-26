package org.spongepowered.common.mixin.api.minecraft.world.item.component;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.api.data.type.ShieldDamageReduction;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.asm.mixin.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(BlocksAttacks.DamageReduction.class)
@Implements(@Interface(iface = ShieldDamageReduction.class, prefix = "shielddamagereduction$"))
public abstract class BlocksAttacks_DamageReductionMixin_API implements ShieldDamageReduction {

    @Shadow @Final private Optional<HolderSet<net.minecraft.world.damagesource.DamageType>> type;
    @Shadow @Final private float base;
    @Shadow @Final private float factor;
    @Shadow @Final private float horizontalBlockingAngle;

    @Override
    public Optional<Set<DamageType>> damageTypes() {
        return this.type.map(set -> set.stream()
            .map(Holder::value)
            .map(DamageType.class::cast)
            .collect(Collectors.toSet()));
    }

    public double shielddamagereduction$horizontalBlockingAngle() {
        return this.horizontalBlockingAngle;
    }

    @Override
    public double constantReduction() {
        return this.base;
    }

    @Override
    public double fractionalReduction() {
        return this.factor;
    }

}
