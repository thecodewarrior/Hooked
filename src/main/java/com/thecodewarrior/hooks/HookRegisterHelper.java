package com.thecodewarrior.hooks;

import com.thecodewarrior.hooks.item.ItemHook;
import com.thecodewarrior.hooks.render.HookRenderer;
import com.thecodewarrior.hooks.util.Hook;
import com.thecodewarrior.hooks.util.HookStats;

public class HookRegisterHelper
{
	private String name;
	private String id;
	private String domain;
	
	private double length = -1;
	private double speed = -1;
	private double retractSpeed = -1;
	
	private int count = 1;
	private int durability = 0;
	
	private boolean multi = false;
	
	public void reset(String hookName) {
		name = null;
		id = null;
		domain = null;
		
		length = -1;
		speed = -1;
		retractSpeed = -1;
		
		durability = 0;
		count = 1;
		
		multi = false;
		
		name = hookName;
		id = hookName.toUpperCase().charAt(0) + hookName.substring(1); // capitalize the first letter
	}
	
	boolean isComplete() {
		if(
				name == null ||
				id == null ||
				speed == -1 ||
				length == -1 ||
				retractSpeed == -1 ||
				durability == 0
		) { return false; }
		return true;
	}
	
	public void register() {
		
		if(!isComplete()) {
			RuntimeException e = new RuntimeException("Tried to register incomplete hook: " + ( name == null ? "NULL" : name ));
			throw e;
		}
		String texLoc = ( domain == null ? "" : domain + "/" ) + name;
		IHookRenderer renderer = new HookRenderer(texLoc);
		
		HookStats stats = new HookStats(length, speed, retractSpeed);
		
		Hook hook = new Hook(HookMod.MODID + ":" + name, stats );
		hook.setRenderer(renderer);
		
		ItemHook item = new ItemHook(hook);
		item.setDomain(domain);
		item.setDurability(durability);
		item.setCount(count);
		item.setMulti(multi);
		item.register("hook"+id);
		
		HookMod.items	 .put(name, item );
		HookMod.renderers.put(name, renderer);
		HookMod.hooks	 .put(name, hook );
	}
	
	// --------- Setters ----------
	
	public HookRegisterHelper setName(String name) {
		this.name = name;
		return this;
	}
	

	public HookRegisterHelper setDomain(String domain)
	{
		this.domain = domain;
		return this;
	}
	
	public HookRegisterHelper setId(String id) {
		this.id = id;
		return this;
	}
	
	public HookRegisterHelper setLength(double length) {
		this.length = length;
		return this;
	}
	
	public HookRegisterHelper setSpeed(double speed) {
		this.speed = speed;
		return this;
	}
	
	public HookRegisterHelper setRetractSpeed(double retractSpeed) {
		this.retractSpeed = retractSpeed;
		return this;
	}
	
	public HookRegisterHelper setCount(int count) {
		this.count = count;
		return this;
	}
	
	public HookRegisterHelper setDurability(int durability) {
		this.durability = durability;
		return this;
	}
	public HookRegisterHelper setUnbreakable() {
		this.durability = -1;
		return this;
	}
	
	public HookRegisterHelper setMulti(boolean multi) {
		this.multi = multi;
		return this;
	}
	
	public HookRegisterHelper setMulti() {
		this.multi = true;
		return this;
	}
	
	public HookRegisterHelper setSingle() {
		this.multi = false;
		return this;
	}
}
