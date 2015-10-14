package com.thecodewarrior.hooks.render.model;

import java.util.Map;

import com.thecodewarrior.hooks.util.ActiveHook;

public interface IHookModel
{
	public void constructTexturesFor(String hookName);
	public void processConfig(Map<String, String> data);
	
	public void draw(ActiveHook hook);
	
	public double getLoopCenterPointDistance(ActiveHook hook);
}
