package org.spongepowered.common.data.type;

import java.awt.Color;

import net.minecraft.item.EnumDyeColor;

import org.spongepowered.api.data.type.DyeColor;

public class SpongeDyeColor implements DyeColor {

	private final String name;
	private final String id;
	private final Color color;

	public SpongeDyeColor(int id, String name) {
		this.id = "dyecolor." + Integer.toString(id);
		this.name = name;
		switch (EnumDyeColor.valueOf(name)) {
		case WHITE: 
			this.color = new Color(0xFFFFFF); break;	
		case BLACK:
			this.color = new Color(0x191919); break;
		case BLUE:
			this.color = new Color(0x334CB2); break;
		case BROWN:
			this.color = new Color(0x664C33); break;
		case CYAN:
			this.color = new Color(0x4C7F99); break;
		case GRAY:
			this.color = new Color(0x4C4C4C); break;
		case GREEN:
			this.color = new Color(0x667F33); break;
		case LIGHT_BLUE:
			this.color = new Color(0x6699D8); break;
		case LIME:
			this.color = new Color(0x7FCC19); break;
		case MAGENTA:
			this.color = new Color(0xB24CD8); break;
		case ORANGE:
			this.color = new Color(0xD87F33); break;
		case PINK:
			this.color = new Color(0xF27FA5); break;
		case PURPLE:
			this.color = new Color(0x7F3FB2); break;
		case RED:
			this.color = new Color(0x993333); break;
		case SILVER:
			this.color = new Color(0x999999); break;
		case YELLOW:
			this.color = new Color(0xE5E533); break;
		default:
			this.color = new Color(0xFFFFFF); break;
		}
	}
	
	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Color getColor() {
		return this.color;
	}

}
