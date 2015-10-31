package com.thecodewarrior.hooks.util;

import com.thecodewarrior.hooks.IHookRenderer;

public class Hook {
	
	String id;
	HookStats stats;
	IHookRenderer renderer;
	public HookStats getStats()
	{
		return stats;
	}
	
	public Hook(String id, HookStats stats) {
		this.id = id;
		this.stats = stats;
	}
	
	public double getSpeed()
	{
		return stats.getSpeed();
	}
	public double getRetractSpeed()
	{
		return stats.getRetractSpeed();
	}
	public double getPullStrength()
	{
		return stats.getPullStrength();
	}
	public double getFlingBoost()
	{
		return stats.getFlingBoost();
	}
	public double getLength()
	{
		return stats.length;
	}
	public String getId()
	{
		return id;
	}
	public void setRenderer(IHookRenderer renderer)
	{
		this.renderer = renderer;
	}
	public IHookRenderer getRenderer() {
		return renderer;
	}
}
