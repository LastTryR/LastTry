package org.egordorichev.lasttry.ui.chat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import org.egordorichev.lasttry.Globals;
import org.egordorichev.lasttry.LastTry;
import org.egordorichev.lasttry.entity.Creatures;
import org.egordorichev.lasttry.entity.creature.Creature;
import org.egordorichev.lasttry.graphics.Assets;
import org.egordorichev.lasttry.graphics.Graphics;
import org.egordorichev.lasttry.inventory.ItemHolder;
import org.egordorichev.lasttry.item.Item;
import org.egordorichev.lasttry.state.GamePlayState;
import org.egordorichev.lasttry.ui.UiPanel;
import org.egordorichev.lasttry.ui.UiScreen;
import org.egordorichev.lasttry.ui.UiTextInput;
import org.egordorichev.lasttry.ui.UiToggleScreen;
import org.egordorichev.lasttry.ui.chat.command.CMDCategory;
import org.egordorichev.lasttry.ui.chat.command.Command;
import org.egordorichev.lasttry.ui.chat.command.CommandHandler;
import org.egordorichev.lasttry.util.Util;

import java.util.ArrayList;
import java.util.List;

public class UiChat extends UiPanel implements UiScreen, UiToggleScreen {
	public static final int WIDTH = 400;
	public static final int HEIGHT = 300;
	/**
	 * Command handler used for storing commands and simplifying invocation of
	 * them.
	 */
	private final CommandHandler commands = new CommandHandler();
	private boolean open;
	private UiTextInput input;
	private TextureRegion back;
	/**
	 * Lines to display in chat.
	 */
	private List<ChatLine> lines = new ArrayList<>();

	public UiChat() {
		super(new Rectangle(10, 0, WIDTH, HEIGHT), Origin.BOTTOM_LEFT);
		this.initCommands();
		this.back = Assets.getTexture("chat_back");
	}

	private void initCommands() {
		// TODO: Hide commands based on user permissions.
		this.commands.register(new Command("help", "Shows a list of existing chat commands", CMDCategory.GAME) {
			@Override
			public void onRun(String[] args) {
				// TODO: Hide commands based on user permissions.
				//
				// Streamed sorting + print consumer because it's shorter
				// than a for loop.
				commands.getCommands().stream().sorted(commands.getSorter())
						.forEach((c) -> printf("%-8s - %s\n", c.getHandle(), c.getDescription()));
			}
		});
		this.commands.register(new Command("give", "Gives the player an item", CMDCategory.GAME) {
			@Override
			public void onRun(String[] args) {
				if (args.length != 1 && args.length != 2) {
					print("/give [item id] (count)");
				} else {
					Item item = Item.fromID(args[0]);
					int count = args.length == 1 ? 1 : Integer.valueOf(args[1]);

					if (item == null) {
						print("Unknown item");
					} else {
						Globals.getPlayer().getInventory().add(new ItemHolder(item, count));
					}
				}
			}
		});
		this.commands.register(new Command("spawn", "Spawn in an creature", CMDCategory.DEBUG) {
			@Override
			public void onRun(String[] args) {
				if (args.length != 1 && args.length != 2) {
					print("/spawn [creature name] (count)");
				} else {
					String name = args[0].replace("\"", "");
					Creature creature = Creatures.create(name);
					int count = args.length == 1 ? 1 : Integer.valueOf(args[1]);

					if (creature == null) {
						print("Unknown creature");
					} else {
						for (int i = 0; i < count; i++) {
							Globals.entityManager.spawn(Creatures.create(name), (int) Globals.getPlayer().physics.getX(),
									(int) Globals.getPlayer().physics.getY());
						}
					}
				}
			}
		});
		this.commands.register(new Command("chunks", "Chunk debug information", CMDCategory.DEBUG) {
			@Override
			public void onRun(String[] args) {
				if (args.length == 0) {
					print("/chunks [gc / list]");
				} else {
					switch (args[0]) {
						case "gc":
							Globals.chunkGcManager.scheduleCustomIntervalChunkGcThread(0);
							print("Running instant chunk GC...");
							break;
						case "list":
							print(Globals.chunkGcManager.getCurrentlyLoadedChunks() + " chunks is loaded, maximum is: "
									+ Globals.getWorld().getSize().getMaxChunks());
							break;
						default:
							print("/chunks [gc / list]");
					}
				}
			}
		});
		this.commands.register(new Command("heal", "Heals the player", CMDCategory.ADMININSTRATION) {
			@Override
			public void onRun(String[] args) {
				Globals.getPlayer().stats.modifyHP(+1000);
			}
		});
		this.commands.register(new Command("day", "Sets the time to day", CMDCategory.ADMININSTRATION) {
			@Override
			public void onRun(String[] args) {
				Globals.environment.time.setHour((byte) 4);
				Globals.environment.time.setMinute((byte) 30);
			}
		});
		this.commands.register(new Command("night", "Sets the time to night", CMDCategory.ADMININSTRATION) {
			@Override
			public void onRun(String[] args) {
				Globals.environment.time.setHour((byte) 20);
				Globals.environment.time.setMinute((byte) 30);
			}
		});
		this.commands.register(new Command("light", "Toggles lights", CMDCategory.DEBUG) {
			@Override
			public void onRun(String[] args) {
				LastTry.noLight = !LastTry.noLight;
				// TODO: If light has not been setup, do that.
			}
		});
		this.commands.register(new Command("kill", "Kills the player", CMDCategory.DEBUG) {
			@Override
			public void onRun(String[] args) {
				Globals.getPlayer().die(); // todo: fatal
			}
		});
		this.commands.register(new Command("clear", "Clears your inventory", CMDCategory.DEBUG) {
			@Override
			public void onRun(String[] args) {
				Globals.getPlayer().getInventory().clear();
			}
		});
		this.commands.register(new Command("devset", "Gives you a dev set", CMDCategory.DEBUG) {
			@Override
			public void onRun(String[] args) {
				Globals.getPlayer().getInventory().clear();
				Globals.getPlayer().getInventory().add(new ItemHolder(Item.fromID("lt:superpick"), 1));
				Globals.getPlayer().getInventory().add(new ItemHolder(Item.fromID("lt:copper_shortsword"), 1));
				Globals.getPlayer().getInventory().add(new ItemHolder(Item.fromID("lt:copper_axe"), 1));
			}
		});

		this.commands.register(new Command("startset", "Gives you a start set", CMDCategory.DEBUG) {
			@Override
			public void onRun(String[] args) {
				Globals.getPlayer().getInventory().clear();
				Globals.getPlayer().getInventory().add(new ItemHolder(Item.fromID("lt:copper_shortsword"), 1));
				Globals.getPlayer().getInventory().add(new ItemHolder(Item.fromID("lt:copper_pickaxe"), 1));
				Globals.getPlayer().getInventory().add(new ItemHolder(Item.fromID("lt:copper_axe"), 1));
			}
		});

		this.commands.register(new Command("setspawn", "Sets spawn point to current position", CMDCategory.DEBUG) {
			@Override
			public void onRun(String[] args) {
				Globals.getPlayer().setSpawnPoint(new Vector2(Globals.getPlayer().physics.getGridX(), Globals.getPlayer().physics.getGridY() + 1));
				print("Spawn point set");
			}
		});

		this.commands.register(new Command("tospawn", "TP to spawn", CMDCategory.DEBUG) {
			@Override
			public void onRun(String[] args) {
				Globals.getPlayer().tpToSpawn();
			}
		});

		this.commands.register(new Command("gm", "God mode", CMDCategory.DEBUG) {
			@Override
			public void onRun(String[] args) {
				Globals.getPlayer().stats.setInvulnerableTime(2147483646);
			}
		});
	}

