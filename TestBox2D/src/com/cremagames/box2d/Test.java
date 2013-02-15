package com.cremagames.box2d;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

public class Test implements ApplicationListener, InputProcessor {
	private OrthographicCamera camera;
	private Box2DDebugRenderer debugRenderer;
	private SpriteBatch batch;
	private World world;
	
	private BoxObject player;
	private BoxObject level;
	private Texture txtPlayer;
	private Texture txtLevel;
	private Texture txtBack;
	
	private boolean puedeSaltar = true;
	
	private final float FIXED_TIMESTEP = 1f/60f;
	private float fixedTimestepAccumulator = 0;
	private float fixedTimestepAccumulatorRatio = 0;
	private int velocityIterations = 6;
	private int positionIterations = 1;
	
	@Override
	public void create() {		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		world = new World(new Vector2(0, -9.8f), true);
		world.setAutoClearForces(false);
		debugRenderer = new Box2DDebugRenderer();
		batch = new SpriteBatch();
		
		Gdx.input.setInputProcessor(this);
		
		//Sprites
		txtPlayer = new Texture(Gdx.files.internal("data/player.png"));
		PhysicsComponent phPlayer = new PhysicsComponent(txtPlayer);
		txtLevel = new Texture(Gdx.files.internal("data/nivel.png"));
		PhysicsComponent phLevel = new PhysicsComponent(txtLevel);
		txtBack = new Texture(Gdx.files.internal("data/angrysky.png"));
		
		level = BoxObject.createBase(world, new Vector2((Gdx.graphics.getWidth() / 2) * Util.WORLD_TO_BOX, 16f * Util.WORLD_TO_BOX), new Vector2((Gdx.graphics.getWidth() / 2) * Util.WORLD_TO_BOX, 16f * Util.WORLD_TO_BOX), phLevel);

		player = BoxObject.createPlayer(world, new Vector2(0 * Util.WORLD_TO_BOX, 40 * Util.WORLD_TO_BOX), new Vector2(15 * Util.WORLD_TO_BOX, 15 * Util.WORLD_TO_BOX), phPlayer);
		
		world.setContactListener(contactListener);
		
		player.getBody().setLinearVelocity(8, 0);
	}

	@Override
	public void dispose() {
		txtPlayer.dispose();
		txtLevel.dispose();
		txtBack.dispose();
	}
	
	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
	    int MAX_STEPS = 5;
	    fixedTimestepAccumulator += Gdx.graphics.getDeltaTime();
	    int nSteps = (int) Math.floor(fixedTimestepAccumulator / FIXED_TIMESTEP);
	 
	    if (nSteps > 0)
	    {
	        fixedTimestepAccumulator -= nSteps * FIXED_TIMESTEP;
	    }
	    fixedTimestepAccumulatorRatio = fixedTimestepAccumulator / FIXED_TIMESTEP;
	    int nStepsClamped = Math.min(nSteps, MAX_STEPS);
	 
	    for (int i = 0; i < nStepsClamped; ++i)
	    {
	    	resetSmoothStates();
	    	singleStep(FIXED_TIMESTEP);
	    }
	    world.clearForces();
	    smoothStates();
	    
	    batch.setProjectionMatrix(camera.combined);
	    batch.begin();
	    batch.draw(txtBack, 0, 0);
	    player.getSprite().draw(batch);
	    level.getSprite().draw(batch);
	    batch.end();
	    
	    //Reset the player if it is offscreen
  	    if(player.getBody().getPosition().x > 800 * Util.WORLD_TO_BOX){
  	    	player.getBody().setTransform(0 * Util.WORLD_TO_BOX, 64 * Util.WORLD_TO_BOX, 0);
  	    }
  	    
	    Matrix4 cameraCopy = camera.combined.cpy();
	    debugRenderer.render(world, cameraCopy.scl(Util.BOX_TO_WORLD));
	}
	
	private void smoothStates()
	{
	    float oneMinusRatio = 1.0f - fixedTimestepAccumulatorRatio;
	 
        float posX = fixedTimestepAccumulatorRatio * player.getBody().getPosition().x + (oneMinusRatio * player.getSprite().getPreviousPosition().x);
        float posY = fixedTimestepAccumulatorRatio * player.getBody().getPosition().y + (oneMinusRatio * player.getSprite().getPreviousPosition().y);
        player.getSprite().setPosition(posX * Util.BOX_TO_WORLD - player.getSprite().getWidth() / 2, posY * Util.BOX_TO_WORLD - player.getSprite().getHeight() / 2);
 
        //texture.rotation = box2Dbody.GetAngle() * fixedTimestepAccumulatorRatio + oneMinusRatio * body.previousAngle;
	}
	 
	private void resetSmoothStates()
	{
        float posX = player.getSprite().getPreviousPosition().x = player.getBody().getPosition().x;
        float posY = player.getSprite().getPreviousPosition().y = player.getBody().getPosition().y;
        player.getSprite().setPosition(posX * Util.BOX_TO_WORLD, posY * Util.BOX_TO_WORLD);
 
        //texture.rotation = body.previousAngle = body.box2Dbody.GetAngle();
	}
	
	private void singleStep(float dt)
	{
	    world.step(dt, velocityIterations, positionIterations);
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
