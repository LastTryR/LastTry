package org.egordorichev.lasttry.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

public class Assets {
	public static BitmapFont f18;
	public static BitmapFont f22;
	public static BitmapFont f24;
	public static TextureAtlas textures;
	public static AssetManager assetManager;

	public static void load() {
		FreetypeFontLoader.FreeTypeFontLoaderParameter fontConfig = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		fontConfig.fontFileName = "font.ttf";
		fontConfig.fontParameters.size = 22;

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameters = new FreeTypeFontGenerator.FreeTypeFontParameter();

		parameters.size = 18;
		f18 = generator.generateFont(parameters);
		parameters.size = 22;
		f22 = generator.generateFont(parameters);
		parameters.size = 24;
		f24 = generator.generateFont(parameters);

		generator.dispose();

		assetManager = new AssetManager();
		assetManager.load("textures.atlas", TextureAtlas.class);
	}

	public static TextureRegion getTexture(String name) {
		return textures.findRegion(name);
	}

	public static boolean update() {
		if (assetManager.update()) {
			textures = assetManager.get("textures.atlas", TextureAtlas.class);
			return true;
		}

		return false;
	}

	public static void dispose() {
		textures.dispose();
		assetManager.dispose();
	}
}