package com.thecodewarrior.hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thecodewarrior.equipment.api.EquipmentApi;
import thecodewarrior.equipment.api.EquipmentType;

import com.thecodewarrior.hooks.item.ItemHook;
import com.thecodewarrior.hooks.item.ItemHookProvider;
import com.thecodewarrior.hooks.item.ItemRegisterer;
import com.thecodewarrior.hooks.net.NetHandler;
import com.thecodewarrior.hooks.proxy.CommonProxy;
import com.thecodewarrior.hooks.render.HookRenderer;
import com.thecodewarrior.hooks.render.model.chain.BasicChainModel;
import com.thecodewarrior.hooks.render.model.chain.SpineChainModel;
import com.thecodewarrior.hooks.render.model.hook.BasicHookModel;
import com.thecodewarrior.hooks.render.model.hook.DiagonalHookModel;
import com.thecodewarrior.hooks.render.model.hook.PigHookModel;
import com.thecodewarrior.hooks.render.model.hook.SkullHookModel;
import com.thecodewarrior.hooks.render.model.hook.SlimeHookModel;
import com.thecodewarrior.hooks.render.model.particle.BasicParticleSpawner;
import com.thecodewarrior.hooks.render.model.particle.NullParticleSpawner;
import com.thecodewarrior.hooks.util.Hook;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = HookMod.MODID, version = HookMod.MODVER, name = HookMod.MODNAME, dependencies=""+
		"required-after:CodeChickenCore;" +
		"required-after:EquipmentApi;"+
		"after:ThermalFoundation;"+
		"after:TConstruct;"+
		"after:Metallurgy")
public class HookMod {
	public static final String MODID   = "hooks";
	public static final String MODVER  = "0.1.0";
	public static final String MODNAME = "Terraria Hooks";
	
	public static final String loggerName = "Catwalks";
	public static Logger logger;
	
	private static Map<String, Logger> loggerMap = new HashMap<String, Logger>();
	
	public static Logger getLogger(String... name)
	{
		if(name.length > 0)
		{
			return getOrCreateLogger(loggerName + "][" + StringUtils.join(name, "]["));
		}
		else
		{
			return getOrCreateLogger(loggerName);
		}
	}
	
	private static Logger getOrCreateLogger(String name)
	{
		if(!loggerMap.containsKey(name))
			loggerMap.put(name, LogManager.getLogger(name));
		return loggerMap.get(name);
	}
	
	public static void l(String name, Object text)
	{
		getOrCreateLogger(name).info(text);
	}
	
	public static void l(String name, String text, Object... args)
	{
		l(name, String.format(text, args));
	}
	
	public static List<Item> hooktests = new ArrayList<Item>();
	
	@SidedProxy(clientSide = "com.thecodewarrior.hooks.proxy.ClientProxy", serverSide = "com.thecodewarrior.hooks.proxy.CommonProxy")
	public static CommonProxy proxy;
		
	public static Map<String, Hook>			 hooks;
	public static Map<String, ItemHook>		 items;
	public static Map<String, IHookRenderer> renderers;
	public static String equipmentSlotId;
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		equipmentSlotId = MODID + ":equipment_hook";
		
		proxy.init();
		NetHandler.init();
		FMLCommonHandler.instance().bus().register(proxy);
		MinecraftForge.EVENT_BUS.register(proxy);
		
		logger = getLogger();
		
		hooks	  = new HashMap<String, Hook>();
		items	  = new HashMap<String, ItemHook>();
		renderers = new HashMap<String, IHookRenderer>();
				
		HookRegistry.basicHookModel = BasicHookModel.class;
		HookRegistry.registerHookModel("basic", BasicHookModel.class);
		
		HookRegistry.basicChainModel = BasicChainModel.class;
		HookRegistry.registerChainModel("basic", BasicChainModel.class);
		
		HookRegistry.nullParticleSpawner = NullParticleSpawner.class;
		HookRegistry.registerParticleSpawner("basic", BasicParticleSpawner.class);
		
		HookRegistry.registerHookModel("diagonal", DiagonalHookModel.class);
		HookRegistry.registerHookModel("slime", SlimeHookModel.class);
		HookRegistry.registerHookModel("pig", PigHookModel.class);
		HookRegistry.registerHookModel("skull", SkullHookModel.class);
		
		HookRegistry.registerChainModel("spine", SpineChainModel.class);
		
		ItemRegisterer r = new ItemRegisterer();
		
		r.init();
		
		EquipmentApi.registerEquipment(equipmentSlotId, new EquipmentType() {
			
			ResourceLocation back = new ResourceLocation(MODID, "textures/slot.png");
			
			@Override
			public ResourceLocation getSlotOverlay(ItemStack paramItemStack)
			{
				return back;
			}
			
			@Override
			public boolean canRemoveStack(ItemStack paramItemStack, EntityPlayer paramEntityPlayer)
			{
				return true;
			}
			
			@Override
			public boolean canPlaceStack(ItemStack stack)
			{
				return stack != null && stack.getItem() != null && stack.getItem() instanceof ItemHookProvider;
			}

			@Override
			public String getSlotDescription(EntityPlayer player)
			{
				return I18n.format("tooltip.hookslot", new Object[0]);
			}
		});
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event)
	{
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}
}
