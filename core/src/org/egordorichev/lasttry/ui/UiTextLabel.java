package org.egordorichev.lasttry.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import org.egordorichev.lasttry.graphics.Assets;
import org.egordorichev.lasttry.graphics.Graphics;

public class UiTextLabel extends UiComponent {
	/**
	 * Text, to be drawn on the button
	 */
	protected String label;
	/**
	 * Label width in pixels
	 */
	protected int labelWidth;
	/**
	 * Current font
	 */
	protected BitmapFont font;
	/**
	 * When a specific font style has been requested, we use this private font variable.
	 * So the specific font style will be applied to only this instance.
	 */
	private BitmapFont privateFontInstance;
	/**
	 * A private variable used when a specific is requested for a private instance.
	 */
	private FontStyles fontStyle;

	public UiTextLabel(Rectangle rectangle, UiComponent.Origin origin, String label) {
		super(rectangle, origin);

		this.label = label;
		this.setFont(Assets.f22);

	}

	public UiTextLabel(Rectangle rectangle, String label) {
		this(rectangle, UiComponent.Origin.TOP_LEFT, label);
	}

	/**
	 * Renders the element.  Checks if a specific font style has been set for the element.  If a font style does exist
	 * we create a new font object (if needed) and change the font style for this specific object.
	 */
	@Override
	public void render() {
		if (this.hidden) {
			return;
		}

		super.render();

		this.font.draw(Graphics.batch, this.label, this.getX(),
				this.getY() + (this.getHeight() - this.font.getLineHeight()) / 2);
	}

	/**
	 * Sets font and calculates new size
	 */
	public void setFont(BitmapFont font) {
		this.font = font;
		this.setLabel(this.label);
	}

	/**
	 * Calculates new size
	 */
	public void setLabel(String label) {
		this.label = label;

		GlyphLayout glyphLayout = new GlyphLayout();
		glyphLayout.setText(this.font, this.label);

		this.labelWidth = (int) glyphLayout.width;
		this.rect.width = this.labelWidth;
		this.rect.height = this.font.getLineHeight() * 1.f;
	}

	/**
	 * Sets the fontStyle to be applied to the font, for this specific instance.
	 *
	 * @param fontStyle Enum representing requested fontStyle.
	 */
	public void setFontStyle(final FontStyles fontStyle) {
		this.fontStyle = fontStyle;
	}

	public enum FontStyles {
		ERRORMESSAGE;
	}
}