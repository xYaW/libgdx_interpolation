package com.cremagames.box2d;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class PhysicsComponent extends Sprite{
	
	private float previousAngle;
	private float smoothedAngle;
	private Vector2 previousPosition;
	private Vector2 smoothedPosition;
	
	public PhysicsComponent(Texture texture) {
		super(texture);
	}
	
	public float getPreviousAngle() {
		return previousAngle;
	}

	public void setPreviousAngle(float previousAngle) {
		this.previousAngle = previousAngle;
	}

	public float getSmoothedAngle() {
		return smoothedAngle;
	}

	public void setSmoothedAngle(float smoothedAngle) {
		this.smoothedAngle = smoothedAngle;
	}

	public Vector2 getPreviousPosition() {
		return previousPosition;
	}

	public void setPreviousPosition(Vector2 previousPosition) {
		this.previousPosition = previousPosition;
	}

	public Vector2 getSmoothedPosition() {
		return smoothedPosition;
	}

	public void setSmoothedPosition(Vector2 smoothedPosition) {
		this.smoothedPosition = smoothedPosition;
	}

}
