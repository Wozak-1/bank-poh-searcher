package com.wheresmystuff;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import java.util.Optional;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.client.callback.ClientThread;
import net.runelite.api.Item;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.api.events.GameTick;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.api.ChatMessageType;
import net.runelite.client.util.ColorUtil;

@PluginDescriptor(
		name = "Bank and POH Seacher",
		description = "Search bank and POH storage snapshots",
		tags = {"bank", "poh", "house", "storage", "search", "panel"}
)
public class WheresMyStuffPlugin extends Plugin
{
	private NavigationButton navigationButton;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private WheresMyStuffConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SnapshotStore snapshotStore;

	@Inject
	private WheresMyStuffPanel panel;


	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private PohStorageScanner pohStorageScanner;

	private Map<StorageLocation, StorageSnapshot> snapshots = new EnumMap<>(StorageLocation.class);
	private boolean loadedForLoggedInProfile = false;
	private Integer pendingPohGroupId = null;
	private int pendingPohScanTicks = 0;
	private int pendingPohAttempts = 0;
	private boolean pendingBankRescan = false;
	private int pendingBankRescanTicks = 0;
	private boolean isBankOpen()
	{
		return client.getWidget(ComponentID.BANK_ITEM_CONTAINER) != null;
	}

	@Provides
	WheresMyStuffConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WheresMyStuffConfig.class);
	}

	@Override
	protected void startUp()
	{
		BufferedImage icon;
		try
		{
			icon = ImageUtil.loadImageResource(getClass(), "icon.png");
		}
		catch (IllegalArgumentException ex)
		{
			icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		}

		navigationButton = NavigationButton.builder()
				.tooltip("Bank/POH Seacher")
				.icon(icon)
				.priority(24)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navigationButton);

		snapshots = new EnumMap<>(StorageLocation.class);
		for (StorageLocation location : StorageLocation.values())
		{
			snapshots.put(location, new StorageSnapshot());
		}

		panel.refresh();
	}

	@Override
	protected void shutDown()
	{
		persistAllSnapshots();

		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState gameState = event.getGameState();

		if (gameState == GameState.LOGGED_IN)
		{
			loadSnapshotsForCurrentProfile();
			SwingUtilities.invokeLater(panel::refresh);
			return;
		}

		if (gameState == GameState.LOGIN_SCREEN
				|| gameState == GameState.HOPPING
				|| gameState == GameState.CONNECTION_LOST)
		{
			persistAllSnapshots();
			loadedForLoggedInProfile = false;

			pendingBankRescan = false;
			pendingBankRescanTicks = 0;

			pendingPohGroupId = null;
			pendingPohScanTicks = 0;
			pendingPohAttempts = 0;
		}
	}
	private void debug(String message)
	{
		if (!config.debugg()) {return;}
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		chatMessageManager.queue(
				QueuedMessage.builder()
						.type(ChatMessageType.GAMEMESSAGE)
						.runeLiteFormattedMessage(
								ColorUtil.wrapWithColorTag("[WMS] ", java.awt.Color.CYAN)
										+ message
						)
						.build()
		);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (!loadedForLoggedInProfile)
		{
			loadSnapshotsForCurrentProfile();
		}

		if (event.getContainerId() != InventoryID.BANK.getId())
		{
			return;
		}

		pendingBankRescan = true;
		pendingBankRescanTicks = 2;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (pendingBankRescan)
		{
			if (pendingBankRescanTicks > 0)
			{
				pendingBankRescanTicks--;
			}
			else
			{
				if (isBankOpen())
				{
					ItemContainer bank = client.getItemContainer(InventoryID.BANK);
					if (bank != null)
					{
						scanBank(bank);
					}
				}

				pendingBankRescan = false;
			}
		}
		
		if (pendingPohGroupId != null)
		{
			if (pendingPohScanTicks > 0)
			{
				pendingPohScanTicks--;
			}
			else
			{
				int groupId = pendingPohGroupId;

				Optional<PohStorageScanner.ScanResult> result = pohStorageScanner.scanLoadedGroup(groupId);
				if (result.isPresent())
				{
					PohStorageScanner.ScanResult scanResult = result.get();
					debug("POH matched " + scanResult.getLocation() + " qty=" + scanResult.getQuantities().size());
					replaceSnapshot(scanResult.getLocation(), scanResult.getQuantities());

					pendingPohGroupId = null;
					pendingPohAttempts = 0;
					pendingPohScanTicks = 0;
				}
				else
				{
					pendingPohAttempts--;

					if (pendingPohAttempts <= 0)
					{
						debug("POH no match for groupId=" + groupId);
						debug("POH text dump: " + pohStorageScanner.debugAllText(groupId));
						pendingPohGroupId = null;
						pendingPohAttempts = 0;
						pendingPohScanTicks = 0;
					}
					else
					{
						pendingPohScanTicks = 1;
					}
				}
			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (!loadedForLoggedInProfile)
		{
			loadSnapshotsForCurrentProfile();
		}

		int groupId = event.getGroupId();
		debug("WidgetLoaded groupId=" + groupId);

		if (groupId != 674 && groupId != 675)
		{
			return;
		}

		if (pendingPohGroupId != null)
		{
			return;
		}

		pendingPohGroupId = groupId;
		pendingPohScanTicks = 2;
		pendingPohAttempts = 6;
		debug("Queued POH scan for groupId=" + groupId);
	}

	private void loadSnapshotsForCurrentProfile()
	{
		Map<StorageLocation, StorageSnapshot> loaded = snapshotStore.loadAllSnapshots();

		snapshots = new EnumMap<>(StorageLocation.class);
		if (loaded != null)
		{
			snapshots.putAll(loaded);
		}

		for (StorageLocation location : StorageLocation.values())
		{
			snapshots.computeIfAbsent(location, ignored -> new StorageSnapshot());
		}

		loadedForLoggedInProfile = true;
	}

	public void requestPanelRefresh()
	{
		clientThread.invokeLater(() ->
		{
			List<StoredItem> bankRows = buildBankRows();
			List<StoredItem> pohRows = buildPohRows();

			SwingUtilities.invokeLater(() -> panel.applyRows(bankRows, pohRows));
		});
	}



	private void persistAllSnapshots()
	{
		if (snapshots == null || snapshots.isEmpty())
		{
			return;
		}

		snapshotStore.saveAllSnapshots(snapshots);
	}

	private void scanBank(ItemContainer bank)
	{
		if (bank == null)
		{
			return;
		}

		if (!isBankOpen())
		{
			return;
		}

		Map<Integer, Integer> quantities = new HashMap<>();
		for (Item item : bank.getItems())
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}

			quantities.merge(item.getId(), item.getQuantity(), Integer::sum);
		}

		if (quantities.isEmpty())
		{
			return;
		}
		debug("Bank snapshot items = " + quantities.size());
		replaceSnapshot(StorageLocation.BANK, quantities);
	}

	void replaceSnapshot(StorageLocation location, Map<Integer, Integer> quantities)
	{
		StorageSnapshot snapshot = getSnapshot(location);
		snapshot.replaceAll(quantities);
		snapshotStore.saveSnapshot(location, snapshot);
		SwingUtilities.invokeLater(panel::refresh);
	}

	public StorageSnapshot getSnapshot(StorageLocation location)
	{
		return snapshots.computeIfAbsent(location, ignored -> new StorageSnapshot());
	}

	public Map<StorageLocation, StorageSnapshot> getSnapshots()
	{
		return new EnumMap<>(snapshots);
	}



	public List<StoredItem> buildBankRows()
	{
		List<StoredItem> rows = buildRowsForLocation(StorageLocation.BANK);
		debug("buildBankRows rows = " + rows.size());
		return rows;
	}

	public List<StoredItem> buildPohRows()
	{
		List<StoredItem> rows = new ArrayList<>();

		for (StorageLocation location : StorageLocation.values())
		{
			if (!location.isPoh())
			{
				continue;
			}

			rows.addAll(buildRowsForLocation(location));
		}

		rows.sort(
				Comparator.comparing(StoredItem::getItemName, String.CASE_INSENSITIVE_ORDER)
						.thenComparing(item -> item.getLocation().getDisplayName(), String.CASE_INSENSITIVE_ORDER)
		);

		return rows;
	}

	private List<StoredItem> buildRowsForLocation(StorageLocation location)
	{
		List<StoredItem> rows = new ArrayList<>();
		StorageSnapshot snapshot = getSnapshot(location);

		if (snapshot == null)
		{
			debug("buildRowsForLocation " + location + " snapshot = null");
			return rows;
		}

		Map<Integer, Integer> quantities = snapshot.getQuantities();
		debug("buildRowsForLocation " + location + " quantities size = " + (quantities == null ? "null" : quantities.size()));

		if (quantities == null || quantities.isEmpty())
		{
			return rows;
		}

		int added = 0;

		for (Map.Entry<Integer, Integer> entry : quantities.entrySet())
		{
			Integer rawIdObj = entry.getKey();
			Integer qtyObj = entry.getValue();

			if (rawIdObj == null || qtyObj == null)
			{
				continue;
			}

			int rawId = rawIdObj;
			int qty = qtyObj;

			if (rawId <= 0 || qty <= 0)
			{
				continue;
			}

			String name = "Item " + rawId;
			int price = 0;

			try
			{
				ItemComposition comp = itemManager.getItemComposition(rawId);
				if (comp != null && comp.getName() != null && !comp.getName().isEmpty())
				{
					name = comp.getName();
				}

				price = itemManager.getItemPrice(rawId);

				if (price <= 0 && comp != null)
				{
					price = comp.getHaPrice();
				}
			}
			catch (Exception e)
			{
				debug("lookup failed rawId=" + rawId
						+ " type=" + e.getClass().getSimpleName()
						+ " msg=" + (e.getMessage()));
			}

			rows.add(new StoredItem(
					rawId,
					name,
					qty,
					price,
					location
			));
			added++;
		}

		rows.sort(Comparator.comparing(StoredItem::getItemName, String.CASE_INSENSITIVE_ORDER));
		debug("buildRowsForLocation " + location + " added rows = " + added);

		return rows;
	}
}