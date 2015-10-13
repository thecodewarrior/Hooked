package com.thecodewarrior.hooks;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.proxy.CommonProxy.Axis;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.IResourceConfig;

public class BasicHookRenderer implements IHookRenderer, IResourceConfig
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
		for (int i = 0; i < randomTwists.length; i++)
		{
			randomTwists[i] = (Math.random()-0.5)*10;
		}
		hookTextureSize  = parseWithDefault( hookTextureSize,  data.get("hookTextureSize" ) );
		chainOffset = parseWithDefault( chainOffset, data.get("chainOffset") );
		loopOffset = parseWithDefault( loopOffset, data.get("loopOffset") );
		loopCenter = parseWithDefault( loopCenter, data.get("loopCenter") );
		chainTextureSize = parseWithDefault( chainTextureSize, data.get("chainTextureSize") );
		chainLinkOffset  = parseWithDefault( chainLinkOffset,  data.get("chainLinkOffset" ) );
		
		diagonal = data.containsKey("diagonalHook") ? data.get("diagonalHook").equals("true") : false;
	}
	
	double parseWithDefault(double def, String s) {
		if(s == null)
			return def;
		else
			s = s.trim();
	    try {
	        return Double.parseDouble(s);
	    }
	    catch (NumberFormatException e) {
	        // It's OK to ignore "e" here because returning a default value is the documented behaviour on invalid input.
	        return def;
	    }
	}
	
	double hookTextureSize  = 16;
	double chainOffset = 16;
	double loopCenter = 0;
	double chainTextureSize = 16;
	double chainLinkOffset  = 16;
	double loopOffset = 0;
	
	double asdf;
	
	boolean diagonal;
	
	double[] randomTwists = new double[1000];
	
	ResourceLocation hookTexture;
	ResourceLocation hookLoop;
	ResourceLocation chain;

	String hookName;
	
	public BasicHookRenderer(String hookName)
	{
		this.hookName = hookName;
		
		hookTexture = new ResourceLocation(HookMod.MODID, "textures/hooks/" + hookName + "/hook.png");
		hookLoop    = new ResourceLocation(HookMod.MODID, "textures/hooks/" + hookName + "/hook_loop.png");
		chain       = new ResourceLocation(HookMod.MODID, "textures/hooks/" + hookName + "/chain.png");
		hookInfo    = new ResourceLocation(HookMod.MODID, "textures/hooks/" + hookName + "/info.txt");
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
				
		mc.renderEngine.bindTexture(hookTexture);
		GL11.glRotated(45, 0, 1, 0);

		GL11.glPushMatrix();
		
		if(diagonal) {
			GL11.glTranslated(0, 0.5, 0);
			GL11.glRotated(-45, 0, 0, 1);
			GL11.glTranslated(0, -0.5, 0);
		}
		
		t.startDrawingQuads();
		renderFace(
			 0.5, 0, 0,  1, 0,
			-0.5, 0, 0,  0, 0,
			-0.5, 1, 0,  0, 1,
			 0.5, 1, 0,  1, 1
		);
		t.draw();
		
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		GL11.glRotated(90, 0, 1, 0);
		if(diagonal) {
			GL11.glTranslated(0, 0.5, 0);
			GL11.glRotated(-45, 0, 0, 1);
			GL11.glTranslated(0, -0.5, 0);
		}
		t.startDrawingQuads();
		renderFace(
			 0.5, 0, 0,  1, 0,
			-0.5, 0, 0,  0, 0,
			-0.5, 1, 0,  0, 1,
			 0.5, 1, 0,  1, 1
		);
		t.draw();
		GL11.glPopMatrix();
		
		
		mc.renderEngine.bindTexture(hookLoop);
		GL11.glRotated(45, 0, 1, 0);
		GL11.glTranslated(0, ( (loopOffset)/hookTextureSize )*(diagonal ? Math.sqrt(2) : 1), 0);

		t.startDrawingQuads();
		renderFace(
			 0.5, 0, 0,  1, 0,
			-0.5, 0, 0,  0, 0,
			-0.5, 1, 0,  0, 1,
			 0.5, 1, 0,  1, 1
		);
		t.draw();
		
		if(didDitchDefaultTransform)
		{
			GL11.glPopMatrix();
			GL11.glPushMatrix(); // give an empty matrix to pop after this method is called
		}
	}
	
	public void renderFace(
			double x1, double y1, double z1, double u1, double v1,
			double x2, double y2, double z2, double u2, double v2,
			double x3, double y3, double z3, double u3, double v3,
			double x4, double y4, double z4, double u4, double v4) {
		Tessellator tess = Tessellator.instance;
		tess.addVertexWithUV(x1, y1, z1, u1, v1);
		tess.addVertexWithUV(x2, y2, z2, u2, v2);
		tess.addVertexWithUV(x3, y3, z3, u3, v3);
		tess.addVertexWithUV(x4, y4, z4, u4, v4);
		
		tess.addVertexWithUV(x4, y4, z4, u4, v4);
		tess.addVertexWithUV(x3, y3, z3, u3, v3);
		tess.addVertexWithUV(x2, y2, z2, u2, v2);
		tess.addVertexWithUV(x1, y1, z1, u1, v1);
		
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
			
			
			double d = (chainOffset/hookTextureSize)*(diagonal ? Math.sqrt(2) : 1);
			Vector3 heading = hook.getHeading().copy().multiply(-d);
			GL11.glTranslated(heading.x, heading.y, heading.z);
			Vector3 newOffset = offsetToPlayer.copy().sub(heading);
			length = newOffset.mag();
			HookMod.proxy.glAlign(Axis.Y, newOffset.copy().normalize(), Axis.X, Axis.Y);
		}
		
		double size = 0.5;
		double offset = size*(chainLinkOffset/chainTextureSize);
		mc.renderEngine.bindTexture(chain);
//		GL11.glRotated(90, 0, 1, 0);
		int i = 0;
		for(double d = -(size*loopCenter/hookTextureSize); d < (length-offset); d += offset) {
			t.startDrawingQuads();
			renderFace(
				 size/2, d     , 0,  1, 0,
				-size/2, d     , 0,  0, 0,
				-size/2, d+size, 0,  0, 1,
				 size/2, d+size, 0,  1, 1
			);
			t.draw();
			if(i%2 == 0)
				GL11.glRotated(   90 + randomTwists[i%randomTwists.length]  , 0, 1, 0);
			else
				GL11.glRotated(-( 90 + randomTwists[i%randomTwists.length] ), 0, 1, 0);
			i++;
		}
		
		if(didDitchDefaultTransform)
		{
			GL11.glPopMatrix();
			GL11.glPushMatrix(); // give an empty matrix to pop after this method is called
		}
	}
	
}
