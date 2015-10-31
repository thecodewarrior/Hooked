package com.thecodewarrior.hooks.util;

import com.thecodewarrior.hooks.IHookRenderer;

public class HookStats
{
	
	double length, speed, retractSpeed, pullStrength, flingBoost;

	public HookStats(double length, double speed, double retractSpeed)
	{
		this.length = length;
		this.speed = speed/20f;
		this.retractSpeed = retractSpeed/20f;
	}
	
	public HookStats() {}
	
	public double getLength()
	{
		return length;
	}
	public HookStats setLength(double length)
	{
		this.length = length;
		return this;
	}
	
	public double getSpeed()
	{
		return speed;
	}
	public HookStats setSpeed(double speed)
	{
		this.speed = speed/20f;
		return this;
	}
	
	public double getRetractSpeed()
	{
		return retractSpeed;
	}
	public HookStats setRetractSpeed(double retractSpeed)
	{
		this.retractSpeed = retractSpeed/20f;
		return this;
	}

	public double getPullStrength()
	{
		return pullStrength;
	}
	public HookStats setPullStrength(double pullStrength)
	{
		this.pullStrength = pullStrength;
		return this;
	}

	public double getFlingBoost()
	{
		return flingBoost;
	}
	public HookStats setFlingBoost(double flingBoost)
	{
		this.flingBoost = flingBoost;
		return this;
	}
	
	
}
