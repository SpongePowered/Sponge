package org.spongepowered.common.mixin.core.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemChorusFruit;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemChorusFruit.class)
public class MixinItemChorusFruit extends Item {

    @Redirect(method = "onItemUseFinish", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;attemptTeleport(DDD)Z"))
    private boolean onAttemptTeleport(EntityLivingBase entity, double x, double y, double z) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.TELEPORT_TYPE, TeleportTypes.CHORUS_FRUIT);
            return entity.attemptTeleport(x, y, z);
        }
    }

}
