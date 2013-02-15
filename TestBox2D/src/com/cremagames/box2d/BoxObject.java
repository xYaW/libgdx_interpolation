package com.cremagames.box2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class BoxObject {
	
	private Body body;
	private Vector2 size;
	private PhysicsComponent sprite;
	
	public BoxObject(Body body, Vector2 size, PhysicsComponent sprite){
		this.body = body;
		this.size = size;
		this.sprite = sprite;
		
		sprite.setPreviousAngle(body.getAngle());
		sprite.setSmoothedAngle(body.getAngle());
		sprite.setPreviousPosition(body.getPosition());
		sprite.setSmoothedPosition(body.getPosition());
	}
	
	public static BoxObject createPlayer(World world, Vector2 position, Vector2 size, PhysicsComponent sprite){
		// the player box
		BodyDef playerDef = new BodyDef();
		playerDef.type = BodyType.DynamicBody;
		playerDef.position.set(position);
		Body playerBody = world.createBody(playerDef);
		playerBody.setUserData(sprite);

		PolygonShape playerShape = new PolygonShape();
		playerShape.setAsBox(size.x, size.y);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = playerShape;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.0f;
		fixtureDef.restitution = 0.0f;
		playerBody.createFixture(fixtureDef);
		
		playerShape.dispose();
		
		return new BoxObject(playerBody, size, sprite);
	}
	
	public static BoxObject createBase(World world, Vector2 position, Vector2 size, PhysicsComponent sprite){
		BodyDef groundDef = new BodyDef();
		groundDef.type = BodyType.StaticBody;
		groundDef.position.set(position);
		Body groundBody = world.createBody(groundDef);
		groundBody.setUserData(sprite);
		PolygonShape groundShape = new PolygonShape();
		groundShape.setAsBox(size.x, size.y);
		groundBody.createFixture(groundShape, 0f);
		groundShape.dispose();
		
		return new BoxObject(groundBody, size, sprite);
	}
	
	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public Vector2 getSize() {
		return size;
	}

	public void setSize(Vector2 size) {
		this.size = size;
	}

	public PhysicsComponent getSprite() {
		return sprite;
	}

	public void setSprite(PhysicsComponent sprite) {
		this.sprite = sprite;
	}

}
