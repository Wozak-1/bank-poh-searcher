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

	public SnapshotHistory loadSnapshot(StorageLocation location)
	{
		String json = configManager.getRSProfileConfiguration(CONFIG_GROUP, location.getConfigKey());
		if (json == null || json.isBlank())
		{
			return new SnapshotHistory();
		}

		try
		{
			SnapshotHistory history = gson.fromJson(json, SnapshotHistory.class);
			return history == null ? new SnapshotHistory() : history;
		}
		catch (RuntimeException ex)
		{
			return new SnapshotHistory();
		}
	}

	public Map<StorageLocation, SnapshotHistory> loadAllHistories()
	{
		Map<StorageLocation, SnapshotHistory> histories = new EnumMap<>(StorageLocation.class);
		for (StorageLocation location : StorageLocation.values())
		{
			histories.put(location, loadSnapshot(location));
		}
		return histories;
	}

	public void saveHistory(StorageLocation location, SnapshotHistory history)
	{
		configManager.setRSProfileConfiguration(
				CONFIG_GROUP,
				location.getConfigKey(),
				gson.toJson(history == null ? new SnapshotHistory() : history)
		);
	}

	public void saveAllHistories(Map<StorageLocation, SnapshotHistory> histories)
	{
		if (histories == null)
		{
			return;
		}

		for (Map.Entry<StorageLocation, SnapshotHistory> entry : histories.entrySet())
		{
			saveHistory(entry.getKey(), entry.getValue());
		}
	}
}