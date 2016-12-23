package com.thecodewarrior.hooks.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.item.ItemHookProvider;
import com.thecodewarrior.hooks.net.NetHandler;
import com.thecodewarrior.hooks.net.server.FireHookMessage;

public class HookProperties implements IExtendedEntityProperties
{
	public static final String IDENT = "terrariaHookProps";
	public boolean isSteady = false;
	List<ActiveHook> hooks = new ArrayList<ActiveHook>();
	Entity e;
	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		
	}
	
	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		
	}
	
	@Override
	public void init(Entity entity, World world)
	{
		e = entity;
	}
	
	public void clearHooks()
	{
		for (ActiveHook activeHook : hooks)
		{
			if(activeHook.isRetracting())
				activeHook.destroy();
			else
				activeHook.setRetracting();
		}
	}
	
	public void cleanHooks()
	{
		List<ActiveHook> removeHooks = new ArrayList<ActiveHook>();
		for (ActiveHook hook : hooks)
		{
			if(hook.shouldDestroy())
				removeHooks.add(hook);
		}
		hooks.removeAll(removeHooks);
	}
	
	public void addHook(ActiveHook hook)
	{
		hooks.add(hook);
	}
	public List<ActiveHook> getHooks()
	{
		return hooks;
	}
	
	public int getStoppedCount() {
		int count = 0;
		for (ActiveHook activeHook : hooks)
		{
			if(activeHook.isStopped())
				count++;
		}
		return count;
	}
	public int getFiringCount() {
		int count = 0;
		for (ActiveHook activeHook : hooks)
		{
			if(!activeHook.isStopped() && !activeHook.shouldDestroy())
				count++;
		}
		return count;
	}
	
	public void hookHit(ActiveHook hook)
	{
		EntityPlayer player = (EntityPlayer)e;
		if(getStoppedCount() > currentMaxCount) {
			Vector3 playerOrigin = new Vector3( player.posX, player.posY+(player.boundingBox.maxY-player.boundingBox.minY)/2, player.posZ);
			Vector3 targetVec = hook.getLocation().copy().sub(playerOrigin).multiply(-1).normalize();
			ActiveHook opposite = null;
			while (getStoppedCount() > currentMaxCount) {
				opposite = getClosestHookToVector(targetVec, playerOrigin).getFirst();
				if(opposite == null)
					break;
				if(opposite != null)
					opposite.setRetracting();
			}
			
		}
	}
	
	Hook currentHook;
	int currentMaxCount;
	public boolean	isHooked;
	
	public void fireHook(EntityPlayer player, Hook hook, ItemStack stack)
	{
		ItemHookProvider hookProvider = (  (ItemHookProvider)stack.getItem()  );
		if(currentHook != hook)
		{
			if(getHooks().size() > 0)
				return;
			currentHook = hook;
			currentMaxCount = hookProvider.getHookCount(stack, player);
		}
		currentMaxCount = hookProvider.getHookCount(stack, player);
		if(!hookProvider.isMultiHook(stack, player) && getFiringCount() > 0) {
			return;
		}
		
		ActiveHook ahook = new ActiveHook(hook);
		ahook.setLocation(new Vector3( RayTracer.getCorrectedHeadVec(player) ));
//		ahook.setLocation(new Vector3(player.posX, player.posY, player.posZ));
		ahook.setHeading(new Vector3( player.getLookVec() ).normalize());
		ahook.setHitNotify(this);
		addHook(ahook);
		
		if(HookMod.proxy.isClient)
			NetHandler.CHANNEL.sendToServer(new FireHookMessage(ahook.getHook().getId(), ahook.getUUID(), ahook.getLocation(), ahook.getHeading()));
	}
	
	public Tuple<ActiveHook, Double> getClosestHookToVector(Vector3 targetVec, Vector3 playerOrigin) {
		ActiveHook currentHook = null;
		double currentDistance = 1000;
		for (ActiveHook aHook : getHooks())
		{
			if(aHook.isRetracting() || aHook.shouldDestroy())
				continue;
			Vector3 hookVec = aHook.getLocation().copy().sub(playerOrigin);
			double distance = hookVec.angle(targetVec);
			if(distance < currentDistance)
			{
				currentHook = aHook;
				currentDistance = distance;
			}
		}
		return new Tuple<ActiveHook, Double>(currentHook, currentDistance);
	}
}
