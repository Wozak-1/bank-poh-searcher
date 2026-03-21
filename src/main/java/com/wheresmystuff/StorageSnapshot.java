package com.wheresmystuff;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StorageSnapshot
{
	private Map<Integer, Integer> quantities = new HashMap<>();
	private SnapshotMetadata metadata = new SnapshotMetadata();

	public StorageSnapshot()
	{
	}

	public Map<Integer, Integer> getQuantities()
	{
		return quantities == null ? Collections.emptyMap() : quantities;
	}

	public void setQuantities(Map<Integer, Integer> quantities)
	{
		this.quantities = quantities == null ? new HashMap<>() : new HashMap<>(quantities);
	}

	public SnapshotMetadata getMetadata()
	{
		if (metadata == null)
		{
			metadata = new SnapshotMetadata();
		}
		return metadata;
	}

	public void setMetadata(SnapshotMetadata metadata)
	{
		this.metadata = metadata == null ? new SnapshotMetadata() : metadata;
	}

	public void replaceAll(Map<Integer, Integer> newQuantities)
	{
		if (quantities == null)
		{
			quantities = new HashMap<>();
		}

		quantities.clear();

		if (newQuantities != null)
		{
			for (Map.Entry<Integer, Integer> entry : newQuantities.entrySet())
			{
				if (entry.getKey() != null && entry.getKey() > 0 && entry.getValue() != null && entry.getValue() > 0)
				{
					quantities.put(entry.getKey(), entry.getValue());
				}
			}
		}

		getMetadata().markSeenNow();
	}
}