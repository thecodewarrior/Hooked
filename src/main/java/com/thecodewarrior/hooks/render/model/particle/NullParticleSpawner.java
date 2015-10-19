package com.thecodewarrior.hooks.render.model.particle;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.render.model.IParticleSpawner;
import com.thecodewarrior.hooks.util.ActiveHook;

public class NullParticleSpawner implements IParticleSpawner
{
	
	@Override
	public void constructTexturesFor(String hookName) {}
	
	@Override
	public void processConfig(Map<String, String> data) {}
	
	@Override
	public void spawnParticles(EntityPlayer player, ActiveHook hook, Vector3 adjustedChainEnd) {}
	
}
