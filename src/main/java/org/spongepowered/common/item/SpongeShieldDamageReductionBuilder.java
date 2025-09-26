package org.spongepowered.common.item;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.ShieldDamageReduction;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.common.bridge.tags.TagBridge;
import org.spongepowered.common.util.Preconditions;

import java.util.Optional;
import java.util.Set;

public class SpongeShieldDamageReductionBuilder implements ShieldDamageReduction.Builder {
    private HolderSet<net.minecraft.world.damagesource.DamageType> damageTypes;
    private Double horizontalBlockingAngle;
    private double base = 0;
    private double factor = 0;

    @Override
    public ShieldDamageReduction.Builder damageTypes(Set<DamageType> damageTypes) {
        final Registry<net.minecraft.world.damagesource.DamageType> registry = (Registry<net.minecraft.world.damagesource.DamageType>) Sponge.server().registry(RegistryTypes.DAMAGE_TYPE);

        this.damageTypes = HolderSet.direct(damageTypes.stream()
            .map(dt -> registry.wrapAsHolder((net.minecraft.world.damagesource.DamageType) (Object) dt))
            .toList());

        return this;
    }

    @Override
    public ShieldDamageReduction.Builder damageTypes(Tag<DamageType> tag) {
        final Registry<net.minecraft.world.damagesource.DamageType> registry = (Registry<net.minecraft.world.damagesource.DamageType>) Sponge.server().registry(RegistryTypes.DAMAGE_TYPE);
        final var vanillaTag = ((TagBridge<net.minecraft.world.damagesource.DamageType>) tag).bridge$asVanillaTag();
        this.damageTypes = registry.getOrThrow(vanillaTag);

        return this;
    }

    @Override
    public ShieldDamageReduction.Builder horizontalBlockingAngle(double angle) {
        Preconditions.checkArgument(angle > 0, "angle must be positive");
        this.horizontalBlockingAngle = angle;

        return this;
    }

    @Override
    public ShieldDamageReduction.Builder constantReduction(double constant) {
        this.base = constant;

        return this;
    }

    @Override
    public ShieldDamageReduction.Builder fractionalReduction(double fraction) {
        this.factor = fraction;

        return this;
    }

    @Override
    public ShieldDamageReduction build() {
        return (ShieldDamageReduction) (Object) new BlocksAttacks.DamageReduction(
            horizontalBlockingAngle != null ? horizontalBlockingAngle.floatValue() : 90,
            Optional.ofNullable(damageTypes),
            (float) base,
            (float) factor
        );
    }

    @Override
    public ShieldDamageReduction.Builder reset() {
        this.damageTypes = null;
        this.horizontalBlockingAngle = null;
        this.base = 0;
        this.factor = 0;

        return this;
    }

}
