package com.wheresmystuff;

import java.util.Arrays;
import java.util.List;

public enum StorageLocation
{
	BANK("Bank", false, "bankSnapshot", "bank"),
	MAGIC_WARDROBE("Magic Wardrobe", true, "poh_magicWardrobe", "magic wardrobe", "wardrobe"),
	TOY_BOX("Toy Box", true, "poh_toyBox", "toy box"),
	CAPE_RACK("Cape Rack", true, "poh_capeRack", "cape rack"),
	TREASURE_CHEST("Treasure Chest", true, "poh_treasureChest", "treasure chest"),
	ARMOUR_CASE("Armour Case", true, "poh_armourCase", "armour case", "armor case"),
	COSTUME_ROOM("Costume Room", true, "poh_costumeRoom", "costume room"),
	BOOKCASE("Bookcase", true, "poh_bookcase", "bookcase");

	private final String displayName;
	private final boolean poh;
	private final String configKey;
	private final List<String> keywords;

	StorageLocation(String displayName, boolean poh, String configKey, String... keywords)
	{
		this.displayName = displayName;
		this.poh = poh;
		this.configKey = configKey;
		this.keywords = Arrays.asList(keywords);
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public boolean isPoh()
	{
		return poh;
	}

	public String getConfigKey()
	{
		return configKey;
	}
}
