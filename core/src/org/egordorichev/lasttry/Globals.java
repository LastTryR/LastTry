package org.egordorichev.lasttry;

import com.badlogic.gdx.math.Vector2;
import org.egordorichev.lasttry.entity.EntityManager;
import org.egordorichev.lasttry.player.Player;
import org.egordorichev.lasttry.player.PlayerIO;
import org.egordorichev.lasttry.ui.UiScreen;
import org.egordorichev.lasttry.ui.chat.UiChat;
import org.egordorichev.lasttry.world.World;
import org.egordorichev.lasttry.world.WorldIO;
import org.egordorichev.lasttry.world.chunk.gc.ChunkGcManager;
import org.egordorichev.lasttry.world.environment.Environment;
import org.egordorichev.lasttry.world.spawn.SpawnSystem;

public class Globals {
	/**
	 * Handles biomes and events
	 */
	public static Environment environment;
	/**
	 * Handles spawns
	 */
	public static SpawnSystem spawnSystem;
	/**
	 * Screen resolution
	 */
	public static Vector2 resolution;
	/**
	 * The entity manager. Handles rendering, updating, item-dropping, etc. of
	 * all loaded entities.
	 */
	public static EntityManager entityManager;
	/**
	 * The chunk garbage collector.
	 */
	public static ChunkGcManager chunkGcManager;
	/**
	 * The chat UI.
	 */
	public static UiChat chat;
	/**
	 * The current world.
	 */
	private static World world;
	/**
	 * The current player
	 */
	private static Player player;
	/**
	 * The currently displayed screen. If the current screen is null, then the
	 * player is in-game with no overlay menus.
	 */
	private static UiScreen currentScreen;

	/**
	 * Clean up and save player and world data.
	 */
	public static void dispose() {
		if (getPlayer() != null) {
			PlayerIO.save();
		}

		if (getWorld() != null) {
			WorldIO.save();
		}
	}

	/**
	 * Returns the current UiScreen being rendered. If the current screen is
	 * null, then the player is in-game with no overlay menus.
	 *
	 * @return
	 */
	public static UiScreen getCurrentScreen() {
		return currentScreen;
	}

	/**
	 * Sets the current UiScreen. Handles the open values of the current and
	 * to-be current screens.
	 *
	 * @param newScreen Screen to set.
	 */
	public static void setScreen(UiScreen newScreen) {
		if (Globals.currentScreen != null) {
			Globals.currentScreen.setOpen(false);
			Globals.currentScreen.onUIClose();
		}

		if (newScreen != null) {
			newScreen.setOpen(true);
			newScreen.onUIOpen();
		}

		Globals.currentScreen = newScreen;
	}

	/**
	 * @return Current world
	 */
	public static World getWorld() {
		return world;
	}

	/**
	 * Sets current world
	 *
	 * @param world World to be set as current
	 */
	public static void setWorld(World world) {
		Globals.world = world;

		/*
		 * TODO: While not a normal vanilla feature of Terraria, I think having
		 * multiple dimensions would be cool.
		 * Kinda like the Minecraft Mystcraft mod.
		 */
	}

	/**
	 * @return Current player
	 */
	public static Player getPlayer() {
		return player;
	}

	/**
	 * Sets the current player
	 *
	 * @param player Player to be set as current
	 */
	public static void setPlayer(Player player) {
		Globals.player = player;
	}
}
