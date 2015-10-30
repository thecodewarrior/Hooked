package com.thecodewarrior.hooks.proxy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import thecodewarrior.equipment.api.EquipmentApi;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.IHookRenderer;
import com.thecodewarrior.hooks.item.ItemHookProvider;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookProperties;
import com.thecodewarrior.hooks.util.HookWrapper;
import com.thecodewarrior.hooks.util.IResourceConfig;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

public class ClientProxy extends CommonProxy implements IResourceManagerReloadListener
{
	public static KeyBinding fireBinding;
	public static KeyBinding togglePullBinding;
	
//	public static boolean isClient = true;
	
	public void init()
	{
		isClient = true;
		fireBinding    = new KeyBinding("key.hooks.fire.desc",    Keyboard.KEY_R, "key.categories.gameplay");
		ClientRegistry.registerKeyBinding(fireBinding);
		
		togglePullBinding = new KeyBinding("key.hooks.togglePull.desc", Keyboard.KEY_P, "key.categories.gameplay");
		ClientRegistry.registerKeyBinding(togglePullBinding);
		
		( (IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager() ).registerReloadListener(this);
	}
	
	public boolean isClientPlayer(Entity e)
	{
		return e == Minecraft.getMinecraft().thePlayer;
	}
	
	@SubscribeEvent
	public void onEvent(KeyInputEvent event)
	{
	    EntityPlayer p = Minecraft.getMinecraft().thePlayer;
		
	    if(Minecraft.getMinecraft().gameSettings.keyBindJump.isPressed() && (shouldPull || p.isSneaking())) {
	    	doRetractHook(p);
	    	HookProperties prop = HookWrapper.getProperties(p);
	    	if(!prop.isSteady) {
	    		p.motionX *= 2;
	    		p.motionY *= 2;
	    		p.motionZ *= 2;
	    	}
	    }
	    else if (fireBinding.isPressed()) 
	    {
			doFireHook(p);
	    }
		else if(togglePullBinding.isPressed())
		{
			this.shouldPull = !this.shouldPull;
		}
	}
	
	public void doFireHook(EntityPlayer player)
	{
		HookWrapper w = new HookWrapper(player);
		ItemStack stack = EquipmentApi.getEquipment(player, HookMod.equipmentSlotId);
		if(stack != null && stack.getItem() instanceof ItemHookProvider)
        {
			w.fireHook(stack);
        }
	}
	
	public void doRetractHook(EntityPlayer player)
	{
		HookWrapper w = new HookWrapper(player);
		w.clearHooks();
	}
	
	@SubscribeEvent
	public void worldRender(RenderWorldLastEvent event)
	{
//		System.out.println(event.getPhase());
		
//		renderHooksFor(Minecraft.getMinecraft().thePlayer, event.partialTicks);
		Minecraft mc = Minecraft.getMinecraft();

		boolean lighting = GL11.glGetBoolean(GL11.GL_LIGHTING);
		
		mc.mcProfiler.startSection("hooks");
		GL11.glDisable(GL11.GL_LIGHTING);
		for(Object p : mc.theWorld.playerEntities)
		{
			renderHooksFor((EntityPlayer)p, event.partialTicks);
		}
		if(lighting) {
			GL11.glEnable(GL11.GL_LIGHTING);
		} else {
			GL11.glDisable(GL11.GL_LIGHTING);
		}
		
		mc.mcProfiler.endSection();
		
//		renderHooksFor(mc.thePlayer, event.partialTicks);


	}
	@SubscribeEvent
	public void renderPlayer(RenderPlayerEvent.Post event)
	{
//		if(event.entityPlayer == Minecraft.getMinecraft().thePlayer)
//			return;
//		renderHooksFor(event.entityPlayer, event.partialRenderTick);
	}
	
	public void renderHooksFor(EntityPlayer player, float partialTicks)
	{
		EntityPlayer rootPlayer = Minecraft.getMinecraft().thePlayer;
		List<ActiveHook> hooks = HookWrapper.getProperties(player).getHooks();
		if(hooks.size() == 0)
			return;
		double x = rootPlayer.lastTickPosX + (rootPlayer.posX - rootPlayer.lastTickPosX) * partialTicks;
        double y = rootPlayer.lastTickPosY + (rootPlayer.posY - rootPlayer.lastTickPosY) * partialTicks;
        double z = rootPlayer.lastTickPosZ + (rootPlayer.posZ - rootPlayer.lastTickPosZ) * partialTicks;
        
		GL11.glPushMatrix();

		Tessellator t = Tessellator.instance;
		
		GL11.glTranslated(-x, -y, -z);
//		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
		for (ActiveHook hook : hooks)
		{
			Vector3 pos = hook.getLocation();
			GL11.glTranslated(pos.x, pos.y, pos.z);
			renderHook(hook, player, partialTicks);
			GL11.glTranslated(-pos.x, -pos.y, -pos.z);
		}
//		GL11.glEnable(GL11.GL_ALPHA_TEST);

		GL11.glPopMatrix();
	}
	
	public void renderHook(ActiveHook hook, EntityPlayer player, float partialTicks) {
		IHookRenderer renderer = hook.getHook().getRenderer();//HookRegistry.getRenderer(hook.getHook().getId());
		
		GL11.glColor4f(1, 1, 1, 1);		
		
		double x     = player.lastTickPosX + ( (player.posX - player.lastTickPosX) * partialTicks);
		double headY = player.lastTickPosY + ( (player.posY - player.lastTickPosY) * partialTicks);
		double z     = player.lastTickPosZ + ( (player.posZ - player.lastTickPosZ) * partialTicks);

		double midY = headY-(player.boundingBox.maxY-player.boundingBox.minY)/2;

		
		if(Minecraft.getMinecraft().thePlayer != player)
		{
			headY += (player.boundingBox.maxY-player.boundingBox.minY)/2;
		}
		
		Vector3 hookPos = hook.getLocation();
		
        Vector3 playerHeadPosition = new Vector3(x, headY, z);
        Vector3 playerMidPosition  = new Vector3(x, midY , z);
        
		Vector3 midDiff = playerMidPosition.copy().sub(hookPos);
		
		Tessellator t = Tessellator.instance;
		Vector3 target_dir = midDiff.copy().normalize();		
		
		GL11.glPushMatrix();
		glAlign(Axis.Y, target_dir, Axis.X, Axis.Y);
		renderer.renderHook(hook, target_dir);
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		glAlign(Axis.Y, target_dir, Axis.X, Axis.Y);
		Vector3 offset = playerMidPosition.copy().sub(hookPos);
		renderer.renderLine(hook, offset);
		GL11.glPopMatrix();
	}
	
	/**
	 * Aligns the "align" axis exactly along the "along" vector. Then rotates it along the "align" axis so the "point" axis is lined up with the "to" axis
	 * @param align
	 * @param along
	 * @param point
	 * @param to
	 */
	public void glAlign(Axis align, Vector3 along, Axis point, Axis to) {
		glAlign(align.getVector(), along, point.getVector(), to.getVector());
	}
		
	/**
	 * Aligns the "align" axis exactly along the "along" vector. Then rotates it along the "align" axis so the "point" axis is lined up with the "to" axis
	 * @param align
	 * @param along
	 * @param point
	 * @param to
	 */
	public void glAlign(Vector3 align, Vector3 along, Vector3 point, Vector3 to) {

		double rot_angle = Math.acos( along.dotProduct(align) );
		Vector3 rot_axis = null;
		if( Math.abs(rot_angle) > 1.0/65536 )
		{
			rot_axis = along.copy().crossProduct(align).normalize();
		    rot_axis.x *= -1;
		    rot_axis.z *= -1;
		    
		    GL11.glRotated( Math.toDegrees(rot_angle), rot_axis.x, rot_axis.y, rot_axis.z );
		}
		Vector3 up_target_dir = rot_axis == null ? to : to.rotate(-rot_angle, rot_axis).normalize();
	    up_target_dir.y = 0;
	    if(up_target_dir.x != 0 || up_target_dir.z != 0) {
		    up_target_dir.normalize();

			Vector3 up_axis = point;
			double up_rot_angle = Math.acos( up_target_dir.copy().dotProduct(up_axis) );
			if( Math.abs(up_rot_angle) > 1.0/65536 )
			{
			    Vector3 up_rot_axis = up_target_dir.copy().crossProduct(up_axis).normalize().multiply(-1);
			    GL11.glRotated( Math.toDegrees(up_rot_angle), 0, up_rot_axis.y, 0 );
			}
	    }
	}

	@Override
	public void onResourceManagerReload(IResourceManager manager)
	{
		for (IResourceConfig config : configs)
		{
			ResourceLocation loc = config.getConfigLocation();
			Map<String, String> data = new HashMap<String, String>();

			try
			{
				List<IResource> resources = manager.getAllResources(loc);
				
				for(IResource resource : resources) {
						InputStream is = resource.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(is));
						String line = reader.readLine();
			            while(line != null){
			            	line = line.replaceFirst("^\\s+", ""); // trim leading
			                int hashLoc = line.indexOf('#');
			                if(hashLoc != -1)
			                	line = line.substring(0, hashLoc);
			                String[] parts = line.split(":", 2);
			                if(parts.length > 1) {
			                	data.put(parts[0], parts[1]);
			                }
			                line = reader.readLine();
			            }
				}

			}
			catch (FileNotFoundException e) {
				HookMod.logger.warn("Could not read resource: " + loc.getResourceDomain() + ":" + loc.getResourcePath());
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
            config.processConfig(data);
		}
	}
	
	List<IResourceConfig> configs = new ArrayList<IResourceConfig>();
	
	public void registerResourceConfig(IResourceConfig obj) {
		configs.add(obj);
	}

}
