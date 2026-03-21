package com.wheresmystuff;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

class WheresMyStuffTableModel extends AbstractTableModel
{
	private static final int COL_NAME = 0;
	private static final int COL_QTY = 1;
	private static final int COL_VALUE = 2;
	private static final int COL_LOCATION = 3;

	private final String[] columns = {"Item", "Qty", "Value", "Location"};
	private List<StoredItem> items = new ArrayList<>();

	void setItems(List<StoredItem> items)
	{
		this.items = new ArrayList<>(items);
		fireTableDataChanged();
	}

	@Override
	public int getRowCount()
	{
		return items.size();
	}

	@Override
	public int getColumnCount()
	{
		return columns.length;
	}

	@Override
	public String getColumnName(int column)
	{
		return columns[column];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		switch (columnIndex)
		{
			case COL_QTY:
				return Integer.class;
			case COL_VALUE:
				return Long.class;
			default:
				return String.class;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		StoredItem item = items.get(rowIndex);
		switch (columnIndex)
		{
			case COL_NAME:
				return item.getItemName();
			case COL_QTY:
				return item.getQuantity();
			case COL_VALUE:
				return item.hasKnownValue() ? item.getTotalValue() : null;
			case COL_LOCATION:
				return item.getLocation().getDisplayName();
			default:
				return null;
		}
	}
}
