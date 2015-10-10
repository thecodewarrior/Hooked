package com.thecodewarrior.hooks;

import java.util.HashMap;
import java.util.Map;

import com.thecodewarrior.hooks.util.Hook;

public class HookRegistry
{
	private static Map<String, Hook> hooks = new HashMap<String, Hook>();
	private static Map<String, IHookRenderer> renderers = new HashMap<String, IHookRenderer>();
	
	public static void registerHook(String id, Hook hook)
	{
		hooks.put(id, hook);
	}
	public static Hook getHook(String id)
	{
		return hooks.get(id);
	}
	
	public static void registerRenderer(String id, IHookRenderer hook)
	{
		renderers.put(id, hook);
	}
	public static IHookRenderer getRenderer(String id)
	{
		IHookRenderer renderer = renderers.get(id);
		if(renderer == null)
			return HookMod.basicRenderer;
		return renderer;
	}
}
