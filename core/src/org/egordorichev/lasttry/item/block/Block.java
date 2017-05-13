package org.egordorichev.lasttry.item.block;

import com.badlogic.gdx.graphics.Texture;
import org.egordorichev.lasttry.Globals;
import org.egordorichev.lasttry.LastTry;
import org.egordorichev.lasttry.entity.drop.DroppedItem;
import org.egordorichev.lasttry.graphics.Graphics;
import org.egordorichev.lasttry.item.Item;
import org.egordorichev.lasttry.item.ItemHolder;
import org.egordorichev.lasttry.item.ItemID;
import org.egordorichev.lasttry.item.items.ToolPower;
import org.egordorichev.lasttry.util.Rectangle;

public class Block extends Item {
    public static final int SIZE = 16;
    public static final byte MAX_HP = 4;

    /** Is the block solid */
    protected boolean solid;

    /** The tool type to use for the block */
    protected ToolPower power;

    /** The block spite-sheet */
    protected Texture tiles;

	/** Block width in tiles */
	protected int width = 1;

    /** Block height in tiles */
    protected int height = 1;

    public Block(short id, String name, boolean solid, ToolPower requiredPower, Texture texture, Texture tiles) {
        super(id, name, texture);
        this.power = requiredPower;
        this.tiles = tiles;
        this.solid = solid;
        this.useSpeed = 30;
    }

    @Override
    public boolean isAutoUse() {
        return true;
    }

	/**
     * Calculates a number based on the edges that have blocks of the same type.
     *
     * @param top    Top edge matches current type.
     * @param right  Right edge matches current type.
     * @param bottom Bottom edge matches current type.
     * @param left   Left edge matches current type.
     * @return
     */
    public static byte calculateBinary(boolean top, boolean right, boolean bottom, boolean left) {
        byte result = 0;

        if (top)
            result += 1;
        if (right)
            result += 2;
        if (bottom)
            result += 4;
        if (left)
            result += 8;

        return result;
    }

    /**
     * Updates the block at given coordinates
     *
     * @param x X-position in the world.
     * @param y Y-position in the world.
     */
    public void updateBlockStyle(int x, int y) {
        /* TODO: if block has animation, update it */
    }

    public void updateBlock(int x, int y) {

    }

    public void onNeighborChange(int x, int y, int nx, int ny) {

    }

    public void die(int x, int y) {
		Globals.entityManager.spawn(new DroppedItem(new ItemHolder(this, 1)), Block.SIZE * x, Block.SIZE * y);
    }

    public boolean canBePlaced(int x, int y) {
    	int dx = (int) Globals.player.physics.getCenterX() / Block.SIZE - x;
    	int dy = (int) Globals.player.physics.getCenterY() / Block.SIZE - y;

    	double length = Math.abs(Math.sqrt(dx * dx + dy * dy));

    	if (length > Globals.player.getItemUseRadius()) {
    		return false;
	    }

	    Block t = Globals.world.blocks.get(x, y + 1);
	    Block b = Globals.world.blocks.get(x, y - 1);
	    Block l = Globals.world.blocks.get(x + 1, y);
	    Block r = Globals.world.blocks.get(x - 1, y);

    	if ((t == null || !t.isSolid()) && (b == null || !b.isSolid()) &&
			    (r == null || !r.isSolid()) && (l == null || !l.isSolid())) {

    		return false;
	    }

    	return true;
    }

    public void place(int x, int y) {
    	Globals.world.blocks.set(this.id, x, y);
    }

    public byte calculateBinary(int x, int y) {
	    boolean t = Globals.world.blocks.getID(x, y + 1) == this.id;
	    boolean r = Globals.world.blocks.getID(x + 1, y) == this.id;
	    boolean b = Globals.world.blocks.getID(x, y - 1) == this.id;
	    boolean l = Globals.world.blocks.getID(x - 1, y) == this.id;
        return Block.calculateBinary(t, r, b, l);
    }

    /**
     * Renders the block at the given coordinates.
     * @param x X-position in the world.
     * @param y Y-position in the world.
     */
    public void renderBlock(int x, int y, byte binary) {
        short variant = 1; // TODO: FIXME: replace with var

	    if (binary == 15) {
            Graphics.batch.draw(this.tiles, x * Block.SIZE,
                y * Block.SIZE, Block.SIZE, Block.SIZE,
                Block.SIZE * (binary), 48 + variant * Block.SIZE, Block.SIZE,
                Block.SIZE, false, false);
        } else {
            Graphics.batch.draw(this.tiles, x * Block.SIZE,
                y * Block.SIZE, Block.SIZE, Block.SIZE,
                Block.SIZE * (binary), variant * Block.SIZE, Block.SIZE,
                Block.SIZE, false, false);
        }

        if (this.renderCracks()) {
	        byte hp = Globals.world.blocks.getHP(x, y);

	        if (hp < Block.MAX_HP) {
				Graphics.batch.draw(Graphics.tileCracks[Block.MAX_HP - hp], x * Block.SIZE, y * Block.SIZE);
	        }
        }
    }

    /** Returns true, if we allowed to draw cracks here */
    protected boolean renderCracks() {
    	return true;
    }

    /**
     * Attempts to place the block in the world at the player's cursor.
     */
    @Override
    public boolean use() {
        int x = LastTry.getMouseXInWorld() / Block.SIZE;
        int y = LastTry.getMouseYInWorld() / Block.SIZE;

        if (this.canBePlaced(x, y) && Globals.world.blocks.getID(x, y) == ItemID.none) {
            Rectangle rectangle = Globals.player.physics.getHitbox();

            if (rectangle.intersects(new Rectangle(x * SIZE, y * SIZE, this.width * SIZE,
		            this.height * SIZE))) {

                return false;
            }

            this.place(x, y);

            return true;
        }

        return false;
    }

    /**
     * Returns required power to break this block
     * @return required power to break this block
     */
    public ToolPower getRequiredPower() {
    	return this.power;
    }

    /**
     * Returns the solidity of the block.
     * @return true if the block is solid.
     */
    public boolean isSolid() {
        return this.solid;
    }

    @Override
    public int getMaxInStack() {
        return 999;
    }
}
