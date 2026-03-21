package com.wheresmystuff;

public class StoredItem
{
	private final int itemId;
	private final String itemName;
	private final int quantity;
	private final int unitPrice;
	private final long totalValue;
	private final StorageLocation location;

	public StoredItem(int itemId, String itemName, int quantity, int unitPrice, StorageLocation location)
	{
		this.itemId = itemId;
		this.itemName = itemName;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.totalValue = unitPrice > 0 ? (long) unitPrice * quantity : -1L;
		this.location = location;
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
}
