package com.wheresmystuff;

import java.time.Duration;
import java.time.Instant;

public class SnapshotMetadata
{
	private long lastSeenEpochMillis;
	private boolean everSeen;

	public long getLastSeenEpochMillis()
	{
		return lastSeenEpochMillis;
	}

	public void setLastSeenEpochMillis(long lastSeenEpochMillis)
	{
		this.lastSeenEpochMillis = lastSeenEpochMillis;
	}

	/**
	 * Compatibility getter for panel code.
	 */
	public long getLastUpdated()
	{
		return lastSeenEpochMillis;
	}

	public boolean isEverSeen()
	{
		return everSeen;
	}

	public void setEverSeen(boolean everSeen)
	{
		this.everSeen = everSeen;
	}

	public void markSeenNow()
	{
		everSeen = true;
		lastSeenEpochMillis = System.currentTimeMillis();
	}

	public DataFreshness freshness(Duration staleAfter, Duration warningAfter)
	{
		if (!everSeen)
		{
			return DataFreshness.NEVER_SEEN;
		}

		Duration age = Duration.between(
				Instant.ofEpochMilli(lastSeenEpochMillis),
				Instant.now()
		);

		if (age.compareTo(staleAfter) >= 0)
		{
			return DataFreshness.OUT_OF_DATE;
		}

		if (age.compareTo(warningAfter) >= 0)
		{
			return DataFreshness.MAYBE_STALE;
		}

		return DataFreshness.FRESH;
	}
}