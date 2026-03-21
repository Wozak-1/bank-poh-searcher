package com.wheresmystuff;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;

@Singleton
public class PohStorageScanner
{
	private static final int MAX_ROOT_CHILDREN = 256;

	private static final int POH_GROUP_A = 674;
	private static final int POH_GROUP_B = 675;

	private final Client client;

	@Inject
	public PohStorageScanner(Client client)
	{
		this.client = client;
	}

	public Optional<ScanResult> scanLoadedGroup(int groupId)
	{
		if (groupId != POH_GROUP_A && groupId != POH_GROUP_B)
		{
			return Optional.empty();
		}

		List<Widget> roots = getRootWidgets(groupId);
		if (roots.isEmpty())
		{
			return Optional.empty();
		}

		String allText = collectLowercaseText(roots);
		StorageLocation location = detectLocationFromText(allText);

		if (location == null || !location.isPoh())
		{
			return Optional.empty();
		}

		Map<Integer, Integer> quantities = extractItemQuantities(roots);
		return Optional.of(new ScanResult(location, quantities));
	}

	private List<Widget> getRootWidgets(int groupId)
	{
		List<Widget> roots = new ArrayList<>();

		for (int childId = 0; childId < MAX_ROOT_CHILDREN; childId++)
		{
			Widget widget = client.getWidget(groupId, childId);
			if (widget != null)
			{
				roots.add(widget);
			}
		}

		return roots;
	}

	private StorageLocation detectLocationFromText(String allText)
	{
		if (allText == null)
		{
			return null;
		}

		String text = allText.toLowerCase(Locale.ENGLISH);

		if (text.contains("magic wardrobe"))
		{
			return StorageLocation.MAGIC_WARDROBE;
		}

		if (text.contains("toy box"))
		{
			return StorageLocation.TOY_BOX;
		}

		if (text.contains("cape rack"))
		{
			return StorageLocation.CAPE_RACK;
		}

		if (text.contains("treasure chest"))
		{
			return StorageLocation.TREASURE_CHEST;
		}

		if (text.contains("armour case") || text.contains("armor case"))
		{
			return StorageLocation.ARMOUR_CASE;
		}

		if (text.contains("bookcase") || text.contains("bookshelf"))
		{
			return StorageLocation.BOOKCASE;
		}

		if (text.contains("fancy dress box"))
		{
			return StorageLocation.COSTUME_ROOM;
		}

		return null;
	}

	private String collectLowercaseText(List<Widget> roots)
	{
		StringBuilder sb = new StringBuilder();

		for (Widget root : roots)
		{
			for (Widget widget : traverse(root))
			{
				String text = widget.getText();
				if (text != null && !text.isEmpty())
				{
					sb.append(' ')
							.append(stripTags(text).toLowerCase(Locale.ENGLISH));
				}

				String name = widget.getName();
				if (name != null && !name.isEmpty())
				{
					sb.append(' ')
							.append(stripTags(name).toLowerCase(Locale.ENGLISH));
				}
			}
		}

		return sb.toString();
	}

	private Map<Integer, Integer> extractItemQuantities(List<Widget> roots)
	{
		Map<Integer, Integer> quantities = new HashMap<>();

		for (Widget root : roots)
		{
			for (Widget widget : traverse(root))
			{
				if (widget == null || widget.isHidden())
				{
					continue;
				}

				int itemId = widget.getItemId();
				if (itemId <= 0)
				{
					continue;
				}


				if (widget.getOpacity() > 0)
				{
					continue;
				}

				quantities.put(itemId, 1);
			}
		}

		return quantities;
	}

	public String debugWidgetSummary(int groupId)
	{
		List<Widget> roots = getRootWidgets(groupId);
		StringBuilder sb = new StringBuilder();

		int count = 0;
		for (Widget root : roots)
		{
			for (Widget widget : traverse(root))
			{
				String text = widget.getText();
				String name = widget.getName();
				int itemId = widget.getItemId();
				int qty = widget.getItemQuantity();
				int spriteId = widget.getSpriteId();
				int modelId = widget.getModelId();

				if ((text != null && !text.isEmpty())
						|| (name != null && !name.isEmpty())
						|| itemId > 0
						|| spriteId > 0
						|| modelId > 0)
				{
					sb.append("child ")
							.append(count)
							.append(": text='").append(text).append('\'')
							.append(" name='").append(name).append('\'')
							.append(" itemId=").append(itemId)
							.append(" qty=").append(qty)
							.append(" spriteId=").append(spriteId)
							.append(" modelId=").append(modelId)
							.append('\n');
				}

				count++;
				if (count >= 120)
				{
					return sb.toString();
				}
			}
		}

		return sb.toString();
	}

	private List<Widget> traverse(Widget root)
	{
		List<Widget> widgets = new ArrayList<>();
		Deque<Widget> queue = new ArrayDeque<>();
		Set<Widget> seen = Collections.newSetFromMap(new IdentityHashMap<>());

		queue.add(root);
		seen.add(root);

		while (!queue.isEmpty())
		{
			Widget widget = queue.removeFirst();
			widgets.add(widget);

			pushChildren(queue, seen, widget.getChildren());
			pushChildren(queue, seen, widget.getDynamicChildren());
			pushChildren(queue, seen, widget.getStaticChildren());
			pushChildren(queue, seen, widget.getNestedChildren());
		}

		return widgets;
	}

	private void pushChildren(Deque<Widget> queue, Set<Widget> seen, Widget[] children)
	{
		if (children == null)
		{
			return;
		}

		for (Widget child : children)
		{
			if (child != null && seen.add(child))
			{
				queue.addLast(child);
			}
		}
	}

	private String stripTags(String text)
	{
		return text.replaceAll("<[^>]*>", " ").replace('&', ' ').trim();
	}

	public static class ScanResult
	{
		private final StorageLocation location;
		private final Map<Integer, Integer> quantities;

		public ScanResult(StorageLocation location, Map<Integer, Integer> quantities)
		{
			this.location = location;
			this.quantities = new HashMap<>(quantities);
		}

		public StorageLocation getLocation()
		{
			return location;
		}

		public Map<Integer, Integer> getQuantities()
		{
			return quantities;
		}
	}

	public String debugAllText(int groupId)
	{
		List<Widget> roots = new ArrayList<>();

		for (int childId = 0; childId < 256; childId++)
		{
			Widget w = client.getWidget(groupId, childId);
			if (w != null)
			{
				roots.add(w);
			}
		}

		if (roots.isEmpty())
		{
			return "<no roots>";
		}

		StringBuilder sb = new StringBuilder();

		for (Widget root : roots)
		{
			for (Widget widget : traverse(root))
			{
				String text = widget.getText();
				if (text != null && !text.isEmpty())
				{
					sb.append(' ')
							.append(stripTags(text).toLowerCase(Locale.ENGLISH));
				}
			}
		}

		String result = sb.toString().trim();
		return result.isEmpty() ? "<no text>" : result;
	}
}