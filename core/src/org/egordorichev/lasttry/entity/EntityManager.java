package org.egordorichev.lasttry.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import org.egordorichev.lasttry.Globals;
import org.egordorichev.lasttry.LastTry;
import org.egordorichev.lasttry.entity.creature.Creature;
import org.egordorichev.lasttry.entity.drop.DroppedItem;
import org.egordorichev.lasttry.graphics.Assets;
import org.egordorichev.lasttry.item.block.Block;
import org.egordorichev.lasttry.util.Callable;
import org.egordorichev.lasttry.util.Camera;
import org.egordorichev.lasttry.util.Rectangle;
import org.egordorichev.lasttry.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EntityManager { // TODO: gore and blood
	public static final int ENEMY_DESPAWN_SWEEP_INTERVAL = 1;
	private static EntityComparator comparator = new EntityComparator();
	/**
	 * List of active entities
	 */
	private List<Entity> entities = new ArrayList<>();
	/**
	 * List of active creatures
	 */
	private List<Creature> creatureEntities = new ArrayList<>();
	/**
	 * List of entities, marked for remove
	 */
	private List<Entity> clearList = new ArrayList<>();

	public EntityManager() {
		Util.runDelayedThreadSeconds(new Callable() {
			@Override
			public void call() {
				attemptDespawnCreatures();
			}
		}, ENEMY_DESPAWN_SWEEP_INTERVAL);
	}

	/**
	 * Renders all entities
	 */
	public void render() {
		boolean displayedStats = false;

		int halfWidth = Gdx.graphics.getWidth() / 2;
		int halfHeight = Gdx.graphics.getHeight() / 2;

		Rectangle camera = new Rectangle(Camera.game.position.x - 16 - halfWidth,
				Camera.game.position.y - 16 - halfHeight, Camera.game.position.x + halfWidth + 32,
				Camera.game.position.y + halfHeight + 32);

		Rectangle mouse = new Rectangle(Gdx.input.getX() + camera.x,
				(Gdx.graphics.getHeight() - Gdx.input.getY()) + camera.y, 16, 16);

		for (Entity entity : this.entities) {
			if (entity.physics.getHitbox().intersects(camera)) {
				entity.render();

				if (!displayedStats && entity.active && entity.physics.getHitbox().intersects(mouse)) {
					if (entity instanceof Creature) {
						Creature creature = ((Creature) entity);

						float x = Gdx.input.getX() + camera.x + 36;
						float y = (Gdx.graphics.getHeight() - Gdx.input.getY()) + camera.y;

						Util.drawWithShadow(Assets.f18, creature.getName(), x, y);
						Util.drawWithShadow(Assets.f18, "HP: " + creature.stats.getHP() + "/" + creature.stats.getMaxHP(), x, y - 20);
						displayedStats = true;
					} else if (entity instanceof DroppedItem) {
						DroppedItem item = ((DroppedItem) entity);
						Util.drawWithShadow(Assets.f18, item.getHolder().asInfo(), Gdx.input.getX() + camera.x + 36, (Gdx.graphics.getHeight() - Gdx.input.getY()) + camera.y);
						displayedStats = true;
					}
				}
			}
		}
	}

	/**
	 * Updates all entities
	 *
	 * @param dt Time, past since last update
	 */
	public void update(int dt) {
		for (Entity entity : this.clearList) {
			this.entities.remove(entity);

			if (entity instanceof Creature) {
				this.creatureEntities.remove(entity);
			}
		}

		this.clearList.clear();

		for (int i = this.entities.size() - 1; i >= 0; i--) {
			Entity entity = this.entities.get(i);
			entity.update(dt);

			if (!entity.isActive() && entity != Globals.getPlayer()) {
				this.clearList.add(entity);
			}

			if (entity instanceof Enemy && entity.physics.getHitbox().intersects(Globals.getPlayer().physics.getHitbox())) {
				((Enemy) entity).onPlayerCollision(Globals.getPlayer());
			}
		}
	}

	/**
	 * Spawns given entity
	 *
	 * @param entity Entity to spawn
	 * @param x      Spawn X (in pixels)
	 * @param y      Spawn Y (in pixels)
	 * @return Given entity
	 */
	public Entity spawn(Entity entity, int x, int y) {
		if (entity == null) {
			return null;
		}

		entity.spawn(x, y);

		this.entities.add(entity);

		if (entity != Globals.getPlayer() && entity instanceof Creature) {
			this.creatureEntities.add((Creature) entity);
		}

		this.sort();

		return entity;
	}

	/**
	 * Spawns block drop
	 *
	 * @param item Drop to spawn
	 * @param x    Spawn X
	 * @param y    Spawn Y
	 * @return Given drop
	 */
	public Entity spawnBlockDrop(DroppedItem item, int x, int y) {
		Entity entity = spawn(item, x, y);

		// calculate velocity to pop dropped item away from blocks
		Vector2 popVelocity = new Vector2();
		float power = 2f;
		int tileX = (x / Block.SIZE);
		int tileY = (y / Block.SIZE);
		for (int k = -1; k <= 1; k++) {
			for (int j = -1; j <= 1; j++) {
				int x1 = tileX + k;
				int y1 = tileY + j;
				if (Globals.getWorld().blocks.get(x1, y1) == null) {
					popVelocity.add(k * power, j * power);
				}
			}
		}
		// Apply pop velocity
		entity.physics.getVelocity().add(popVelocity);
		return entity;
	}

	/**
	 * Sorts entities by z-index
	 */
	private void sort() {
		Collections.sort(this.entities, comparator);
	}

	/**
	 * Marks entity to be removed
	 *
	 * @param entity Entity to be marked
	 */
	public void markForRemoval(Entity entity) {
		this.clearList.add(entity);
	}

	/**
	 * @return All entities
	 */
	public List<Entity> getEntities() {
		return entities;
	}

	/**
	 * @return All creatures
	 */
	public List<Creature> getCreatureEntities() {
		return creatureEntities;
	}

	/**
	 * Attempts to despawn creatures
	 */
	private synchronized void attemptDespawnCreatures() {
		try {
			for (int i = 0; i < this.creatureEntities.size(); i++) {
				Creature creature = creatureEntities.get(i);

				// Acquire a read only lock, source:
				// http://winterbe.com/posts/2015/04/30/java8-concurrency-tutorial-synchronized-locks-examples/
				ReadWriteLock readOnlyLock = new ReentrantReadWriteLock();

				readOnlyLock.readLock().lock();
				creature.tryToDespawn();
				readOnlyLock.readLock().unlock();
			}
		} catch (Exception exception) {
			LastTry.handleException(exception);
		}
	}

	private static class EntityComparator implements Comparator<Entity> {
		@Override
		public int compare(Entity o, Entity t1) {
			if (o.getZIndex() > t1.getZIndex()) {
				return 1;
			} else if (o.getZIndex() < t1.getZIndex()) {
				return -1;
			}

			return 0;
		}
	}
}