package org.spongepowered.common.mixin.core.util.text;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import net.kyori.adventure.text.Component;
import net.minecraft.util.text.ITextComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.adventure.AdventureTextComponent;
import org.spongepowered.common.adventure.SpongeAdventure;

import java.lang.reflect.Type;

@Mixin(ITextComponent.Serializer.class)
public abstract class ITextComponentSerializerMixin {
    @Shadow public abstract JsonElement shadow$serialize(final ITextComponent text, final Type type, final JsonSerializationContext jsonSerializationContext);

    @Shadow @Final private static Gson GSON;

    @Inject(method = "serialize", at = @At("HEAD"), cancellable = true)
    public void impl$writeComponentText(final ITextComponent text, final Type type, final JsonSerializationContext ctx,
            final CallbackInfoReturnable<JsonElement> cir) {
        if(text instanceof AdventureTextComponent) {
            final @Nullable ITextComponent converted = ((AdventureTextComponent) text).deepConvertedIfPresent();
            if(converted != null) {
                cir.setReturnValue(this.shadow$serialize(text, type, ctx));
            } else {
                cir.setReturnValue(ctx.serialize(((AdventureTextComponent) text).wrapped(), Component.class));
            }
        }
    }

    // inject into the anonymous function to build a gson instance
    @Inject(method = "*()Lcom/google/gson/Gson;", at = @At(value = "INVOKE_ASSIGN", target = "com/google/gson/GsonBuilder.disableHtmlEscaping()Lcom/google/gson/GsonBuilder;", remap = false),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION, remap = false)
    private static void impl$injectAdventureGson(final CallbackInfoReturnable<Gson> cir, final GsonBuilder gson) {
        SpongeAdventure.GSON.populator().apply(gson);
    }

    @Inject(method = "toJson", at = @At("HEAD"), cancellable = true)
    private static void impl$redirectSerialization(final ITextComponent component, final CallbackInfoReturnable<String> cir) {
        if (component instanceof AdventureTextComponent) {
            cir.setReturnValue(GSON.toJson(((AdventureTextComponent) component).wrapped()));
        }
    }

    @Inject(method = "toJsonTree", at = @At("HEAD"), cancellable = true)
    private static void impl$redirectTreeSerialization(final ITextComponent component, final CallbackInfoReturnable<JsonElement> cir) {
        if (component instanceof AdventureTextComponent) {
            cir.setReturnValue(GSON.toJsonTree(((AdventureTextComponent) component).wrapped()));
        }
    }

}
