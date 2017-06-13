package org.egordorichev.lasttry.entity.creature;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.egordorichev.lasttry.entity.Entity;
import org.egordorichev.lasttry.entity.components.GraphicsComponent;
import org.egordorichev.lasttry.graphics.Animation;

public class CreatureGraphicsComponent extends GraphicsComponent {
	/**
	 * Creature texture
	 */
	protected TextureRegion texture;
	/**
	 * Creature, that is being served
	 */
	protected Creature creature;

	public CreatureGraphicsComponent() {
		int size = CreatureStateComponent.State.values().length;

		this.animations = new Animation[size];

		for (int i = 0; i < size; i++) {
			this.animations[i] = new Animation(true);
		}
	}

	@Override
	public void setEntity(Entity entity) { // Little hack ;)
		super.setEntity(entity);
		this.creature = (Creature) entity;
	}

	@Override
	public void render() {
		this.animations[this.creature.state.get().getID()].render(this.creature.physics.getX(), this.creature.physics.getY(),
				this.creature.physics.getSize().x, this.creature.physics.getSize().y, !this.creature.physics.isFlipped(), false);
	}

	@Override
	public void update(int dt) {
		this.animations[this.creature.state.get().getID()].update();
	}
}