package com.thecodewarrior.hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thecodewarrior.hooks.net.NetHandler;
import com.thecodewarrior.hooks.proxy.CommonProxy;
import com.thecodewarrior.hooks.util.Hook;
import com.thecodewarrior.hooks.util.HookStats;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = HookMod.MODID, version = HookMod.MODVER, name = HookMod.MODNAME, dependencies="required-after:CodeChickenCore")
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
	
	public static IHookRenderer basicRenderer;
	
	public static Map<String, Hook>			 hooks;
	public static Map<String, ItemHook>		 items;
	public static Map<String, IHookRenderer> renderers;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.init();
		NetHandler.init();
		FMLCommonHandler.instance().bus().register(proxy);
		MinecraftForge.EVENT_BUS.register(proxy);
		
		hooks	  = new HashMap<String, Hook>();
		items	  = new HashMap<String, ItemHook>();
		renderers = new HashMap<String, IHookRenderer>();
		
		basicRenderer = new BasicHookRenderer("unknown");
		
		HookRegisterHelper h = new HookRegisterHelper();
		
		h.reset("iron");
		h.setLength(10).setSpeed(5).setRetractSpeed(10).setDurability(256);
		h.register();
		
		h.reset("gold");
		h.setLength(35).setSpeed(35).setRetractSpeed(35).setDurability(64).setCount(4);
		h.register();
		
		h.reset("diamond");
		h.setLength(15).setSpeed(20).setRetractSpeed(40).setDurability(2048).setCount(3);
		h.register();
		
		h.reset("emerald");
		h.setLength(20).setSpeed(20).setRetractSpeed(40).setUnbreakable().setCount(3);
		h.register();
		
		h.reset("ender");
		h.setLength(25).setSpeed(25 * 20 / 2).setRetractSpeed(25 * 20 / 2).setUnbreakable().setCount(3);
		h.register();
		
		h.reset("slime");
		h.setLength(25).setSpeed(30).setRetractSpeed(60).setUnbreakable().setCount(3);
		h.register();
		
		h.reset("wither");
		h.setLength(30).setSpeed(30).setRetractSpeed(60).setUnbreakable().setCount(4);
		h.register();
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
