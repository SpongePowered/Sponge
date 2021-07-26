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
package org.spongepowered.fabric.launch.mapping;

import org.spongepowered.common.launch.mapping.SpongeMappingManager;
import org.spongepowered.common.util.MissingImplementationException;

public class FabricMappingManager implements SpongeMappingManager {
	@Override
	public String toRuntimeClassName(String srcName) {
		throw new MissingImplementationException(this.getClass().getName(), "toRuntimeClassName");
	}

	@Override
	public String toRuntimeFieldName(Class<?> owner, String srcName) {
		throw new MissingImplementationException(this.getClass().getName(), "toRuntimeFieldName");
	}

	@Override
	public String toRuntimeMethodName(Class<?> owner, String srcName, Class<?>... params) {
		// TODO: properly implement this method
		switch (srcName) {
			case "neighborChanged":
				return "method_9612";
			case "entityInside":
				return "method_9548";
			case "stepOn":
				return "method_9591";
			default:
				throw new MissingImplementationException(this.getClass().getName(), "toRuntimeMethodName");
		}
	}
}
