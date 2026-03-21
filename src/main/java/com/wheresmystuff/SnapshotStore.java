package com.wheresmystuff;

import com.google.gson.Gson;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
public class SnapshotStore
{
	static final String CONFIG_GROUP = "wheresmystuff";

	private final ConfigManager configManager;
	private final Gson gson;

	@Inject
	public SnapshotStore(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;
	}

	public StorageSnapshot loadSnapshot(StorageLocation location)
	{
		String json = configManager.getRSProfileConfiguration(CONFIG_GROUP, location.getConfigKey());
		if (json == null || json.isBlank())
		{
			return new StorageSnapshot();
		}

		try
		{
			StorageSnapshot snapshot = gson.fromJson(json, StorageSnapshot.class);
			return snapshot == null ? new StorageSnapshot() : snapshot;
		}
		catch (RuntimeException ex)
		{
			return new StorageSnapshot();
		}
	}

	public Map<StorageLocation, StorageSnapshot> loadAllSnapshots()
	{
		Map<StorageLocation, StorageSnapshot> snapshots = new EnumMap<>(StorageLocation.class);
		for (StorageLocation location : StorageLocation.values())
		{
			snapshots.put(location, loadSnapshot(location));
		}
		return snapshots;
	}

	public void saveSnapshot(StorageLocation location, StorageSnapshot snapshot)
	{
		configManager.setRSProfileConfiguration(
				CONFIG_GROUP,
				location.getConfigKey(),
				gson.toJson(snapshot == null ? new StorageSnapshot() : snapshot)
		);
	}

	public void saveAllSnapshots(Map<StorageLocation, StorageSnapshot> snapshots)
	{
		if (snapshots == null)
		{
			return;
		}

		for (Map.Entry<StorageLocation, StorageSnapshot> entry : snapshots.entrySet())
		{
			saveSnapshot(entry.getKey(), entry.getValue());
		}
	}
}