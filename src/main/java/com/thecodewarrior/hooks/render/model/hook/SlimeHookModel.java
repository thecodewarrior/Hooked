package com.thecodewarrior.hooks.render.model.hook;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.render.model.IHookModel;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookUtil;

public class SlimeHookModel implements IHookModel
{
	double size = .75;
	
	@Override
	public void constructTexturesFor(String hookName)
	{
	}

	@Override
	public void processConfig(Map<String, String> data)
	{
		size = HookUtil.parseWithDefault(size, data.get("hook.size"));
		
		EntitySlime tmp = new EntitySlime(null);
		while(tmp.getSlimeSize() != 1) {
			tmp = new EntitySlime(null);
		}
		slime = tmp;
	}

	private Entity slime;

	@Override
	public void draw(ActiveHook hook)
	{
		
		GL11.glRotated(90, 0, 1, 0);
		GL11.glRotated(90, 1, 0, 0);

		Minecraft mc = Minecraft.getMinecraft();		
		
		GL11.glScaled(size, size, size);
		
		slime.worldObj = mc.theWorld;
		GL11.glTranslated(0, -0.25, -0.25);
		RenderManager.instance.renderEntityWithPosYaw(slime, 0, 0, 0, 0, 1);
	}

	@Override
	public double getLoopCenterPointDistance(ActiveHook hook)
	{
		return size/2.0;
	}
	
}
/* some code to make the slime lay flat against the surface it hits, waaay to complicated for right now, I might pick this up at some point.

Vector3 sideVec = new Vector3(hook.getHitSide().offsetX, hook.getHitSide().offsetY, hook.getHitSide().offsetZ);
Vector3 headVec = hook.getHeading().copy();
if(hook.getHitSide().offsetX != 0) {
	
	if(Math.abs( headVec.x ) > Math.abs( headVec.z ) && Math.abs( headVec.x ) > Math.abs( headVec.y )){
		GL11.glRotated(-90, 0, hook.getHitSide().offsetX, 0);
	} else {
		headVec.x = 0;
		headVec.normalize();
		double rot_angle = Math.acos( headVec.dotProduct(new Vector3(0, 0, 1)) );
		Vector3 rot_axis = null;
		if( Math.abs(rot_angle) > 1.0/65536 )
		{
			rot_axis = headVec.copy().crossProduct(new Vector3(0, 0, 1)).normalize();
		    rot_axis.x *= -1;
		    rot_axis.z *= -1;
		    
		    GL11.glRotated( Math.toDegrees(rot_angle), rot_axis.x, rot_axis.y, rot_axis.z );
		}
		if(headVec.z < 0)
			GL11.glRotated(180, 0, 0, 1);
	}
	
}

if(hook.getHitSide().offsetY != 0) {
	
	boolean mostlyVertical = Math.abs( headVec.y ) > Math.abs( headVec.x ) && Math.abs( headVec.y ) > Math.abs( headVec.z );
	
	headVec.y = 0;
	headVec.normalize();
	double rot_angle = Math.acos( headVec.dotProduct(new Vector3(0, 0, 1)) );
	Vector3 rot_axis = null;
	if( Math.abs(rot_angle) > 1.0/65536 )
	{
		rot_axis = headVec.copy().crossProduct(new Vector3(0, 0, 1)).normalize();
		rot_axis.y *= -1;
	    
	    GL11.glRotated( Math.toDegrees(rot_angle), rot_axis.x, rot_axis.y, rot_axis.z );
	}
	if(mostlyVertical)
		GL11.glRotated(90, hook.getHitSide().offsetY, 0, 0);
	
}

if(hook.getHitSide().offsetZ != 0) {
	
	if(Math.abs( headVec.z ) > Math.abs( headVec.x ) && Math.abs( headVec.z ) > Math.abs( headVec.y )){
		if(hook.getHitSide().offsetZ > 0)
			GL11.glRotated(180, 0, 1, 0);
	} else {
		headVec.z = 0;
		headVec.normalize();
		double rot_angle = Math.acos( headVec.dotProduct(new Vector3(1, 0, 0)) );
		Vector3 rot_axis = null;
		if( Math.abs(rot_angle) > 1.0/65536 )
		{
			rot_axis = headVec.copy().crossProduct(new Vector3(1, 0, 0)).normalize();
		    rot_axis.x *= -1;
		    rot_axis.z *= -1;
		    
		    GL11.glRotated( Math.toDegrees(rot_angle), rot_axis.x, rot_axis.y, rot_axis.z );
		}
		GL11.glRotated(90, 0, 1, 0);
		if(headVec.x < 0)
			GL11.glRotated(180, 0, 0, 1);
	}
	
}
*/
