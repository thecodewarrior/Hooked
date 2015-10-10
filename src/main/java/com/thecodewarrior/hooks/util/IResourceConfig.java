package com.thecodewarrior.hooks.util;

import java.util.Map;

import net.minecraft.util.ResourceLocation;

public interface IResourceConfig
{
	public ResourceLocation getConfigLocation();
	public void processConfig(Map<String, String> data);
}
