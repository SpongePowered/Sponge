package org.spongepowered.common.mixin.core.entity.ai.attributes;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import org.spongepowered.api.entity.attribute.Attribute;
import org.spongepowered.api.entity.attribute.AttributeModifier;
import org.spongepowered.api.entity.attribute.operation.AttributeOperation;
import org.spongepowered.api.entity.attribute.type.AttributeType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.entity.attribute.operation.SpongeAttributeOperation;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Implements(@Interface(iface = Attribute.class, prefix = "api$"))
@Mixin(IAttributeInstance.class)
public interface MixinIAttributeInstance extends Attribute {

    @Shadow IAttribute shadow$getAttribute();
    @Shadow double shadow$getBaseValue();
    @Shadow void shadow$setBaseValue(double baseValue);
    @Shadow Collection<net.minecraft.entity.ai.attributes.AttributeModifier> shadow$getModifiersByOperation(int operation);
    @Shadow Collection<net.minecraft.entity.ai.attributes.AttributeModifier> shadow$getModifiers();
    @Shadow boolean shadow$hasModifier(net.minecraft.entity.ai.attributes.AttributeModifier modifier);
    @Nullable @Shadow net.minecraft.entity.ai.attributes.AttributeModifier shadow$getModifier(UUID uuid);
    @Shadow void shadow$applyModifier(net.minecraft.entity.ai.attributes.AttributeModifier modifier);
    @Shadow void shadow$removeModifier(net.minecraft.entity.ai.attributes.AttributeModifier modifier);
    @Shadow double shadow$getAttributeValue();

    @Override
    default AttributeType getType() {
        return (AttributeType) this.shadow$getAttribute();
    }

    @Intrinsic
    default double api$getBaseValue() {
        return this.shadow$getBaseValue();
    }

    @Intrinsic
    default void api$setBaseValue(double baseValue) {
        this.shadow$setBaseValue(baseValue);
    }

    @Override
    default double getValue() {
        return this.shadow$getAttributeValue();
    }

    @Override
    default Collection<AttributeModifier> getModifiers() {
        return (Collection<AttributeModifier>) (Object) this.shadow$getModifiers();
    }

    @Override
    default Collection<AttributeModifier> getModifiers(AttributeOperation operation) {
        return (Collection<AttributeModifier>) (Object) this.shadow$getModifiersByOperation(((SpongeAttributeOperation) operation).getOpcode());
    }

    @Override
    default boolean hasModifier(AttributeModifier modifier) {
        return this.shadow$hasModifier((net.minecraft.entity.ai.attributes.AttributeModifier) modifier);
    }

    @Override
    default Optional<AttributeModifier> getModifier(UUID uniqueId) {
        return Optional.ofNullable((AttributeModifier) this.shadow$getModifier(uniqueId));
    }

    @Override
    default void addModifier(AttributeModifier modifier) {
        this.shadow$applyModifier((net.minecraft.entity.ai.attributes.AttributeModifier) modifier);
    }

    @Override
    default void removeModifier(AttributeModifier modifier) {
        this.shadow$removeModifier((net.minecraft.entity.ai.attributes.AttributeModifier) modifier);
    }

}
