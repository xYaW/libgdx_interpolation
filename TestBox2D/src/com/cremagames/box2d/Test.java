package com.cremagames.box2d;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
	
	private float fixedTimestepAccumulator = 0;
	private float fixedTimestepAccumulatorRatio = 0;

	
	   private final static int MAX_FPS = 45;
       private final static int MIN_FPS = 15;
       public final static float TIME_STEP = 1f / MAX_FPS;
       private final static float MAX_STEPS = 1f + MAX_FPS / MIN_FPS;
       private final static float MAX_TIME_PER_FRAME = TIME_STEP * MAX_STEPS;
       private final static int VELOCITY_ITERS = 6;
       private final static int POSITION_ITERS = 2;
       float physicsTimeLeft;
	
	final int interpolation=0;
	final int semifixed=1;
	final int nothing=2;
	int motor=nothing;
	
	int w;
	int h;
	
	BitmapFont font;
	
	
	@Override
	public void create() {		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		world = new World(new Vector2(0, -9.8f), true);
		world.setAutoClearForces(false);
		debugRenderer = new Box2DDebugRenderer();
		batch = new SpriteBatch();
		
		font = new BitmapFont();
		font.setColor(Color.BLACK);
		
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
		
		player.getBody().setLinearVelocity(5, 0);
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
		
		String motorLabel="";
		switch(motor){
		case interpolation:
			motorLabel="Interpolation";
		    int MAX_STEPS = 5;
		    fixedTimestepAccumulator += Gdx.graphics.getDeltaTime();
		    int nSteps = (int) Math.floor(fixedTimestepAccumulator / TIME_STEP);
		 
		    if (nSteps > 0)
		    {
		        fixedTimestepAccumulator -= nSteps * TIME_STEP;
		    }
		    fixedTimestepAccumulatorRatio = fixedTimestepAccumulator / TIME_STEP;
		    int nStepsClamped = Math.min(nSteps, MAX_STEPS);
		 
		    for (int i = 0; i < nStepsClamped; ++i)
		    {
		    	resetSmoothStates();
		    	singleStep(TIME_STEP);
		    }
		    world.clearForces();
		    smoothStates();
		    break;
		case semifixed:
			motorLabel="semiFixed";
            boolean stepped = fixedStep(Gdx.graphics.getDeltaTime());
            if(stepped) singleStep(TIME_STEP);
            setSrpiteStates();
			break;
		case nothing:
			motorLabel="nothing";
			singleStep(Gdx.graphics.getDeltaTime());
            setSrpiteStates();
            break;
		}

	    
	    batch.setProjectionMatrix(camera.combined);
	    batch.begin();
	    batch.draw(txtBack, 0, 0);
	    player.getSprite().draw(batch);
	    level.getSprite().draw(batch);
	    font.draw(batch,"Motor: "+motorLabel , w/2, h/2);
	    batch.end();
	    
	    
	    
	    
	    //Reset the player if it is offscreen
  	    if(player.getBody().getPosition().x > 800 * Util.WORLD_TO_BOX){
  	    	player.getBody().setTransform(0 * Util.WORLD_TO_BOX, 64 * Util.WORLD_TO_BOX, 0);
  	    }
  	    
	    Matrix4 cameraCopy = camera.combined.cpy();
	    debugRenderer.render(world, cameraCopy.scl(Util.BOX_TO_WORLD));
	}
	


	private boolean fixedStep(float delta) {
		  physicsTimeLeft += delta;
          if (physicsTimeLeft > MAX_TIME_PER_FRAME)
                  physicsTimeLeft = MAX_TIME_PER_FRAME;

          boolean stepped = false;
          while (physicsTimeLeft >= TIME_STEP) {
                  world.step(TIME_STEP, VELOCITY_ITERS, POSITION_ITERS);
                  physicsTimeLeft -= TIME_STEP;
                  stepped = true;
          }
          return stepped;		
	}

	private void smoothStates()
	{
	    float oneMinusRatio = 1.0f - fixedTimestepAccumulatorRatio;
        float posX = fixedTimestepAccumulatorRatio * player.getBody().getPosition().x + (oneMinusRatio * player.getSprite().getPreviousPosition().x);
        float posY = fixedTimestepAccumulatorRatio * player.getBody().getPosition().y + (oneMinusRatio * player.getSprite().getPreviousPosition().y);
        player.getSprite().setPosition(posX * Util.BOX_TO_WORLD - player.getSprite().getWidth() / 2, posY * Util.BOX_TO_WORLD - player.getSprite().getHeight() / 2);
 
        //texture.rotation = box2Dbody.GetAngle() * fixedTimestepAccumulatorRatio + oneMinusRatio * body.previousAngle;
	}
	
	private void setSrpiteStates(){
		   float posX =  player.getBody().getPosition().x ;
	        float posY =  player.getBody().getPosition().y ;
	        player.getSprite().setPosition(posX * Util.BOX_TO_WORLD - player.getSprite().getWidth() / 2, posY * Util.BOX_TO_WORLD - player.getSprite().getHeight() / 2);
	 
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
	    world.step(dt, VELOCITY_ITERS, POSITION_ITERS);
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
		w=width;
		h=height;
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
		if(keycode== Keys.SPACE){
			motor++;
			if(motor>2) motor=0;
		}
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
		if(screenY<h/2){
			motor++;
			if(motor>2) motor=0;
		}
		else if(puedeSaltar){
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