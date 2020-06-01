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
package org.spongepowered.common.mixin.api.mcp.map;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.MapDecoration;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.map.MapUtil;
import org.spongepowered.common.map.decoration.SpongeMapDecoration;
import org.spongepowered.common.registry.type.map.MapDecorationRegistryModule;
import org.spongepowered.common.util.Constants;

import java.io.IOException;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;

@Mixin(MapDecoration.class)
public class MapDecorationMixin_API implements SpongeMapDecoration {
    @Shadow @Final private MapDecoration.Type type;
    @Shadow private byte x;
    @Shadow private byte y;

    @Shadow private byte rotation;
    // If should save to disk
    private boolean isPersistent;
    //
    private String key = Constants.Map.DECORATION_KEY_PREFIX + UUID.randomUUID().toString();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initialiser(MapDecoration.Type typeIn, byte xIn, byte yIn, byte rotationIn, CallbackInfo ci) {
        // All of the below types have no reason to be saved to disk
        // This is because they can/should be calculated when needed
        // Furthermore if a sponge plugin adds a MapDecoration, isPersistent
        // should also be changed to true
        switch (typeIn) {
            case PLAYER:
            case PLAYER_OFF_MAP:
            case PLAYER_OFF_LIMITS:
            case FRAME: {
                this.isPersistent = false;
                break;
            }
            default: {
                this.isPersistent = true;
                break;
            }

        }
    }

    @Override
    public MapDecorationType getType() {
        return MapDecorationRegistryModule.getByMcType(type)
                .orElseThrow(() -> new IllegalStateException("Tried to get MapDecoration type but it didn't exist in Sponge's registries! Have MC Decoration types been missed?"));
    }

    @Override
    public Vector2i getPosition() {
        return new Vector2i(this.x, this.y);
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public void setPosition(Vector2i position) {
        checkState(MapUtil.isInBounds(position.getX()), "x position out of bounds");
        checkState(MapUtil.isInBounds(position.getY()), "y position out of bounds");
        this.x = (byte) position.getX();
        this.y = (byte) position.getY();
    }

    @Override
    public void setPosition(Vector3i position) {
        this.x = (byte) position.getX();
        this.y = (byte) position.getZ();
    }

    @Override
    public void setX(int x) {
        checkState(MapUtil.isInBounds(x), "x out of bounds");
        this.x = (byte) x;
    }

    @Override
    public void setY(int y) {
        checkState(MapUtil.isInBounds(y), "y out of bounds");
        this.y = (byte)y;
    }

    @Override
    public void setRotation(int rot) {
        this.rotation = (byte)rot;
    }

    @Override
    public int getRotation() {
        return 0;
    }

    @Override
    public void setPersistent(boolean persistent) {
        this.isPersistent = persistent;
    }

    @Override
    public boolean isPersistent() {
        return this.isPersistent;
    }

    @Override
    public NBTTagCompound getMCNBT() {
        try {
            return net.minecraft.nbt.JsonToNBT.getTagFromJson(DataFormats.JSON.write(this.toContainer()));
        } catch (NBTException | IOException e) {
            // I don't see this ever happening but lets put logging in anyway
            throw new IllegalStateException("Error converting DataView to MC NBT", e);
        }
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Constants.Map.DECORATION_TYPE, this.type.getIcon())
                .set(Constants.Map.DECORATION_ID, this.key)
                .set(Constants.Map.DECORATION_X, this.x)
                .set(Constants.Map.DECORATION_Y, this.y)
                .set(Constants.Map.DECORATION_ROTATION, this.rotation);
    }
}
