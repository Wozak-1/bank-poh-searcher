package com.runwms;

import com.wheresmystuff.WheresMyStuffPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WheresMyStuffRun
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(WheresMyStuffPlugin.class);
		RuneLite.main(args);
	}
}