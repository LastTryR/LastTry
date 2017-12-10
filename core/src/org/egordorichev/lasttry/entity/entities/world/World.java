package org.egordorichev.lasttry.entity.entities.world;

import org.egordorichev.lasttry.entity.asset.Assets;
import org.egordorichev.lasttry.entity.component.IdComponent;
import org.egordorichev.lasttry.entity.component.SizeComponent;
import org.egordorichev.lasttry.entity.engine.system.systems.CameraSystem;
import org.egordorichev.lasttry.entity.entities.camera.CameraComponent;
import org.egordorichev.lasttry.entity.entities.item.tile.Block;
import org.egordorichev.lasttry.entity.entities.item.tile.Wall;
import org.egordorichev.lasttry.entity.entities.world.chunk.Chunk;
import org.egordorichev.lasttry.graphics.Display;

/**
 * Handles chunk rendering
 */
public class World extends ChunkManager {
	/**
	 * Static instance
	 */
	public static World instance;

	public World(String name, String type) {
		instance = this;
		IdComponent id = (IdComponent) this.addComponent(IdComponent.class);
		id.id = name + ":" + type;

		/*
		 * Setup clock
		 */
		ClockComponent clock = (ClockComponent) this.addComponent(ClockComponent.class);
		clock.speed = 60.0f;
	}

	/**
	 * Renders the world
	 */
	@Override
	public void render() {
		CameraComponent cam = CameraSystem.instance.get("main").getComponent(CameraComponent.class);

		short xStart = (short) Math.floor((cam.camera.position.x - Display.WIDTH / 2) / Block.SIZE);
		short yStart = (short) Math.floor((cam.camera.position.y - Display.HEIGHT / 2) / Block.SIZE);
		short width = (short) (Math.floor(Display.WIDTH / Block.SIZE) + 2);
		short height = (short) (Math.floor(Display.HEIGHT / Block.SIZE) + 2);

		for (short x = xStart; x < xStart + width; x++) {
			for (short y = yStart; y < yStart + height; y++) {
				String blockId = this.getBlock(x, y);
				Block block = null;
				int neighbors = 15;
				float light = this.getLight(x, y);

				if (blockId != null) {
					block = (Block) Assets.items.get(blockId);
					neighbors = block.getNeighbors(x, y);
				}

				if (blockId == null || neighbors != 15) {
					String wallId = this.getWall(x, y);

					if (wallId != null) {
						Wall wall = (Wall) Assets.items.get(wallId);
						wall.render(x, y, light);
					}
				}

				if (block != null) {
					block.render(x, y, neighbors, light);
				}
			}
		}
	}

	/**
	 * Returns highest block Y at given X
	 *
	 * @param x Block X
	 * @return Returns highest block Y at given X
	 */
	public short getHighest(short x) {
		SizeComponent size = this.getComponent(SizeComponent.class);

		if (x >= size.width * Chunk.SIZE || x < 0) {
			return 0;
		}

		for (short y = (short) (size.height * Chunk.SIZE); y >= 0; y--) {
			if (this.getBlock(x, y) != null) {
				return y;
			}
		}

		return 0;
	}
}