package com.wheresmystuff;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;

public class StoredItemListCellRenderer extends JPanel implements ListCellRenderer<StoredItem>
{
    private static final Color PANEL_BG = new Color(26, 26, 26);
    private static final Color PANEL_ALT_BG = new Color(31, 31, 31);
    private static final Color PANEL_SELECTED = new Color(60, 88, 126);
    private static final Color BORDER = new Color(48, 48, 48);
    private static final Color NAME_COLOR = new Color(238, 238, 238);
    private static final Color META_COLOR = new Color(165, 165, 165);
    private static final Color VALUE_COLOR = new Color(225, 212, 120);
    private static final Color QTY_BG = new Color(50, 50, 50);
    private static final Color LOCATION_BG = new Color(70, 70, 70);
    private final JLabel loadingLabel = new JLabel("loading");

    private final ItemManager itemManager;
    private final boolean showLocation;

    private final JPanel iconWrap = new JPanel(new BorderLayout());
    private final JLabel iconLabel = new JLabel();

    private final JPanel centerWrap = new JPanel();
    private final JLabel nameLabel = new JLabel();
    private final JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final JLabel qtyBadge = new JLabel();
    private final JLabel valueBadge = new JLabel();
    private final JPanel locationRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final JLabel locationBadge = new JLabel();


    public StoredItemListCellRenderer(ItemManager itemManager, boolean showLocation)
    {
        this.itemManager = itemManager;
        this.showLocation = showLocation;

        setLayout(new BorderLayout(10, 0));
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        iconWrap.setLayout(null);
        iconWrap.setOpaque(true);
        iconWrap.setBackground(new Color(32, 32, 32));
        iconWrap.setBorder(BorderFactory.createLineBorder(new Color(58, 58, 58)));
        iconWrap.setPreferredSize(new Dimension(42, 42));
        iconWrap.setMinimumSize(new Dimension(42, 42));
        iconWrap.setMaximumSize(new Dimension(42, 42));

        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setVerticalAlignment(SwingConstants.CENTER);
        loadingLabel.setForeground(new Color(130, 130, 130));
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(Font.PLAIN, 9f));
        loadingLabel.setBounds(0, 0, 42, 42);

        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        iconLabel.setBounds(5, 5, 32, 32);

        iconWrap.add(loadingLabel);
        iconWrap.add(iconLabel);

        centerWrap.setOpaque(false);
        centerWrap.setLayout(new BoxLayout(centerWrap, BoxLayout.Y_AXIS));

        nameLabel.setForeground(NAME_COLOR);
        nameLabel.setFont(FontManager.getRunescapeBoldFont().deriveFont(Font.PLAIN, 13f));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoRow.setOpaque(false);
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleBadge(qtyBadge, NAME_COLOR, QTY_BG);
        styleBadge(valueBadge, VALUE_COLOR, QTY_BG);

        locationRow.setOpaque(false);
        locationRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleBadge(locationBadge, NAME_COLOR, LOCATION_BG);

        infoRow.add(qtyBadge);
        infoRow.add(valueBadge);

        centerWrap.add(nameLabel);
        centerWrap.add(Box.createVerticalStrut(5));
        centerWrap.add(infoRow);
        centerWrap.add(Box.createVerticalStrut(4));
        centerWrap.add(locationRow);

        add(iconWrap, BorderLayout.WEST);
        add(centerWrap, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends StoredItem> list,
            StoredItem item,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
    {
        Color bg = isSelected ? PANEL_SELECTED : (index % 2 == 0 ? PANEL_BG : PANEL_ALT_BG);
        setBackground(bg);
        iconWrap.setBackground(bg);
        centerWrap.setBackground(bg);
        infoRow.setBackground(bg);
        locationRow.setBackground(bg);

        iconLabel.setIcon(null);
        loadingLabel.setVisible(true);

        try
        {
            BufferedImage image = itemManager.getImage(item.getItemId(), item.getQuantity(), false);
            if (image != null && image.getWidth() > 0 && image.getHeight() > 0)
            {
                iconLabel.setIcon(new ImageIcon(image));
                loadingLabel.setVisible(false);
            }
        }
        catch (Exception e)
        {
            loadingLabel.setVisible(true);
        }

        nameLabel.setText(item.getItemName());
        nameLabel.setToolTipText(item.getItemName());

        qtyBadge.setText("Qty " + format(item.getQuantity()));
        valueBadge.setText(item.hasKnownValue() ? format(item.getTotalValue()) + " gp" : "Value -");

        locationRow.removeAll();
        if (showLocation)
        {
            locationBadge.setText(shortLocation(item.getLocation().getDisplayName()));
            locationRow.add(locationBadge);
            locationRow.setVisible(true);
        }
        else
        {
            locationRow.setVisible(false);
        }

        return this;
    }

    private void styleBadge(JLabel label, Color fg, Color bg)
    {
        label.setOpaque(true);
        label.setForeground(fg);
        label.setBackground(bg);
        label.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 10f));
    }

    private String shortLocation(String name)
    {
        if (name == null)
        {
            return "";
        }

        switch (name)
        {
            case "Magic Wardrobe":
                return "Wardrobe";
            case "Treasure Chest":
                return "Treasure Chest";
            case "Cape Rack":
                return "Cape Rack";
            case "Toy Box":
                return "Toy Box";
            case "Costume Room":
                return "Costume Room";
            case "Armour Case":
                return "Armour Case";
            default:
                return name;
        }
    }

    private String format(long value)
    {
        if (value >= 1_000_000_000L)
        {
            return String.format("%.1fb", value / 1_000_000_000.0);
        }
        if (value >= 1_000_000L)
        {
            return String.format("%.1fm", value / 1_000_000.0);
        }
        if (value >= 1_000L)
        {
            return String.format("%.1fk", value / 1_000.0);
        }
        return Long.toString(value);
    }
}