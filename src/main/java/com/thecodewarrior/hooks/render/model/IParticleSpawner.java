package com.thecodewarrior.hooks.render.model;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.util.ActiveHook;

public interface IParticleSpawner
{
	public void constructTexturesFor(String hookName);
	public void processConfig(Map<String, String> data);
	
	public void spawnParticles(EntityPlayer player, ActiveHook hook, Vector3 adjustedChainEnd);
}
