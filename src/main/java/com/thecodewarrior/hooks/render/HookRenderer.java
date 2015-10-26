package com.thecodewarrior.hooks.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.HookRegistry;
import com.thecodewarrior.hooks.IHookRenderer;
import com.thecodewarrior.hooks.proxy.CommonProxy.Axis;
import com.thecodewarrior.hooks.render.model.IChainModel;
import com.thecodewarrior.hooks.render.model.IHookModel;
import com.thecodewarrior.hooks.render.model.IParticleSpawner;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookUtil;
import com.thecodewarrior.hooks.util.IResourceConfig;

public class HookRenderer implements IHookRenderer, IResourceConfig
{

	ResourceLocation hookInfo;

	@Override
	public ResourceLocation getConfigLocation()
	{
		return hookInfo;
	}
	
	@Override
	public void processConfig(Map<String, String> data)
	{
		String hookModelName = data.get("hook.model");		
		String chainModelName = data.get("chain.model");
		String particleSpawnerName = data.get("particle.model");

		try
		{
			hookModel = HookRegistry.getHookModel(hookModelName).newInstance();
		}
		catch (InstantiationException e) { e.printStackTrace(); }
		catch (IllegalAccessException e) { e.printStackTrace(); }
		try
		{
			chainModel = HookRegistry.getChainModel(chainModelName).newInstance();
		}
		catch (InstantiationException e) { e.printStackTrace(); }
		catch (IllegalAccessException e) { e.printStackTrace(); }
		
		try
		{
			particleSpawner = HookRegistry.getParticleSpawner(particleSpawnerName).newInstance();
		}
		catch (InstantiationException e) { e.printStackTrace(); }
		catch (IllegalAccessException e) { e.printStackTrace(); }
		
		
		 hookModel.processConfig(data);
		 hookModel.constructTexturesFor(hookName);
		chainModel.processConfig(data);
		chainModel.constructTexturesFor(hookName);
		particleSpawner.processConfig(data);
		particleSpawner.constructTexturesFor(hookName);
	}
	
	String hookName;
	
	public IHookModel hookModel;
	public IChainModel chainModel;
	public IParticleSpawner particleSpawner;
	
	public HookRenderer(String hookName)
	{
		this.hookName = hookName;
		
		hookInfo = new ResourceLocation(HookMod.MODID, "textures/hooks/" + hookName + "/info.txt");
		HookMod.proxy.registerResourceConfig(this);
	}
	
	@Override
	public void renderHook(ActiveHook hook, Vector3 pointing)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Tessellator t = Tessellator.instance;
		
		boolean didDitchDefaultTransform = !hook.isRetracting();
				
		if(didDitchDefaultTransform)
		{
			GL11.glPopMatrix(); // get rid of the default transform
			GL11.glPushMatrix();
	
			HookMod.proxy.glAlign(Axis.Y, hook.getHeading().copy().multiply(-1), Axis.X, Axis.Y);
		}
				
		hookModel.draw(hook);
		
		if(didDitchDefaultTransform)
		{
			GL11.glPopMatrix();
			GL11.glPushMatrix(); // give an empty matrix to pop after this method is called
		}
	}

	@Override
	public void renderLine(ActiveHook hook, Vector3 offsetToPlayer)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Tessellator t = Tessellator.instance;
		
		boolean didDitchDefaultTransform = !hook.isRetracting();
		double length = offsetToPlayer.mag();
		if(didDitchDefaultTransform)
		{
			GL11.glPopMatrix(); // get rid of the default transform
			GL11.glPushMatrix();
			
			
			double d = hookModel.getLoopCenterPointDistance(hook);
			Vector3 heading = hook.getHeading().copy().multiply(-d);
			GL11.glTranslated(heading.x, heading.y, heading.z);
			Vector3 newOffset = offsetToPlayer.copy().sub(heading);
			length = newOffset.mag();
			HookMod.proxy.glAlign(Axis.Y, newOffset.copy().normalize(), Axis.X, Axis.Y);
		}
		
		chainModel.draw(hook, length);
		
		if(didDitchDefaultTransform)
		{
			GL11.glPopMatrix();
			GL11.glPushMatrix(); // give an empty matrix to pop after this method is called
		}
	}
	
	public void spawnParticles(ActiveHook hook, EntityPlayer player) {
		double d = hookModel.getLoopCenterPointDistance(hook);
		Vector3 heading = hook.getHeading().copy().multiply(-d);
		
		Vector3 correctedLocation = hook.getLocation().copy().add(heading);
		particleSpawner.spawnParticles(player, hook, correctedLocation);
	}
	
}
