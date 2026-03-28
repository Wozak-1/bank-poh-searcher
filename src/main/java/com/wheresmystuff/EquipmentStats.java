package com.wheresmystuff;

public class EquipmentStats
{
    private final int slot;
    private final boolean twoHanded;

    private final int stab;
    private final int slash;
    private final int crush;
    private final int magic;
    private final int ranged;

    private final int stabDef;
    private final int slashDef;
    private final int crushDef;
    private final int magicDef;
    private final int rangedDef;

    private final int meleeStr;
    private final int rangedStr;
    private final int magicDmg;
    private final int prayer;

    public EquipmentStats(
            int slot,
            boolean twoHanded,
            int stab, int slash, int crush, int magic, int ranged,
            int stabDef, int slashDef, int crushDef, int magicDef, int rangedDef,
            int meleeStr, int rangedStr, int magicDmg, int prayer)
    {
        this.slot = slot;
        this.twoHanded = twoHanded;
        this.stab = stab;
        this.slash = slash;
        this.crush = crush;
        this.magic = magic;
        this.ranged = ranged;
        this.stabDef = stabDef;
        this.slashDef = slashDef;
        this.crushDef = crushDef;
        this.magicDef = magicDef;
        this.rangedDef = rangedDef;
        this.meleeStr = meleeStr;
        this.rangedStr = rangedStr;
        this.magicDmg = magicDmg;
        this.prayer = prayer;
    }

    public boolean hasAnyBonus()
    {
        return stab != 0 || slash != 0 || crush != 0 || magic != 0 || ranged != 0
                || stabDef != 0 || slashDef != 0 || crushDef != 0 || magicDef != 0 || rangedDef != 0
                || meleeStr != 0 || rangedStr != 0 || magicDmg != 0 || prayer != 0;
    }

    public EquipmentStats difference(EquipmentStats equipped)
    {
        if (equipped == null)
        {
            return this;
        }

        return new EquipmentStats(
                slot,
                twoHanded,
                stab - equipped.stab,
                slash - equipped.slash,
                crush - equipped.crush,
                magic - equipped.magic,
                ranged - equipped.ranged,
                stabDef - equipped.stabDef,
                slashDef - equipped.slashDef,
                crushDef - equipped.crushDef,
                magicDef - equipped.magicDef,
                rangedDef - equipped.rangedDef,
                meleeStr - equipped.meleeStr,
                rangedStr - equipped.rangedStr,
                magicDmg - equipped.magicDmg,
                prayer - equipped.prayer
        );
    }

    public int getSlot() { return slot; }
    public boolean isTwoHanded() { return twoHanded; }

    public int getStab() { return this.stab; }
    public int getCrush() { return this.crush; }
    public int getSlash() { return this.slash; }
    public int getMagic() { return this.magic; }
    public int getRanged() { return this.ranged; }

    public int getStabDef() { return this.stabDef; }
    public int getCrushDef() { return this.crushDef; }
    public int getSlashDef() { return this.slashDef; }
    public int getMagicDef() { return this.magicDef; }
    public int getRangedDef() { return this.rangedDef; }

    public int getMeleeStr() { return this.meleeStr; }
    public int getMagicDmg() { return this.magicDmg; }
    public int getRangedStr() { return this.rangedStr; }
    public int getPrayer() { return this.prayer; }
}