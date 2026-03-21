package com.wheresmystuff;

import java.awt.BorderLayout;
import java.util.Objects;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.EnumMap;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class WheresMyStuffPanel extends PluginPanel
{
	private static final Color PANEL_BG = ColorScheme.DARK_GRAY_COLOR;
	private static final Color CARD_BG = ColorScheme.DARKER_GRAY_COLOR;
	private static final Color BORDER = new Color(58, 58, 58);
	private static final Color HEADER = new Color(235, 235, 235);
	private static final Color MUTED = new Color(170, 170, 170);
	private static final Color TABLE_BG = new Color(30, 30, 30);
	private static final Color SELECT_BG = new Color(62, 87, 120);

	private static final Color FRESH_COLOR = new Color(95, 190, 110);
	private static final Color MAYBE_STALE_COLOR = new Color(224, 179, 78);
	private static final Color OUT_OF_DATE_COLOR = new Color(214, 98, 98);
	private static final Color NEVER_SEEN_COLOR = new Color(130, 130, 130);

	private static final Color TAB_BG = new Color(36, 36, 36);
	private static final Color TAB_HOVER_BG = new Color(45, 45, 45);
	private static final Color TAB_ACTIVE_TEXT = Color.WHITE;
	private static final Color TAB_TEXT = new Color(200, 200, 200);
	private static final Color TAB_ACTIVE_BG = new Color(64, 96, 136);

	private final WheresMyStuffPlugin plugin;
	private final ItemManager itemManager;

	private boolean snapshotsExpanded = false;
	private JButton snapshotToggleButton;
	private JPanel snapshotWrapper;

	private final JPanel tabContentPanel = new JPanel(new CardLayout());

	private final JTextField searchField = new JTextField();
	private final JLabel bankSummaryLabel = new JLabel("Bank");
	private final JLabel pohSummaryLabel = new JLabel("POH");
	private final JPanel statusRowsPanel = new JPanel();

	private final DefaultListModel<StoredItem> bankListModel = new DefaultListModel<>();
	private final DefaultListModel<StoredItem> pohListModel = new DefaultListModel<>();

	private final JList<StoredItem> bankList;
	private final JList<StoredItem> pohList;

	private List<StoredItem> allBankRows = new ArrayList<>();
	private List<StoredItem> allPohRows = new ArrayList<>();

	private final JLabel bankEmptyLabel = new JLabel("No bank data yet. Open your bank to create a snapshot.");
	private final JLabel pohEmptyLabel = new JLabel("No POH data yet. Open a POH storage interface to create snapshots.");

	private final JPanel bankResultsCard = new JPanel(new CardLayout());
	private final JPanel pohResultsCard = new JPanel(new CardLayout());

	@Inject
	public WheresMyStuffPanel(WheresMyStuffPlugin plugin, ItemManager itemManager)
	{
		super(false);
		this.plugin = plugin;
		this.itemManager = itemManager;

		this.bankList = createItemList(bankListModel, false);
		this.pohList = createItemList(pohListModel, true);

		buildUi();
		refresh();
		startAutoRefreshTimer();
	}

	private JList<StoredItem> createItemList(DefaultListModel<StoredItem> model, boolean showLocation)
	{
		JList<StoredItem> list = new JList<>(model);
		list.setBackground(TABLE_BG);
		list.setSelectionBackground(SELECT_BG);
		list.setSelectionForeground(Color.WHITE);
		list.setCellRenderer(new StoredItemListCellRenderer(itemManager, showLocation));
		list.setFixedCellHeight(-1);
		list.setVisibleRowCount(-1);
		list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		list.setSelectionModel(new DisabledSelectionModel());
		list.setBorder(BorderFactory.createEmptyBorder());
		list.setPrototypeCellValue(new StoredItem(
				0,
				"25th anniversary skeleton costume top",
				25000,
				1500000,
				showLocation ? StorageLocation.MAGIC_WARDROBE : StorageLocation.BANK
		));
		return list;
	}

	private void buildUi()
	{
		setLayout(new BorderLayout(0, 10));
		setBackground(PANEL_BG);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(PANEL_BG);

		content.add(createTitlePanel());
		content.add(Box.createVerticalStrut(10));
		content.add(createSearchPanel());
		content.add(Box.createVerticalStrut(10));
		content.add(createSummaryPanel());
		content.add(Box.createVerticalStrut(10));
		content.add(createStatusCard());
		content.add(Box.createVerticalStrut(10));
		content.add(createTabsPanel());

		add(content, BorderLayout.CENTER);

		searchField.getDocument().addDocumentListener((docListener) this::applyFilters);
	}

	private JPanel createTitlePanel()
	{
		JPanel panel = createCardPanel();
		panel.setLayout(new BorderLayout());
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

		JLabel title = new JLabel("Where’s My Stuff");
		title.setForeground(HEADER);
		title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 16f));
		title.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel subtitle = new JLabel("<html><div style='text-align:center;'>Search bank and POH snapshots</div></html>");
		subtitle.setForeground(MUTED);
		subtitle.setFont(subtitle.getFont().deriveFont(12f));
		subtitle.setHorizontalAlignment(SwingConstants.CENTER);

		JPanel inner = new JPanel();
		inner.setBackground(CARD_BG);
		inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

		inner.add(title);
		inner.add(Box.createVerticalStrut(4));
		inner.add(subtitle);

		panel.add(inner, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createSearchPanel()
	{
		JPanel panel = createCardPanel();
		panel.setLayout(new BorderLayout(0, 6));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

		JLabel searchLabel = new JLabel("Search");
		searchLabel.setForeground(MUTED);
		searchLabel.setFont(searchLabel.getFont().deriveFont(java.awt.Font.BOLD, 11f));

		searchField.setBackground(TABLE_BG);
		searchField.setForeground(HEADER);
		searchField.setCaretColor(HEADER);
		searchField.setBorder(new CompoundBorder(
				BorderFactory.createLineBorder(BORDER),
				BorderFactory.createEmptyBorder(6, 8, 6, 8)
		));
		searchField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 28));
		searchField.setMinimumSize(new Dimension(Integer.MAX_VALUE, 28));
		searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		searchField.setToolTipText("Search by item name or location");

		JPanel fieldHolder = new JPanel(new BorderLayout());
		fieldHolder.setBackground(CARD_BG);
		fieldHolder.add(searchField, BorderLayout.NORTH);

		panel.add(searchLabel, BorderLayout.NORTH);
		panel.add(fieldHolder, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createSummaryPanel()
	{
		JPanel panel = createCardPanel();
		panel.setLayout(new BorderLayout());
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

		JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
		inner.setBackground(CARD_BG);

		styleSummaryLabel(bankSummaryLabel);
		styleSummaryLabel(pohSummaryLabel);

		inner.add(bankSummaryLabel);
		inner.add(pohSummaryLabel);

		panel.add(inner, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createStatusCard()
	{
		snapshotWrapper = createCardPanel();
		snapshotWrapper.setLayout(new BorderLayout(0, 6));
		snapshotWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

		JPanel headerRow = new JPanel(new BorderLayout());
		headerRow.setBackground(CARD_BG);

		JLabel header = new JLabel("Snapshots");
		header.setForeground(HEADER);
		header.setFont(header.getFont().deriveFont(java.awt.Font.BOLD, 12f));

		snapshotToggleButton = new JButton("▶");
		snapshotToggleButton.setForeground(MUTED);
		snapshotToggleButton.setBorder(null);
		snapshotToggleButton.setContentAreaFilled(false);
		snapshotToggleButton.setFocusPainted(false);
		snapshotToggleButton.addActionListener(e -> toggleSnapshots());

		headerRow.add(header, BorderLayout.WEST);
		headerRow.add(snapshotToggleButton, BorderLayout.EAST);

		statusRowsPanel.setBackground(CARD_BG);
		statusRowsPanel.setLayout(new BoxLayout(statusRowsPanel, BoxLayout.Y_AXIS));
		statusRowsPanel.setVisible(snapshotsExpanded);

		snapshotWrapper.add(headerRow, BorderLayout.NORTH);
		snapshotWrapper.add(statusRowsPanel, BorderLayout.CENTER);

		return snapshotWrapper;
	}

	private void toggleSnapshots()
	{
		snapshotsExpanded = !snapshotsExpanded;
		statusRowsPanel.setVisible(snapshotsExpanded);
		snapshotToggleButton.setText(snapshotsExpanded ? "▼" : "▶");
		snapshotWrapper.revalidate();
		snapshotWrapper.repaint();
	}

	private JPanel createTabsPanel()
	{
		JPanel outer = new JPanel(new BorderLayout());
		outer.setBackground(PANEL_BG);
		outer.setOpaque(true);

		JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		tabBar.setBackground(PANEL_BG);
		tabBar.setOpaque(true);
		tabBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		JButton bankTab = createTabButton("Bank", true);
		JButton pohTab = createTabButton("POH", false);

		tabContentPanel.removeAll();
		tabContentPanel.setLayout(new CardLayout());
		tabContentPanel.setBackground(TABLE_BG);
		tabContentPanel.setOpaque(true);
		tabContentPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER),
				BorderFactory.createEmptyBorder(0, 0, 0, 0)
		));

		bankResultsCard.removeAll();
		pohResultsCard.removeAll();

		bankResultsCard.setLayout(new CardLayout());
		pohResultsCard.setLayout(new CardLayout());

		bankResultsCard.setBackground(TABLE_BG);
		bankResultsCard.setOpaque(true);

		pohResultsCard.setBackground(TABLE_BG);
		pohResultsCard.setOpaque(true);

		bankResultsCard.add(createListPane(bankList), "TABLE");
		bankResultsCard.add(createEmptyPanel(bankEmptyLabel), "EMPTY");

		pohResultsCard.add(createListPane(pohList), "TABLE");
		pohResultsCard.add(createEmptyPanel(pohEmptyLabel), "EMPTY");

		tabContentPanel.add(bankResultsCard, "BANK");
		tabContentPanel.add(pohResultsCard, "POH");

		bankTab.addActionListener(e ->
		{
			setActiveTab(bankTab, pohTab);
			showCard(tabContentPanel, "BANK");
			updateVisibleEmptyState();
		});

		pohTab.addActionListener(e ->
		{
			setActiveTab(pohTab, bankTab);
			showCard(tabContentPanel, "POH");
			updateVisibleEmptyState();
		});

		tabBar.add(bankTab);
		tabBar.add(pohTab);

		outer.add(tabBar, BorderLayout.NORTH);
		outer.add(tabContentPanel, BorderLayout.CENTER);
		outer.setPreferredSize(new Dimension(0, 420));

		showCard(tabContentPanel, "BANK");
		updateVisibleEmptyState();

		return outer;
	}

	private JScrollPane createListPane(JList<StoredItem> list)
	{
		JScrollPane scroll = new JScrollPane(list);
		scroll.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER),
				BorderFactory.createEmptyBorder(4, 4, 4, 4)
		));
		scroll.setBackground(TABLE_BG);
		scroll.setOpaque(true);
		scroll.getViewport().setBackground(TABLE_BG);
		scroll.getViewport().setOpaque(true);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setPreferredSize(new Dimension(0, 380));
		scroll.setMinimumSize(new Dimension(0, 280));

		scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI()
		{
			@Override
			protected void configureScrollBarColors()
			{
				this.thumbColor = new Color(88, 88, 88);
				this.trackColor = new Color(34, 34, 34);
			}

			@Override
			protected JButton createDecreaseButton(int orientation)
			{
				return createZeroButton();
			}

			@Override
			protected JButton createIncreaseButton(int orientation)
			{
				return createZeroButton();
			}

			private JButton createZeroButton()
			{
				JButton button = new JButton();
				button.setPreferredSize(new Dimension(0, 0));
				button.setMinimumSize(new Dimension(0, 0));
				button.setMaximumSize(new Dimension(0, 0));
				return button;
			}

			@Override
			protected void paintTrack(java.awt.Graphics g, JComponent c, java.awt.Rectangle trackBounds)
			{
				g.setColor(new Color(34, 34, 34));
				g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
			}

			@Override
			protected void paintThumb(java.awt.Graphics g, JComponent c, java.awt.Rectangle thumbBounds)
			{
				if (thumbBounds.isEmpty() || !scroll.getVerticalScrollBar().isEnabled())
				{
					return;
				}

				java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
				g2.setColor(new Color(88, 88, 88));
				g2.fillRoundRect(
						thumbBounds.x + 2,
						thumbBounds.y + 2,
						thumbBounds.width - 4,
						thumbBounds.height - 4,
						8,
						8
				);
				g2.dispose();
			}
		});

		scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, Integer.MAX_VALUE));
		scroll.getVerticalScrollBar().setBackground(new Color(34, 34, 34));

		return scroll;
	}

	private JButton createTabButton(String text, boolean active)
	{
		JButton button = new JButton(text);
		button.setFocusPainted(false);
		button.setFont(FontManager.getRunescapeBoldFont());
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));

		button.setForeground(active ? TAB_ACTIVE_TEXT : TAB_TEXT);
		button.setBackground(active ? TAB_ACTIVE_BG : TAB_BG);
		button.setOpaque(true);
		button.setContentAreaFilled(true);

		button.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(active ? TAB_ACTIVE_BG.brighter() : new Color(70, 70, 70)),
				BorderFactory.createEmptyBorder(7, 16, 7, 16)
		));

		button.addMouseListener(new java.awt.event.MouseAdapter()
		{
			@Override
			public void mouseEntered(java.awt.event.MouseEvent e)
			{
				if (button.getBackground().equals(TAB_ACTIVE_BG))
				{
					return;
				}

				button.setBackground(TAB_HOVER_BG);
				button.setForeground(Color.WHITE);
				button.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(new Color(95, 95, 95)),
						BorderFactory.createEmptyBorder(7, 16, 7, 16)
				));
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent e)
			{
				if (button.getBackground().equals(TAB_ACTIVE_BG))
				{
					return;
				}

				button.setBackground(TAB_BG);
				button.setForeground(TAB_TEXT);
				button.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(new Color(70, 70, 70)),
						BorderFactory.createEmptyBorder(7, 16, 7, 16)
				));
			}
		});

		return button;
	}

	private void setActiveTab(JButton active, JButton inactive)
	{
		active.setBackground(TAB_ACTIVE_BG);
		active.setForeground(TAB_ACTIVE_TEXT);
		active.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(TAB_ACTIVE_BG.brighter()),
				BorderFactory.createEmptyBorder(7, 16, 7, 16)
		));

		inactive.setBackground(TAB_BG);
		inactive.setForeground(TAB_TEXT);
		inactive.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(70, 70, 70)),
				BorderFactory.createEmptyBorder(7, 16, 7, 16)
		));
	}

	private JPanel createEmptyPanel(JLabel label)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(TABLE_BG);
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));

		String text = label.getText();
		label.setText("<html><div style='text-align:center; width:160px;'>" + text + "</div></html>");
		label.setForeground(MUTED);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.NORTH);
		label.setOpaque(false);

		panel.add(label, BorderLayout.NORTH);
		return panel;
	}

	private JPanel createCardPanel()
	{
		JPanel panel = new JPanel();
		panel.setBackground(CARD_BG);
		panel.setBorder(new CompoundBorder(
				BorderFactory.createLineBorder(BORDER),
				BorderFactory.createEmptyBorder(8, 10, 8, 10)
		));
		return panel;
	}

	private void styleSummaryLabel(JLabel label)
	{
		label.setForeground(HEADER);
		label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD, 11f));
	}

	public void refresh()
	{
		rebuildStatusRows();
		plugin.requestPanelRefresh();
		revalidate();
		repaint();
	}

	public void applyRows(List<StoredItem> bankRows, List<StoredItem> pohRows)
	{
		allBankRows = new ArrayList<>(bankRows);
		allPohRows = new ArrayList<>(pohRows);

		applyFilters();
		revalidate();
		repaint();
	}

	private void reloadRows(DefaultListModel<StoredItem> model, List<StoredItem> rows)
	{
		model.clear();

		for (StoredItem item : rows)
		{
			model.addElement(item);
		}
	}

	private void updateSummary()
	{
		int bankRows = bankListModel.getSize();
		int pohRows = pohListModel.getSize();

		bankSummaryLabel.setText("Bank: " + bankRows + " result" + (bankRows == 1 ? "" : "s"));
		pohSummaryLabel.setText("POH: " + pohRows + " result" + (pohRows == 1 ? "" : "s"));
	}

	private void applyFilters()
	{
		String text = searchField.getText();
		String filterText = text == null ? "" : text.trim().toLowerCase();

		List<StoredItem> filteredBank = new ArrayList<>();
		List<StoredItem> filteredPoh = new ArrayList<>();

		for (StoredItem item : allBankRows)
		{
			if (matchesFilter(item, filterText, false))
			{
				filteredBank.add(item);
			}
		}

		for (StoredItem item : allPohRows)
		{
			if (matchesFilter(item, filterText, true))
			{
				filteredPoh.add(item);
			}
		}

		reloadRows(bankListModel, filteredBank);
		reloadRows(pohListModel, filteredPoh);

		updateSummary();
		updateVisibleEmptyState();
	}

	private boolean matchesFilter(StoredItem item, String filterText, boolean includeLocation)
	{
		if (filterText == null || filterText.isEmpty())
		{
			return true;
		}

		if (item.getItemName() != null && item.getItemName().toLowerCase().contains(filterText))
		{
			return true;
		}

		return includeLocation
				&& item.getLocation() != null
				&& item.getLocation().getDisplayName() != null
				&& item.getLocation().getDisplayName().toLowerCase().contains(filterText);
	}

	private void updateVisibleEmptyState()
	{
		showCard(bankResultsCard, bankListModel.getSize() > 0 ? "TABLE" : "EMPTY");
		showCard(pohResultsCard, pohListModel.getSize() > 0 ? "TABLE" : "EMPTY");
	}

	private void showCard(JPanel panel, String name)
	{
		CardLayout cl = (CardLayout) panel.getLayout();
		cl.show(panel, name);
	}

	private void rebuildStatusRows()
	{
		statusRowsPanel.removeAll();

		addStatusRow("Bank", plugin.getSnapshot(StorageLocation.BANK));

		Map<StorageLocation, StorageSnapshot> pohSnapshots = plugin.getSnapshots();
		Map<StorageLocation, StorageSnapshot> safeMap = pohSnapshots == null
				? new EnumMap<>(StorageLocation.class)
				: pohSnapshots;

		for (StorageLocation location : StorageLocation.values())
		{
			if (location == StorageLocation.BANK)
			{
				continue;
			}

			addStatusRow(prettyLocation(location), safeMap.get(location));
		}

		statusRowsPanel.revalidate();
		statusRowsPanel.repaint();
	}

	private void addStatusRow(String label, StorageSnapshot snapshot)
	{
		DataFreshness freshness = getFreshness(snapshot);

		JPanel row = new JPanel(new BorderLayout(8, 0));
		row.setBackground(CARD_BG);
		row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

		JLabel left = new JLabel(label);
		left.setForeground(HEADER);
		left.setFont(left.getFont().deriveFont(12f));

		JLabel right = new JLabel(formatFreshness(freshness));
		right.setForeground(colorForFreshness(freshness));
		right.setFont(right.getFont().deriveFont(java.awt.Font.BOLD, 11f));
		right.setHorizontalAlignment(SwingConstants.RIGHT);
		right.setPreferredSize(new Dimension(90, 18));

		row.add(left, BorderLayout.CENTER);
		row.add(right, BorderLayout.EAST);

		statusRowsPanel.add(row);
	}

	private DataFreshness getFreshness(StorageSnapshot snapshot)
	{
		if (snapshot == null || snapshot.getMetadata() == null)
		{
			return DataFreshness.NEVER_SEEN;
		}

		SnapshotMetadata metadata = snapshot.getMetadata();
		if (metadata.getLastUpdated() <= 0)
		{
			return DataFreshness.NEVER_SEEN;
		}

		long now = System.currentTimeMillis();
		long ageMillis = Math.max(0, now - metadata.getLastUpdated());
		long hours = ageMillis / (1000L * 60L * 60L);

		if (hours < 12)
		{
			return DataFreshness.FRESH;
		}
		else if (hours < 72)
		{
			return DataFreshness.MAYBE_STALE;
		}
		else
		{
			return DataFreshness.OUT_OF_DATE;
		}
	}

	private String formatFreshness(DataFreshness freshness)
	{
		switch (freshness)
		{
			case FRESH:
				return "Fresh";
			case MAYBE_STALE:
				return "Maybe stale";
			case OUT_OF_DATE:
				return "Out of date";
			case NEVER_SEEN:
			default:
				return "Never seen";
		}
	}

	private Color colorForFreshness(DataFreshness freshness)
	{
		switch (freshness)
		{
			case FRESH:
				return FRESH_COLOR;
			case MAYBE_STALE:
				return MAYBE_STALE_COLOR;
			case OUT_OF_DATE:
				return OUT_OF_DATE_COLOR;
			case NEVER_SEEN:
			default:
				return NEVER_SEEN_COLOR;
		}
	}

	private String prettyLocation(StorageLocation location)
	{
		String raw = Objects.toString(location, "");
		raw = raw.replace('_', ' ').toLowerCase();

		StringBuilder sb = new StringBuilder(raw.length());
		boolean capitalize = true;

		for (char c : raw.toCharArray())
		{
			if (capitalize && Character.isLetter(c))
			{
				sb.append(Character.toUpperCase(c));
				capitalize = false;
			}
			else
			{
				sb.append(c);
			}

			if (c == ' ')
			{
				capitalize = true;
			}
		}

		return sb.toString();
	}

	private void startAutoRefreshTimer()
	{
		Timer timer = new Timer(30_000, e -> refresh());
		timer.setRepeats(true);
		timer.start();
	}

	private static class DisabledSelectionModel extends DefaultListSelectionModel
	{
		@Override
		public void setSelectionInterval(int index0, int index1)
		{
		}

		@Override
		public void addSelectionInterval(int index0, int index1)
		{
		}
	}
}