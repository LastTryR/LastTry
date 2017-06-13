package org.egordorichev.lasttry.item.items;

public class ToolPower {
	public static final ToolPower DEFAULT = new ToolPower(0, 0, 0);
	public short pickaxe;
	public short axe;
	public short hammer;

	public ToolPower(int pickaxe, int axe, int hammer) {
		this.pickaxe = (short) pickaxe;
		this.axe = (short) axe;
		this.hammer = (short) hammer;
	}

	public static ToolPower pickaxe(int power) {
		return new ToolPower(power, 0, 0);
	}

	public static ToolPower axe(int power) {
		return new ToolPower(0, power, 0);
	}

	public static ToolPower hammer(int power) {
		return new ToolPower(0, 0, power);
	}

	public boolean isEnoughFor(ToolPower power) {
		boolean enoughForPickaxe = this.pickaxe >= power.pickaxe;
		boolean enoughForAxe = this.axe >= power.axe;
		boolean enoughForHammer = this.hammer >= power.hammer;

		return enoughForPickaxe && enoughForAxe && enoughForHammer;
	}
}