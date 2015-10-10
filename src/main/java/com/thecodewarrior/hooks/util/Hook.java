package com.thecodewarrior.hooks.util;

public class Hook {
	
	String id;
	HookStats stats;
	
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
	public double getLength()
	{
		return stats.length;
	}
	public String getId()
	{
		return id;
	}
}
