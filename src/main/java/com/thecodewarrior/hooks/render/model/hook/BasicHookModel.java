package com.thecodewarrior.hooks.render.model.hook;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.render.model.IHookModel;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookClientUtil;
import com.thecodewarrior.hooks.util.HookUtil;

public class BasicHookModel implements IHookModel
{
	
	private ResourceLocation hookTexture;
	private ResourceLocation loopTexture;

	private double hookTextureSize = 16;
	private double loopTextureSize = 16;
	
	private double hookOffset = 0;
	private double loopOffset = 0;
	private double loopCenter = 16;
	
	@Override
	public void constructTexturesFor(String hookName)
	{
		hookTexture = new ResourceLocation(HookMod.MODID, "textures/hooks/" + hookName + "/hook.png");
		loopTexture = new ResourceLocation(HookMod.MODID, "textures/hooks/" + hookName + "/hook_loop.png");
	}

	@Override
	public void processConfig(Map<String, String> data)
	{
		hookTextureSize = HookUtil.parseWithDefault( hookTextureSize, data.get("hook.hook.textureSize" ) );
		loopTextureSize = HookUtil.parseWithDefault( loopTextureSize, data.get("hook.loop.textureSize" ) );
		hookOffset 		= HookUtil.parseWithDefault( hookOffset,	  data.get("hook.hook.offset" ) );
		loopOffset 		= HookUtil.parseWithDefault( loopOffset,	  data.get("hook.loop.offset" ) );
		loopCenter 		= HookUtil.parseWithDefault( loopCenter,	  data.get("hook.loop.center" ) );
		
	}

	@Override
	public double getLoopCenterPointDistance(ActiveHook hook)
	{
		return loopOffset/loopTextureSize + loopCenter/loopTextureSize;
	}
	
	@Override
	public void draw(ActiveHook hook)
	{
		GL11.glPushMatrix();
		
		Minecraft mc = Minecraft.getMinecraft();
		Tessellator t = Tessellator.instance;
		
		GL11.glTranslated(0,  loopOffset/loopTextureSize, 0);
		GL11.glRotated(90, 0, 1, 0);
		mc.renderEngine.bindTexture(loopTexture);
		t.startDrawingQuads();
		HookClientUtil.renderFace(
			 0.5, 0, 0,  1, 0,
			-0.5, 0, 0,  0, 0,
			-0.5, 1, 0,  0, 1,
			 0.5, 1, 0,  1, 1
		);
		t.draw();
		GL11.glRotated(-90, 0, 1, 0);
		GL11.glTranslated(0, -loopOffset/loopTextureSize, 0);
		
		GL11.glTranslated(0, hookOffset/hookTextureSize, 0);		
		GL11.glRotated(45, 0, 1, 0);
		
		mc.renderEngine.bindTexture(hookTexture);	
		t.startDrawingQuads();
		HookClientUtil.renderFace(
			 0.5, 0, 0,  1, 0,
			-0.5, 0, 0,  0, 0,
			-0.5, 1, 0,  0, 1,
			 0.5, 1, 0,  1, 1
		);
		t.draw();
		
		GL11.glRotated(90, 0, 1, 0);
		
		t.startDrawingQuads();
		HookClientUtil.renderFace(
			 0.5, 0, 0,  1, 0,
			-0.5, 0, 0,  0, 0,
			-0.5, 1, 0,  0, 1,
			 0.5, 1, 0,  1, 1
		);
		t.draw();		
		
		
		
		GL11.glPopMatrix();
	}
	
}
