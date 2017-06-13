package org.egordorichev.lasttry.entity.drop;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import org.egordorichev.lasttry.Globals;
import org.egordorichev.lasttry.entity.Entity;
import org.egordorichev.lasttry.graphics.Graphics;
import org.egordorichev.lasttry.inventory.ItemHolder;

import java.util.List;

public class DroppedItem extends Entity {
	private static final float ATTRACTION_RANGE = 60;
	/**
	 * Holder with item, that was dropped
	 */
	private final ItemHolder holder;
	/**
	 * Item texture
	 */
	private TextureRegion texture;

	public DroppedItem(ItemHolder holder) {
		super();

		this.holder = holder;
		this.texture = this.holder.getItem().getTextureRegion();
		this.physics.setSize(this.texture.getRegionWidth(), this.texture.getRegionHeight());
	}

	@Override
	public void render() {
		Graphics.batch.draw(this.texture, this.physics.getX(), this.physics.getY());
	}

	@Override
	public void update(int dt) {
		this.physics.update(dt);
		this.graphics.update(dt);
		if (this.holder.getItem().isUnobtainable()) {
			Globals.entityManager.markForRemoval(this);
			return;
		}
		this.updateAttraction(dt);
		this.checkPlayerAbsorbtion(dt);
		this.packRelated(dt);
	}

	/**
	 * Check if the item should be absorbed by the player.
	 *
	 * @param dt
	 */
	private void checkPlayerAbsorbtion(int dt) {
		if (this.physics.getHitbox().intersects(Globals.getPlayer().physics.getHitbox())) {
			if (this.holder.getItem().getID().equals("lt:heart")) {
				Globals.getPlayer().stats.modifyHP(20 * this.holder.getCount());
			} else if (this.holder.getItem().getID().equals("lt:mana")) {
				Globals.getPlayer().stats.modifyMana(20 * this.holder.getCount());
			} else {
				Globals.getPlayer().getInventory().add(this.holder);
			}

			Globals.entityManager.markForRemoval(this);
		}
	}

	/**
	 * Update's velocity towards the player.
	 *
	 * @param dt
	 */
	private void updateAttraction(int dt) {
		Vector2 p1 = Globals.getPlayer().physics.getPosition().cpy();
		Vector2 p2 = physics.getPosition().cpy();
		float dist = p1.dst(p2);
		boolean inRange = dist < ATTRACTION_RANGE;

		if (inRange) {
			float attraction = 100f;
			float distPow = (float) Math.pow(dist, 2);
			this.physics.getVelocity().add(p2.sub(p1).scl(-attraction, -attraction).scl(1f / distPow));
		}

		this.physics.setSolid(!inRange);
	}

	/**
	 * Packs items of the same type into a single item.
	 *
	 * @param dt Time, since last update
	 */
	private void packRelated(int dt) {
		List<Entity> entities = Globals.entityManager.getEntities();
		entities.stream().filter(e -> !e.equals(this))
				.filter(e -> e.isActive())
				.filter(e -> physics.getHitbox().intersects(e.physics.getHitbox()))
				.filter(e -> e instanceof DroppedItem).map(e -> ((DroppedItem) e))
				.filter(i -> holder.getItem().getID().equals(i.holder.getItem().getID())).forEach(i -> consume(i));
	}

	/**
	 * Merges this and the other item together.
	 *
	 * @param other
	 */
	private void consume(DroppedItem other) {
		/*
		 * other.die() causes BOTH items to die
		 * this.die() causes only THIS item to die
		 *
		 * what??
		 */

		int sum = this.holder.getCount() + other.holder.getCount();

		if (sum <= other.holder.getItem().getMaxInStack()) {
			other.holder.setCount(sum);
			active = false;
			Globals.entityManager.markForRemoval(this);
		}
	}

	/**
	 * @return Item holder
	 */
	public ItemHolder getHolder() {
		return holder;
	}

	@Override
	public void spawn(int x, int y) {
		super.spawn(x, y);
		this.physics.setPosition(x, y);
	}
}