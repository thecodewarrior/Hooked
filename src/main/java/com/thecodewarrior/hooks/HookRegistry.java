package com.thecodewarrior.hooks;

import java.util.HashMap;
import java.util.Map;

import com.thecodewarrior.hooks.render.HookRenderer;
import com.thecodewarrior.hooks.render.model.IChainModel;
import com.thecodewarrior.hooks.render.model.IHookModel;
import com.thecodewarrior.hooks.render.model.IParticleSpawner;
import com.thecodewarrior.hooks.util.Hook;

public class HookRegistry
{
	private static Map<String, Hook> hooks = new HashMap<String, Hook>();
	public static void registerHook(String id, Hook hook)
	{
		hooks.put(id, hook);
	}
	public static Hook getHook(String id)
	{
		return hooks.get(id);
	}
	
	// ------------------------------------------------------------------------
//	public static HookRenderer basicRenderer;
//	private static Map<String, IHookRenderer> renderers = new HashMap<String, IHookRenderer>();
//	public static void registerRenderer(String id, IHookRenderer hook)
//	{
//		renderers.put(id, hook);
//	}
//	public static IHookRenderer getRenderer(String id)
//	{
//		IHookRenderer renderer = renderers.get(id);
//		if(renderer == null)
//			return basicRenderer;
//		return renderer;
//	}
	
	// ------------------------------------------------------------------------
	public static Class<? extends IHookModel> basicHookModel;
	private static Map<String, Class<? extends IHookModel>> hookModels = new HashMap<String, Class<? extends IHookModel>>();
	public static void registerHookModel(String id, Class<? extends IHookModel> hook)
	{
		hookModels.put(id, hook);
	}
	public static Class<? extends IHookModel> getHookModel(String id)
	{
		Class<? extends IHookModel> model = hookModels.get(id);
		if(model == null)
			return basicHookModel;
		return model;
	}
	
	// ------------------------------------------------------------------------
	public static Class<? extends IChainModel> basicChainModel;
	private static Map<String, Class<? extends IChainModel>> chainModels = new HashMap<String, Class<? extends IChainModel>>();
	public static void registerChainModel(String id, Class<? extends IChainModel> hook)
	{
		chainModels.put(id, hook);
	}
	public static Class<? extends IChainModel> getChainModel(String id)
	{
		Class<? extends IChainModel> model = chainModels.get(id);
		if(model == null)
			return basicChainModel;
		return model;
	}
	
	// ------------------------------------------------------------------------
	public static Class<? extends IParticleSpawner> basicParticleSpawner;
	private static Map<String, Class<? extends IParticleSpawner>> particleSpawners = new HashMap<String, Class<? extends IParticleSpawner>>();
	public static void registerParticleSpawner(String id, Class<? extends IParticleSpawner> hook)
	{
		particleSpawners.put(id, hook);
	}
	public static Class<? extends IParticleSpawner> getParticleSpawner(String id)
	{
		Class<? extends IParticleSpawner> model = particleSpawners.get(id);
		if(model == null)
			return basicParticleSpawner;
		return model;
	}
}
