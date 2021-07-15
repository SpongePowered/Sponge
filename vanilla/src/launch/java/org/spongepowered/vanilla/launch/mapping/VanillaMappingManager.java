package org.spongepowered.vanilla.launch.mapping;

import org.spongepowered.common.launch.mapping.SpongeMappingManager;

public class VanillaMappingManager implements SpongeMappingManager {
	@Override
	public String toRuntimeClassName(String srcName) {
		return srcName;
	}

	@Override
	public String toRuntimeFieldName(Class<?> owner, String srcName) {
		return srcName;
	}

	@Override
	public String toRuntimeMethodName(Class<?> owner, String srcName, Class<?>... params) {
		return srcName;
	}
}
