package org.spongepowered.common.mixin.core.entity.passive;

import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.ParrotData;
import org.spongepowered.api.data.type.ParrotVariant;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.animal.Parrot;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeParrotData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSittingData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.SpongeEntityConstants;

import java.util.List;
import java.util.Random;

@Mixin(EntityParrot.class)
public abstract class MixinEntityParrot extends MixinEntityTameable implements Parrot {

    @Shadow public abstract int getVariant();

    @Redirect(method = "processInteract", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0, remap = false))
    public int onTame(Random rand, int bound, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        int random = rand.nextInt(bound);
        if (random == 0) {
            stack.setCount(stack.getCount() + 1);
            try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                Sponge.getCauseStackManager().pushCause(player);
                if (!SpongeImpl.postEvent(SpongeEventFactory.createTameEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), this))) {
                    stack.setCount(stack.getCount() - 1);
                    return random;
                }
            }
        }
        return 1;
    }

    @Override
    public ParrotData getParrotData() {
        return new SpongeParrotData(SpongeEntityConstants.PARROT_VARIANT_IDMAP.get(this.getVariant()));
    }

    @Override
    public Value<ParrotVariant> variant() {
        return new SpongeValue<>(Keys.PARROT_VARIANT, DataConstants.Parrot.DEFAULT_VARIANT, SpongeEntityConstants.PARROT_VARIANT_IDMAP.get(this.getVariant()));
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(new SpongeSittingData(this.shadow$isSitting()));
        manipulators.add(getParrotData());
    }

}
