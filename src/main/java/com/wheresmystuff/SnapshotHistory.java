package com.wheresmystuff;

import java.util.ArrayList;
import java.util.List;

public class SnapshotHistory
{
    private List<StorageSnapshot> snapshots = new ArrayList<>();

    public List<StorageSnapshot> getSnapshots()
    {
        return snapshots == null ? new ArrayList<>() : snapshots;
    }

    public void setSnapshots(List<StorageSnapshot> snapshots)
    {
        this.snapshots = snapshots == null ? new ArrayList<>() : new ArrayList<>(snapshots);
    }
}