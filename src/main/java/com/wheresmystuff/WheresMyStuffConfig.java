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

	@ConfigItem(
			keyName = "debugg",
			name = "Debug",
			description = "Warning, will flood your chat.",
			position = 4
	)
	default boolean debugg()
	{
		return false;
	}
	@ConfigItem(
			keyName = "snapshotCount",
			name = "Snapshot Count",
			description = "Number of snapshots for comparing bank history. Default 5.",
			position = 3
	)
	default int snapshotCount()
	{
		return 5;
	}
}
