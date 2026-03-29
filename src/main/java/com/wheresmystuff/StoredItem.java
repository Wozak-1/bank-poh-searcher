package com.wheresmystuff;

public class StoredItem
{
	private final int itemId;
	private final String itemName;
	private final int quantity;
	private final int unitPrice;
	private final long itemLastChangedEpochMillis;
	private final long totalValue;
	private final StorageLocation location;
	private final EquipmentStats equipmentStats;
	private final EquipmentStats comparisonStats;

	public StoredItem(
			int itemId,
			String itemName,
			int quantity,
			int unitPrice,
			StorageLocation location,
			EquipmentStats equipmentStats,
			EquipmentStats comparisonStats,
			long itemLastChangedEpochMillis)
	{
		this.itemId = itemId;
		this.itemName = itemName;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.totalValue = unitPrice > 0 ? (long) unitPrice * quantity : -1L;
		this.location = location;
		this.equipmentStats = equipmentStats;
		this.comparisonStats = comparisonStats;
		this.itemLastChangedEpochMillis = itemLastChangedEpochMillis;
	}

	public int getItemId()
	{
		return itemId;
	}

	public String getItemName()
	{
		return itemName;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public int getUnitPrice()
	{
		return unitPrice;
	}

	public long getTotalValue()
	{
		return totalValue;
	}

	public boolean hasKnownValue()
	{
		return totalValue >= 0;
	}

	public StorageLocation getLocation()
	{
		return location;
	}

	public EquipmentStats getEquipmentStats()
	{
		return equipmentStats;
	}

	public boolean hasEquipmentStats()
	{
		return equipmentStats != null && equipmentStats.hasAnyBonus();
	}

	public EquipmentStats getComparisonStats()
	{
		return comparisonStats;
	}

	public boolean hasComparisonStats()
	{
		return comparisonStats != null && comparisonStats.hasAnyBonus();
	}

	public long getItemLastChangedEpochMillis()
	{
		return itemLastChangedEpochMillis;
	}
}