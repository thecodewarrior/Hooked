package com.thecodewarrior.hooks.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.IExtendedEntityProperties;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.item.ItemHookProvider;
import com.thecodewarrior.hooks.net.NetHandler;
import com.thecodewarrior.hooks.net.server.FireHookMessage;

public class HookWrapper {
	EntityPlayer player;
	HookProperties props;
	
	public HookWrapper(EntityPlayer player)
	{
		this.player = player;
		this.props = getProperties();
	}
	
	public HookProperties getProperties()
	{
		return getProperties(player);
	}
	
	public static HookProperties getProperties(EntityPlayer player)
	{
		IExtendedEntityProperties props = player.getExtendedProperties(HookProperties.IDENT);
		if(props == null || !( props instanceof HookProperties))
		{
			HookProperties newProperties = new HookProperties();
			newProperties.init(player, player.worldObj);
			player.registerExtendedProperties(HookProperties.IDENT, newProperties);
			return newProperties;
		}
		else
		{
			return (HookProperties)props;
		}
	}
	
	public void fireHook(ItemStack stack)
	{	
		ItemHookProvider hookProvider = (ItemHookProvider)stack.getItem();
		
		props.fireHook(player, hookProvider.getHook(stack, player), stack);
	}
	
	public void clearHooks()
	{
		props.clearHooks();
	}
	
	public List<ActiveHook> getHooks()
	{
		return props.getHooks();
	}
}
