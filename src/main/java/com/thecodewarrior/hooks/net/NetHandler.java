package com.thecodewarrior.hooks.net;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import codechicken.lib.vec.Vector3;

import com.thecodewarrior.hooks.net.client.DestroyHookClientMessage;
import com.thecodewarrior.hooks.net.client.FireHookClientMessage;
import com.thecodewarrior.hooks.net.client.LatchHookClientMessage;
import com.thecodewarrior.hooks.net.client.RetractHookClientMessage;
import com.thecodewarrior.hooks.net.server.DestroyHookMessage;
import com.thecodewarrior.hooks.net.server.FireHookMessage;
import com.thecodewarrior.hooks.net.server.LatchHookMessage;
import com.thecodewarrior.hooks.net.server.RetractHookMessage;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NetHandler
{
	public static SimpleNetworkWrapper CHANNEL;
	
	public static void init()
	{
		CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("hooksNetChannel");
		
		CHANNEL.registerMessage(FireHookMessage.Handler.class,    FireHookMessage.class,    0, Side.SERVER);
		CHANNEL.registerMessage(RetractHookMessage.Handler.class, RetractHookMessage.class, 1, Side.SERVER);
		CHANNEL.registerMessage(DestroyHookMessage.Handler.class, DestroyHookMessage.class, 2, Side.SERVER);
		CHANNEL.registerMessage(LatchHookMessage.Handler.class,   LatchHookMessage.class,   3, Side.SERVER);
		int o = 100; // offset for client messages
		CHANNEL.registerMessage(FireHookClientMessage.Handler.class,    FireHookClientMessage.class,    o+0, Side.CLIENT);
		CHANNEL.registerMessage(RetractHookClientMessage.Handler.class, RetractHookClientMessage.class, o+1, Side.CLIENT);
		CHANNEL.registerMessage(DestroyHookClientMessage.Handler.class, DestroyHookClientMessage.class, o+2, Side.CLIENT);
		CHANNEL.registerMessage(LatchHookClientMessage.Handler.class,   LatchHookClientMessage.class,   o+3, Side.CLIENT);
	}
	
	public static void writeUUID(ByteBuf buf, UUID id)
	{
		buf.writeLong(id.getMostSignificantBits());
		buf.writeLong(id.getLeastSignificantBits());
	}
	
	public static UUID readUUID(ByteBuf buf)
	{
		long mostSigBits  = buf.readLong();
		long leastSigBits = buf.readLong();
		
		return new UUID(mostSigBits, leastSigBits);
	}
	
	public static void writeVector3(ByteBuf buf, Vector3 vec)
	{
		buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
	}
	
	public static Vector3 readVector3(ByteBuf buf)
	{
    	double x = buf.readDouble();
    	double y = buf.readDouble();
    	double z = buf.readDouble();
    	
    	return new Vector3(x,y,z);
	}
}
