package com.wheresmystuff;

import java.time.Duration;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(SnapshotStore.CONFIG_GROUP)
public interface WheresMyStuffConfig extends Config
{
	@ConfigItem(
		keyName = "warningAfterHours",
		name = "Warning after hours",
		description = "Show a warning if a snapshot is older than this many hours",
		position = 1
	)
	default int warningAfterHours()
	{
		return 24;
	}

	@ConfigItem(
		keyName = "staleAfterHours",
		name = "Stale after hours",
		description = "Mark a snapshot out of date if it is older than this many hours",
		position = 2
	)
	default int staleAfterHours()
	{
		return 168;
	}

	default Duration warningAfter()
	{
		return Duration.ofHours(Math.max(1, warningAfterHours()));
	}

	default Duration staleAfter()
	{
		return Duration.ofHours(Math.max(warningAfterHours() + 1, staleAfterHours()));
	}

	@ConfigItem(
			keyName = "debugg",
			name = "Debug",
			description = "Warning, will flood your chat.",
			position = 3
	)
	default boolean debugg()
	{
		return true;
	}
}
