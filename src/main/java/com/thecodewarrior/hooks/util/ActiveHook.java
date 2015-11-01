package com.thecodewarrior.hooks.util;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.net.NetHandler;
import com.thecodewarrior.hooks.net.server.DestroyHookMessage;
import com.thecodewarrior.hooks.net.server.LatchHookMessage;
import com.thecodewarrior.hooks.net.server.RetractHookMessage;

public class ActiveHook
{
	Hook hook;
	Vector3 pos;
	Vector3 heading;
	Vector3 vel;
	boolean isStopped;
	boolean retracting;
	boolean destroy = false;
	UUID uuid;
	ForgeDirection hitSide;
	
	public ActiveHook(Hook hook)
	{
		this.hook = hook;
		isStopped = false;
		uuid = UUID.randomUUID();
	}
	
	public ForgeDirection getHitSide()
	{
		return hitSide;
	}
	public void setHitSide(ForgeDirection hitSide)
	{
		this.hitSide = hitSide;
	}
	
	public UUID getUUID()
	{
		return uuid;
	}
	public void setUUID(UUID id)
	{
		uuid = id;
	}
	public boolean shouldDestroy()
	{
		return destroy;
	}
	
	public boolean isRetracting()
	{
		return retracting;
	}
	public void setRetracting()
	{
		this.retracting = true;
		this.isStopped = false;
		if(HookMod.proxy.isClient)
			NetHandler.CHANNEL.sendToServer(new RetractHookMessage( getUUID(), getLocation()));
		updateVelocity();
	}
	
	public void destroyRaw()
	{
		destroy = true;
	}
	
	public void destroy()
	{
		destroy = true;
		if(HookMod.proxy.isClient)
			NetHandler.CHANNEL.sendToServer(new DestroyHookMessage( getUUID() ));
	}
	
	public static final AxisAlignedBB defaultAABB = AxisAlignedBB.getBoundingBox(
			-HookUtil.MARGIN_OF_ERROR, -HookUtil.MARGIN_OF_ERROR, -HookUtil.MARGIN_OF_ERROR,
			 HookUtil.MARGIN_OF_ERROR,  HookUtil.MARGIN_OF_ERROR,  HookUtil.MARGIN_OF_ERROR);
	
	public AxisAlignedBB getAABB()
	{
		return defaultAABB;
	}
	
	public void update(Entity e)
	{
		if(!HookMod.proxy.isClientPlayer(e))
			return;
		Vector3 eLoc = new Vector3(e.posX, e.posY, e.posZ);
		Vector3 loc = getLocation();
		double d = distance(eLoc, getLocation());
		if(d > getHook().getLength()+1)
			setRetracting();
		if(d > 100*getHook().getLength())
			destroy();
		if(isRetracting() && d < getHook().getRetractSpeed())
			destroy();
		
		if(isStopped())
		{
			List<AxisAlignedBB> colliding = e.worldObj.getCollidingBoundingBoxes(e, getAABB().copy().offset(loc.x, loc.y, loc.z).expand(0.001, 0.001, 0.001));
			if(colliding.size() < 1)
				setRetracting();
		}
	}
	
	double distance(Vector3 a, Vector3 b)
	{
		return Math.abs(a.copy().sub(b).mag());
	}
	
	HookProperties hitNotify;
	
	public void setHitNotify(HookProperties hitNotify)
	{
		this.hitNotify = hitNotify;
	}
	
	public boolean isStopped()
	{
		return isStopped;
	}
	public void setStopped()
	{
		if(this.retracting)
		{
			destroy();
		}
		if(this.isStopped)
			return;
		this.isStopped = true;
		if(HookMod.proxy.isClient)
			NetHandler.CHANNEL.sendToServer(new LatchHookMessage( getUUID(), getLocation() ));
		if(this.hitNotify != null && !shouldDestroy())
			this.hitNotify.hookHit(this);
	}
	
	public Hook getHook()
	{
		return hook;
	}
	
	public Vector3 getLocation()
	{
		return pos;
	}
	public void setLocation(Vector3 pos)
	{
		this.pos = pos;
	}
	public Vector3 getPullLocation() {
		if(isRetracting()) {
			return getLocation();
		} else {
			return getLocation().copy().add(getHeading().copy().multiply(-1));
		}
	}
	
	public Vector3 getHeading()
	{
		return heading;
	}
	
	public void setHeading(Vector3 heading)
	{
		this.heading = heading;
		updateVelocity();
	}
	
	public double getSpeed()
	{
		return hook.getSpeed();
	}
	
	public Vector3 getVelocity()
	{
		return vel;
	}
	void updateVelocity()
	{
		this.vel = getHeading().copy().multiply(getSpeed());
	}
}
