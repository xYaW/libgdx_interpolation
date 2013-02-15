package com.cremagames.box2d;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

public class TestSinNada implements ApplicationListener, InputProcessor {
	private OrthographicCamera camera;
	private Box2DDebugRenderer debugRenderer;
	private SpriteBatch batch;
	private World world;
	
	private BoxObject player;
	private BoxObject level;
	private Texture txtPlayer;
	private Texture txtLevel;
	
	private boolean puedeSaltar = true;
	
	@Override
	public void create() {		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 854, 480);
		world = new World(new Vector2(0, -9.8f), true);
		debugRenderer = new Box2DDebugRenderer();
		batch = new SpriteBatch();
		
		Gdx.input.setInputProcessor(this);
		
		//Sprites
		txtPlayer = new Texture(Gdx.files.internal("data/player.png"));
		Sprite sprPlayer = new Sprite(txtPlayer);
		txtLevel = new Texture(Gdx.files.internal("data/nivel.png"));
		Sprite sprLevel = new Sprite(txtLevel);
		
		//level = BoxObject.createBase(world, new Vector2((Gdx.graphics.getWidth() / 2) * Util.WORLD_TO_BOX, 16f * Util.WORLD_TO_BOX), new Vector2((Gdx.graphics.getWidth() / 2) * Util.WORLD_TO_BOX, 16f * Util.WORLD_TO_BOX), sprLevel);

		//player = BoxObject.createPlayer(world, new Vector2(0 * Util.WORLD_TO_BOX, 40 * Util.WORLD_TO_BOX), new Vector2(15 * Util.WORLD_TO_BOX, 15 * Util.WORLD_TO_BOX), sprPlayer);
		
		world.setContactListener(contactListener);
	}

	@Override
	public void dispose() {
		txtPlayer.dispose();
		txtLevel.dispose();
	}
	
	float accumulator = 0;
	float BOX_STEP = 1/30f;

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		Matrix4 cameraCopy = camera.combined.cpy();
	    debugRenderer.render(world, cameraCopy.scl(Util.BOX_TO_WORLD));
	    
	    batch.setProjectionMatrix(camera.combined);
	    batch.begin();
	    //player.getSprite().draw(batch);
	    //level.getSprite().draw(batch);
	    batch.end();
	    
	    updatePhysics(Gdx.graphics.getDeltaTime());
	}
	
	private void updatePhysics(float dt){
		accumulator+=dt;
		
		while(accumulator>BOX_STEP){
			world.step(BOX_STEP, 6, 2);
			accumulator-=BOX_STEP;
			
			//player.updatePosition();
		}
		
		player.getBody().setLinearVelocity(5, player.getBody().getLinearVelocity().y);
	    
		//Comprobamos si el player se sale de la pantalla
	    if(player.getBody().getPosition().x > 854 * Util.WORLD_TO_BOX){
	    	player.getBody().setTransform(0 * Util.WORLD_TO_BOX, 64 * Util.WORLD_TO_BOX, 0);
	    }
	    
	    //Si el player está cayendo, le aplicamos una fuerza
	    if(player.getBody().getLinearVelocity().y < 0 && !puedeSaltar){
	    	player.getBody().applyForce(new Vector2(0, -6.5f), player.getBody().getWorldCenter());
	    }
	}
	
	private ContactListener contactListener = new ContactListener() {
		
		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {
			
		}
		
		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {
			
		}
		
		@Override
		public void endContact(Contact contact) {
			
		}
		
		@Override
		public void beginContact(Contact contact) {
			if((contact.getFixtureA().getBody() == player.getBody()
					|| contact.getFixtureB().getBody() == player.getBody()) && player.getBody().getLinearVelocity().y <= 0){
				puedeSaltar = true;
			}
		}
	};

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	
	//================================================================================
    // InputProcessor
    //================================================================================

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(puedeSaltar){
			player.getBody().applyLinearImpulse(new Vector2(0, 2), player.getBody().getWorldCenter());
			player.getBody().setAngularVelocity(-8.5f);
			player.getBody().setAngularDamping(1);
			puedeSaltar = false;
		}
		
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