	@Override
	public void addComponents() {
		this.input = new UiTextInput(new Rectangle(15, 25, 400, 20), Origin.BOTTOM_LEFT) {
			@Override
			public void onEnter() {
				String text = getText();
				// FIXME: WTF invisible characters
				//
				// Something is REALLY weird with invisible characters
				if (text.length() >= 3 && text.substring(0, 3).contains("/")) {
					// TODO: Wtf is char(13) doing at the beginning of a string?
					text = text.substring(text.indexOf("/") + 1);
					commands.runInput(text);
				} else {
					// TODO: Player chat
					print("<Player> " + text);
				}

				clear();
			}
		};

		this.input.setFont(Assets.f18);
		this.input.setIgnoreInput(true);

		this.add(this.input);
	}

	@Override
	public void render() {
		if (this.open) {
			Graphics.batch.draw(this.back, 5, 5);
			super.render();
		}

		for (int i = this.lines.size() - 1; i >= 0; i--) {
			ChatLine line = this.lines.get(i);

			if (line.shouldBeRemoved()) {
				this.lines.remove(i);
			}

			Util.drawWithShadow(Assets.f18, line.text, 10, 55 + i * 20);
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			Globals.setScreen(null);
		}
	}

	/**
	 * Prints the objects to chat in the format given.
	 *
	 * @param format Format to use.
	 * @param args   Objects to print.
	 */
	public void printf(String format, Object... args) {
		String text = String.format(format, args);
		print(text);
	}

	/**
	 * Prints the text to chat.
	 *
	 * @param text Text to print.
	 */
	public void print(String text) {
		this.lines.add(0, new ChatLine(text));
	}

	@Override
	public void onUIOpen() {
		this.input.setIgnoreInput(false);
		this.input.type("/");

		GamePlayState.stop();
	}

	@Override
	public void onUIClose() {
		this.input.setIgnoreInput(true);
		this.input.clear();

		GamePlayState.play();
	}

	@Override
	public boolean isOpen() {
		return this.open;
	}

	@Override
	public void setOpen(boolean open) {
		this.open = open;
	}
}