/*
Nick Chace
Malkylm Wright
CSC 165-02
Scott Gordon

Final Projects - Monster Battles:

Controls for Dolphin:
Keyboard:
	W/2 - Moves Dolphin Forward
	S/3 - Moves Dolphin Backward
	A - Yaws Dolphin Left
	D - Yaws Dolphin Right
	Up Arrow - Pitches Dolphin Forward
	Down Arrow - Pitches Dolphin Downward


Controls for Camera:
Keyboard: (Sky/Overhead Camera)
	I - Zooms the sky camera in
	O - Zooms the sky camera out
	U - Pans sky camera North
	H - Pans sky camera West
	J - Pans sky camera South
	K - Pans sky camera East
	1  - Toggles Visibility of World Axes
Controller DualSense: (Orbital Controller)
	Right Joy Stick Up and Down - Radial motion, zooms in and out
	Right Joy Stick Left and Right - Azimuthal motion, rotates around avatar
	Left Joy Stick Up and Down - Elevation, increases elevation above/below avatar

In this version, the lava pool is bigger, objects rotate upon visiting, and the chest blinks upon collection of all items.


*/
//Package
package a2;

//Imports
import tage.*;
import tage.shapes.*;

import tage.input.*;
import tage.input.action.*;
import tage.nodeControllers.InvisibilityController;
import tage.nodeControllers.RotationController;
import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import tage.audio.*;
import tage.physics.*;
import tage.physics.JBullet.*;

import java.lang.Math;
import java.util.Random;
//import java.awt.*;
import java.awt.event.*;
import java.io.*;
//import java.util.*;
import javax.swing.*;
import org.joml.*;
//import org.w3c.dom.events.Event;
import java.net.InetAddress;
import java.net.UnknownHostException;
import tage.networking.IGameConnection.ProtocolType;

public class MyGame extends VariableFrameRateGame
{
	//Fields
	private static Engine engine; //Game engine object
	

	private boolean paused=false, punching = false, onDolphin=true, colliding=false, visitedObj1=false, visitedObj2=false, visitedObj3=false, visitedObj4=false, gameOver=false, allVisited=false, touchingLava=false, axisLines=true, visitLock1=false, visitLock2=false, visitLock3=false, visitLock4=false, isClientConnected=false;
	private int counter=0, score=0, holder=0;
	private float rotationAmount = 0, moveSpd = 2, turnSpd = 2, toggleSpd;
	private double lastFrameTime, currFrameTime, elapsTime, timePassed;
	private Random rand = new Random();
	private long holdTimeHitBox = 0;
	private boolean countingHitBox = false;

	//Input Stuff
	private InputManager im;
	private GhostManager gm;

	//Orbit stuff
	private Camera camera;
	private Camera secondaryCam;
	private CameraOrbit3D orbitController;
	private CameraController sCamController;

	//Node Controllers
	private NodeController ic, rc1, rc2, rc3, rc4;
	//private InvisibilityController ic;

	//Viewport Stuff
	Viewport mainVP;
	Viewport secondVP;
	private int mainVLeft;
	private int mainVRight;
	private int mainVBot;
	private int mainVTop;
	private int secVLeft;
	private int secVRight;
	private int secVBot;
	private int secVTop;

	private GameObject dol;
	//GameObject for ground, and terrain
	private GameObject ground, terrain, terrain2;
	//GameObjects for scattered items
	private GameObject objCube, objSphere, objTorus, objPlane, objDiamond, objLava, objChest, objXLine, objYLine, objZLine, objGhost, objSpider, objPlayerPunchBox;
	//Refridgerator magnets
	private GameObject objMagnet1, objMagnet2, objMagnet3, objMagnet4;
	//Fully Functional Player Objects
	private GameObject objPlayer, objDummy, objDummy2;
	

	private ObjShape dolS;
	//Shape for ground and terrain
	private ObjShape groundS, terrainS, terrain2S;
	//ObjShapes for scattered items
	private ObjShape cubeS, sphereS, torusS, planeS, diamondS, lavaS, chestS, axisS, xS, yS, zS, ghostS, spiderS, playerPunchS;
	private ObjShape magnetS;
	//Shape for player model
	private ObjShape playerS;
	//NPC Stuff
	private ObjShape npcS;
	//Shape for animated Player actions
	private AnimatedShape animPlayerS, animGhostS;

	private TextureImage doltx;
	//Texture image for ground, terrain, and terrain height, and testing texture
	private TextureImage groundTx, terrainTx, hillsTx, grass2Tx, gravelTx, water2Tx;
	//Texture image for scattered items
	private TextureImage cubeTx, sphereTx, torusTx, planeTx, diamondTx, lavaTx, chestTx, xTx, yTx, zTx, ghostTx, playerPunchTx;
	private TextureImage magnet1Tx, magnet2Tx, magnet3Tx, magnet4Tx;
	//Finished Player Textures
	private TextureImage playerRedTx, playerBlueTx, playerGreenTx, playerCyanTx, playerTanTx, playerBlankTx;
	//Texture for spider
	private TextureImage spiderTx;
	//Texture for NPC
	private TextureImage npcTx;

	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;

	private Light light1, light2;
	private boolean lightToggle = false;
	private boolean isRunning = false;
	private boolean choosingModel = true;
	private int health = 100;
	
	//Skybox
	private int fluffyClouds;
	
	//Terrain following
	private Vector3f lastLocation;
	
	//Audio
	private IAudioManager audioManager;
	private Sound whistleSound, runSound, punchSound, spiderSound, backgroundMusic; 
	
	//Physics
	private PhysicsEngine physicsEngine;
	private PhysicsObject planePhys, borderPhys, playerCapsulePhys, ghostCapsulePhys, punchPhys, spiderPhys,
			northWallPhys, southWallPhys, EastWallPhys, westWallPhys, goalOnePhys, goalTwoPhys, ballPhys;
	private boolean runningPhysics = false;
	private float vals[] = new float[16];

	public MyGame(String serverAddress, int serverPort, String protocol) {
		super(); 
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}
	

	public static void main(String[] args)
	{	MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]); //Creates game object
		engine = new Engine(game); //Puts game into engine object created earlier
		game.initializeSystem(); //Runs game initalization step
		game.game_loop(); //Stars game loop (update)
	}

	@Override
	/**Load all game shapes into engine */
	public void loadShapes()
	{	dolS = new ImportedModel("dolphinHighPoly.obj");
		cubeS = new Cube();
		magnetS = new Cube();
		groundS = new Plane();
		xS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(400f, 0f, 0f));
		yS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 400f, 0f));
		zS = new Line(new Vector3f(0f, 0f, 0f), new Vector3f(0f, 0f, 400f));
		chestS = new Cube();
		torusS = new Torus();
		planeS = new Plane();
		sphereS = new Sphere();
		lavaS = new Plane();
		diamondS = new ManualDiamond();
		ghostS = new ImportedModel("dolphinHighPoly.obj");
		playerS = new ImportedModel("ninja_lowpoly.obj");
		spiderS = new ImportedModel("spider.obj");
		playerPunchS = new Cube();
		terrainS = new TerrainPlane(1000);
		terrain2S = new Plane();
		
		//NPC Stuff
		npcS = new Cube();
		
		animPlayerS = new AnimatedShape("dummy.rkm", "dummy.rks");
		animPlayerS.loadAnimation("RUN", "run.rka");
		animPlayerS.loadAnimation("PUNCH", "punch.rka");
		
		animGhostS = new AnimatedShape("dummy.rkm", "dummy.rks");
		animGhostS.loadAnimation("RUN", "run.rka");
		animGhostS.loadAnimation("PUNCH", "punch.rka");
	}

	@Override
	/**Load all textures into engine */
	public void loadTextures()
	{	doltx = new TextureImage("Dolphin_HighPolyUV.png");
		cubeTx = new TextureImage("cubeTexture.jpg");
		torusTx = new TextureImage("flowersTexture.jpg");
		planeTx = new TextureImage("canTexture.png");
		sphereTx = new TextureImage("faceTexture.png");
		groundTx = new TextureImage("waterTexture.png");
		magnet1Tx = new TextureImage("magnet1Texture.png");
		magnet2Tx = new TextureImage("magnet2Texture.png");
		magnet3Tx = new TextureImage("magnet3Texture.png");
		magnet4Tx = new TextureImage("magnet4Texture.png");
		lavaTx = new TextureImage("lavaTexture.png");
		chestTx = new TextureImage("chest.png");
		xTx = new TextureImage("xAxis.png");
		yTx = new TextureImage("yAxis.png");
		zTx = new TextureImage("zAxis.png");
		playerPunchTx = new TextureImage("zAxis.png");
		diamondTx = new TextureImage("goldTexture.jpg");
		ghostTx = new TextureImage("redDolphin.jpg");
		hillsTx = new TextureImage("hills.png");
		grass2Tx = new TextureImage("grass2.jpg");
		playerRedTx = new TextureImage("Ninja_uvmapNEW_RED.png");
		playerBlueTx = new TextureImage("Ninja_uvmapNEW_BLUE.png");
		playerGreenTx = new TextureImage("Ninja_uvmapNEW_GREEN.png");
		playerCyanTx = new TextureImage("Ninja_uvmapNEW_CYAN.png");
		playerTanTx = new TextureImage("Ninja_uvmapNEW_Tan.png");
		playerBlankTx = new TextureImage("Ninja_uvmap.png");
		gravelTx = new TextureImage("100_1450_seamless.JPG"); //https://opengameart.org/content/seamless-textures 
		spiderTx = new TextureImage("spider-uv.png");
		npcTx = new TextureImage("spider-uv.png");
		water2Tx = new TextureImage("water2.jpg");
	}

	@Override
	/**Build the declared objects */
	public void buildObjects()
	{	Matrix4f initialTranslation, initialScale, initialRotation, rotateX, rotateY, rotateZ;
		Vector3f xAxis, yAxis, zAxis;
	 

		// build dolphin in the center of the window
		dol = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(0,1f,0);
		initialScale = (new Matrix4f()).scaling(3.0f);
		initialRotation = (new Matrix4f()).rotation(0, 0, 0, 0);
		dol.setLocalTranslation(initialTranslation);
		dol.setLocalScale(initialScale);
		dol.setLocalRotation(initialRotation);
		
		//Ground Object
		
		ground = new GameObject(GameObject.root(), groundS, groundTx);
		initialTranslation = (new Matrix4f()).translation(0, -20, 0);
		initialScale = (new Matrix4f()).scaling(1.0f);
		ground.setLocalTranslation(initialTranslation);
		ground.setLocalScale(initialScale);

		//Build cube 
		objCube = new GameObject(GameObject.root(), cubeS, cubeTx); //Create new game object
		initialTranslation = (new Matrix4f()).translation(getSpawnCoord(), 2, getSpawnCoord()); //Create the initial transformation matrices (in this sense its the initial translation and the initial scaling of the cube)
		initialScale = (new Matrix4f()).scaling(2f);
		objCube.setLocalTranslation(initialTranslation); //Actually set the different transformations
		objCube.setLocalScale(initialScale);

		//Axis spawn
		objXLine = new GameObject(GameObject.root(), xS, xTx);
		(objXLine.getRenderStates()).setColor(new Vector3f(1f, 0f, 0f));
		//initialScale = (new Matrix4f()).scaling(400f, 0.05f, 0.05f);
		//objXLine.setLocalScale(initialScale);

		objYLine = new GameObject(GameObject.root(), yS, yTx);
		(objYLine.getRenderStates()).setColor(new Vector3f(0f, 0f, 1f));
		//initialScale = (new Matrix4f()).scaling(0.05f, 400f, 0.05f);
		//objYLine.setLocalScale(initialScale);

		objZLine = new GameObject(GameObject.root(), zS, zTx);
		(objZLine.getRenderStates()).setColor(new Vector3f(0f, 1f, 0f));
		//initialScale = (new Matrix4f()).scaling(0.05f, 0.05f, 400f);
		//objZLine.setLocalScale(initialScale);

		//Chest Object
		objChest = new GameObject(GameObject.root(), chestS, chestTx);
		initialTranslation = (new Matrix4f()).translation(2, 0.5f, 2);
		initialScale = (new Matrix4f()).scale(0.5f);
		objChest.setLocalTranslation(initialTranslation);
		objChest.setLocalScale(initialScale);

		//Magnet seriess
		objMagnet1 = new GameObject(GameObject.root(), magnetS, magnet1Tx);
		initialTranslation = (new Matrix4f()).translation(2, 0, 2);
		initialScale = (new Matrix4f()).scaling(0.08f, 0.1f, 0.025f);
		objMagnet1.setLocalTranslation(initialTranslation);
		objMagnet1.setLocalScale(initialScale);

		objMagnet2 = new GameObject(GameObject.root(), magnetS, magnet2Tx);
		initialTranslation = (new Matrix4f()).translation(2, 0, 2);
		initialScale = (new Matrix4f()).scaling(0.08f, 0.1f, 0.025f);
		objMagnet2.setLocalTranslation(initialTranslation);
		objMagnet2.setLocalScale(initialScale);

		objMagnet3 = new GameObject(GameObject.root(), magnetS, magnet3Tx);
		initialTranslation = (new Matrix4f()).translation(2, 0, 2);
		initialScale = (new Matrix4f()).scaling(0.08f, 0.1f, 0.025f);
		objMagnet3.setLocalTranslation(initialTranslation);
		objMagnet3.setLocalScale(initialScale);

		objMagnet4 = new GameObject(GameObject.root(), magnetS, magnet4Tx);
		initialTranslation = (new Matrix4f()).translation(2, 0, 2);
		initialScale = (new Matrix4f()).scaling(0.08f, 0.1f, 0.025f);
		objMagnet4.setLocalTranslation(initialTranslation);
		objMagnet4.setLocalScale(initialScale);

		//Torus
		objTorus = new GameObject(GameObject.root(), torusS, torusTx);
		initialTranslation = (new Matrix4f()).translation(getSpawnCoord(), 0.25f, getSpawnCoord());
		initialScale = (new Matrix4f()).scaling(1f);
		objTorus.setLocalTranslation(initialTranslation);
		objTorus.setLocalScale(initialScale);

		//Plane
		objPlane = new GameObject(GameObject.root(), planeS, planeTx);
		initialTranslation = (new Matrix4f()).translation(getSpawnCoord(), -10, getSpawnCoord());
		xAxis = objPlane.getLocalRightVector();
		yAxis = objPlane.getLocalUpVector();
		Matrix4f planeRotation; //This is how rotation is done
		rotateX = (new Matrix4f()).rotation((float)Math.toRadians(270), xAxis);
		float randTurn = (float)(Math.toRadians(rand.nextInt(360)));
		rotateY = (new Matrix4f()).rotation(randTurn, yAxis);
		planeRotation = rotateY.mul(rotateX.mul(objPlane.getLocalRotation())); 
		initialScale = (new Matrix4f()).scaling(3f);
		objPlane.setLocalTranslation(initialTranslation);
		objPlane.setLocalRotation(planeRotation);
		objPlane.setLocalScale(initialScale);

		//Sphere
		objSphere = new GameObject(GameObject.root(), sphereS, sphereTx);
		initialTranslation = (new Matrix4f()).translation(getSpawnCoord(), 3, getSpawnCoord());
		initialScale = (new Matrix4f()).scaling(3f);
		objSphere.setLocalTranslation(initialTranslation);
		objSphere.setLocalScale(initialScale);

		//Lava Object
		objLava = new GameObject(GameObject.root(), lavaS, lavaTx);
		initialTranslation = (new Matrix4f()).translation(getSpawnCoord(), 0.1f, getSpawnCoord());
		initialScale = (new Matrix4f()).scaling(3.5f);
		objLava.setLocalTranslation(initialTranslation);
		objLava.setLocalScale(initialScale);


		/* 
		//Diamond Object, which is the child object of the dolphin
		objDiamond = new GameObject(GameObject.root(), diamondS, diamondTx);
		initialScale = (new Matrix4f()).scaling(0.5f, 1f, 0.5f);
		initialScale = (new Matrix4f()).scaling(0.15f);
		objDiamond.setLocalScale(initialScale);
		objDiamond.setParent(dol);
		objDiamond.propagateTranslation(true);
		objDiamond.propagateRotation(false);*/
		
		//Terrain texture and height generation
		terrain = new GameObject(GameObject.root(), terrainS, gravelTx);
		initialTranslation = (new Matrix4f()).translation(0f,-10f,0f);
		terrain.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scale(1000f, 30.0f, 1000f);
		terrain.setLocalScale(initialScale);
		terrain.setHeightMap(hillsTx);
		(terrain.getRenderStates()).setTiling(2); //Mirrored Repeat
		(terrain.getRenderStates()).setTileFactor(100);
		
		terrain.getRenderStates().setTiling(2);
		terrain.getRenderStates().setTileFactor(100);
		
		terrain2 = new GameObject(GameObject.root(), terrain2S, water2Tx);
		initialTranslation = (new Matrix4f()).translation(0f, -0.5f,0f);
		terrain2.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scale(1000f, 1.0f, 1000f);
		terrain2.setLocalScale(initialScale);
		
		(terrain2.getRenderStates()).setTiling(2); //Mirrored Repeat
		(terrain2.getRenderStates()).setTileFactor(100);
		
		terrain2.getRenderStates().setTiling(2);
		terrain2.getRenderStates().setTileFactor(100);
		

		//NPC & PC Game Objects
		objSpider = new GameObject(GameObject.root(), spiderS, spiderTx);
		initialTranslation = (new Matrix4f()).translation(20, 0, 5);
		initialScale = (new Matrix4f()).scaling(2f);
		objSpider.setLocalTranslation(initialTranslation);
		objSpider.setLocalScale(initialScale);
		(terrain2.getRenderStates()).setTiling(2); //Mirrored Repeat
		(terrain2.getRenderStates()).setTileFactor(10);
		
		objPlayer = new GameObject(GameObject.root(), animPlayerS, playerBlankTx);
		initialTranslation = (new Matrix4f()).translation(0,4,5);
		initialScale = (new Matrix4f()).scaling(0.9f);
		objPlayer.setLocalTranslation(initialTranslation);
		objPlayer.setLocalScale(initialScale);

		//For hitbox debugging
		objPlayerPunchBox = new GameObject(GameObject.root(), playerPunchS, playerPunchTx);
		initialScale = (new Matrix4f()).scaling(0.5f);
		objPlayerPunchBox.setLocalScale(initialScale);
		objPlayerPunchBox.setParent(objPlayer);
		objPlayerPunchBox.propagateTranslation(true);
		objPlayerPunchBox.propagateRotation(true);
		objPlayerPunchBox.applyParentRotationToPosition(true);
		objPlayerPunchBox.getRenderStates().disableRendering();;
		
		
		//Goals and Soccerball for Physics Object
		/*
		objGoalOne = new GameObject(GameObject.root(), goalS, goalTx);
		initialTranslation = (new Matrix4f()).translation(0,2,-5);
		initialScale = (new Matrix4f()).scaling(1.0f);
		objPlayer.setLocalTranslation(initialTranslation);
		objPlayer.setLocalScale(initialScale);
		
		objGoalTwo = new GameObject(GameObject.root(), goalS, goalTx);
		initialTranslation = (new Matrix4f()).translation(-4,2,-5);
		initialScale = (new Matrix4f()).scaling(1.0f);
		objPlayer.setLocalTranslation(initialTranslation);
		objPlayer.setLocalScale(initialScale);
		
		objBall = new GameObject(GameObject.root(), ballS, ballTx);
		initialTranslation = (new Matrix4f()).translation(0,2,5);
		initialScale = (new Matrix4f()).scaling(1.0f);
		objPlayer.setLocalTranslation(initialTranslation);
		objPlayer.setLocalScale(initialScale);
		*/
	}
	
	@Override
	/**Initialize sounds */
	public void loadSounds(){
		AudioResource whistle_resource, run_resource, punch_resource, spider_resource, background_resource;
		audioManager = engine.getAudioManager();
		
		whistle_resource = audioManager.createAudioResource("assets/sounds/whistle.wav", AudioResourceType.AUDIO_SAMPLE); //I made
		run_resource = audioManager.createAudioResource("assets/sounds/run.wav", AudioResourceType.AUDIO_SAMPLE); //https://opengameart.org/content/fantozzis-footsteps-grasssand-stone 
		punch_resource = audioManager.createAudioResource("assets/sounds/punch.wav", AudioResourceType.AUDIO_SAMPLE); https://opengameart.org/content/hit-sound-effects 
		spider_resource = audioManager.createAudioResource("assets/sounds/spider5.wav", AudioResourceType.AUDIO_SAMPLE); //I made
		background_resource = audioManager.createAudioResource("assets/sounds/Incorporeality.wav", AudioResourceType.AUDIO_SAMPLE); //Free

		whistleSound = new Sound(whistle_resource, SoundType.SOUND_EFFECT, 100, false);
		runSound = new Sound(run_resource, SoundType.SOUND_EFFECT, 30, true);
		punchSound = new Sound(punch_resource, SoundType.SOUND_EFFECT, 100, false);
		spiderSound = new Sound(spider_resource, SoundType.SOUND_EFFECT, 250, true);
		backgroundMusic = new Sound(background_resource, SoundType.SOUND_MUSIC, 2, true);
		whistleSound.initialize(audioManager);
		runSound.initialize(audioManager);
		punchSound.initialize(audioManager);
		spiderSound.initialize(audioManager);
		backgroundMusic.initialize(audioManager);
		
		whistleSound.setMaxDistance(15.0f);
		whistleSound.setMinDistance(0.1f);
		whistleSound.setRollOff(5.0f);
		runSound.setMaxDistance(5.0f);
		runSound.setMinDistance(0.1f);
		runSound.setRollOff(4.0f);
		punchSound.setMaxDistance(5.0f);
		punchSound.setMinDistance(0.1f);
		punchSound.setRollOff(4.0f);
		
		spiderSound.setMaxDistance(10.0f);
		spiderSound.setMinDistance(2f);
		spiderSound.setRollOff(1.0f);
	}
	
	/**Gets the single value coordinate to return to the translation function. This increases an offset padding so shapes don't spawn in the middle of the screen */
	public float getSpawnCoord() {
		float x = rand.nextFloat(14);
		x += 5;
		int n = rand.nextInt(5);
		if(n%2 == 0){
			x *= -1;
		}
		return x;
	}
	
	/**Passes location and orientation of the camera and avatar to the audio manager to create 3D sound */
	public void setEarParameters(){
		Camera camera = (engine.getRenderSystem()).getViewport("PRIMARY").getCamera();
		audioManager.getEar().setLocation(objPlayer.getWorldLocation());
		audioManager.getEar().setOrientation(camera.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	@Override
	/**Initialize all lights in game */
	public void initializeLights()
	{	Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);
		light1 = new Light();
		light1.setType(Light.LightType.POSITIONAL);
		light1.setLocation(new Vector3f(5.0f, 1.0f, 2.0f));
		
		light2 = new Light();
		light2.setType(Light.LightType.SPOTLIGHT);
		light2.setLocation(objPlayer.getWorldLocation());
		light2.setDirection(new Vector3f(0.0f, 0.0f, 0.4f));
		
		(engine.getSceneGraph()).addLight(light1);
		(engine.getSceneGraph()).addLight(light2);
	}
	
	@Override
	/**Initialize skybox */
	public void loadSkyBoxes(){
		fluffyClouds = (engine.getSceneGraph()).loadCubeMap("fluffyClouds");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

	@Override
	/**Initialize game settings */
	public void initializeGame()
	{	lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		// ------------- Input Section -------------------
		im = engine.getInputManager();
		lastLocation = objPlayer.getWorldLocation(); //Terrain following check


		// ------------- positioning the camera -------------
		camera = (engine.getRenderSystem().getViewport("PRIMARY").getCamera());
		secondaryCam = (engine.getRenderSystem().getViewport("SECONDARY").getCamera());
		// ------------- more camera stuff -------------
		String gpName = im.getFirstGamepadName();
		
		orbitController = new CameraOrbit3D(camera, objPlayer, gpName, engine);
		sCamController = new CameraController(secondaryCam, engine);


		FwdAction fwdAc = new FwdAction(this); //add protclient
		YawAction turnAc = new YawAction(this);
		PitchAction pitchAc = new PitchAction(this);

		ground.getRenderStates().setTiling(1);
		ground.getRenderStates().setTileFactor(7);

		//----------------InvisibilityController- Node Controllers ------------------
		ic = new InvisibilityController(engine, 3f);
		rc1 = new RotationController(engine, new Vector3f(0, 1, 0), 0.01f);
		rc2 = new RotationController(engine, new Vector3f(0, 1, 0), 0.01f);
		rc3 = new RotationController(engine, new Vector3f(0, 1, 0), 0.01f);
		rc4 = new RotationController(engine, new Vector3f(0, 1, 0), 0.01f);
		ic.addTarget(objChest);
		rc1.addTarget(objCube);
		rc2.addTarget(objPlane);
		rc3.addTarget(objSphere);
		rc4.addTarget(objTorus);
		(engine.getSceneGraph()).addNodeController(ic);
		(engine.getSceneGraph()).addNodeController(rc1);
		(engine.getSceneGraph()).addNodeController(rc2);
		(engine.getSceneGraph()).addNodeController(rc3);
		(engine.getSceneGraph()).addNodeController(rc4);
		
		if(choosingModel){
			setupNetworking();
		}
		
		
		// ---------------- Sound Section --------------------
		spiderSound.setLocation(objSpider.getWorldLocation());
		//Set run and punch sounds to player avatar
		setEarParameters();
		spiderSound.play();
		backgroundMusic.play();
		
		//------------------Physics Section-------------------------
		float[] gravity = {0f, -5f, 0f};
		physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
		physicsEngine.setGravity(gravity);

		float mass = 5.0f;
		float up[ ] = {0,1,0};
		float radius = 0.75f;
		float height = 2.75f;
		double[ ] tempTransform;
		
		//Ball
		/*
		translation = new Matrix4f(objBall.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		ballPhys = (engine.getSceneGraph()).addPhysicsCapsuleX(
		mass, tempTransform, radius, height);
		ballPhys.setBounciness(0.8f);
		objBall.setPhysicsObject(ballPhys);
		
		//Goal One
		translation = new Matrix4f(objGoalOne.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		goalOnePhys = (engine.getSceneGraph()).addPhysicsCapsuleX(
		mass, tempTransform, radius, height);
		goalOnePhys.setBounciness(0.8f);
		objGoalOne.setPhysicsObject(goalOnePhys);
		
		//Goal Two
		translation = new Matrix4f(objGoalTwo.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		goalTwoPhys = (engine.getSceneGraph()).addPhysicsCapsuleX(
		mass, tempTransform, radius, height);
		goalTwoPhys.setBounciness(0.8f);
		objGoalTwo.setPhysicsObject(goalTwoPhys);
		*/
		
		/*Player
		//Matrix4f translation = new Matrix4f(objPlayer.getLocalTranslation().rotate((float)Math.toRadians(90), 0, 0, 1));
		Matrix4f translation = new Matrix4f(objPlayer.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));

		playerCapsulePhys = (engine.getSceneGraph()).addPhysicsCylinder(mass, tempTransform, radius, height);
		playerCapsulePhys.setBounciness(0.45f);
		objPlayer.setPhysicsObject(playerCapsulePhys);
		
		
		//Plane
		translation = new Matrix4f(terrain2.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		planePhys = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, up, 0.0f);
		planePhys.setBounciness(1.0f);
		terrain2.setPhysicsObject(planePhys);*/

		Matrix4f translation = new Matrix4f(objPlayer.getLocalTranslation().rotate((float)Math.toRadians(90), 0, 0, 1));

		//Player Punch Box
		float punchVals[] = {1f, 1f, 1f};
		translation = new Matrix4f(objPlayerPunchBox.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		punchPhys = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, punchVals);
		punchPhys.setBounciness(1f);
		punchPhys.setFriction(1f);
		objPlayerPunchBox.setPhysicsObject(punchPhys);

		/*Plane
		translation = new Matrix4f(terrain.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		planePhys = (engine.getSceneGraph()).addPhysicsStaticPlane(tempTransform, up, 0.0f);
		//planePhys.setBounciness(0f);
		terrain.setPhysicsObject(planePhys);
*/
		

		//Spider
		float spiderVals[] = {10f, 10f, 18f};
		translation = new Matrix4f(objSpider.getLocalTranslation()); 
		tempTransform = toDoubleArray(translation.get(vals));
		spiderPhys = (engine.getSceneGraph()).addPhysicsBox(mass, tempTransform, spiderVals);
		spiderPhys.setBounciness(1f);
		objSpider.setPhysicsObject(spiderPhys);
		
		engine.enableGraphicsWorldRender();
		//engine.enablePhysicsWorldRender();
	}



	@Override
	public void createViewports(){
		(engine.getRenderSystem()).addViewport("PRIMARY", 0, 0, 1f, 1f);
		(engine.getRenderSystem()).addViewport("SECONDARY", 0, 0.75f, 0.25f, 0.25f);
		mainVP = (engine.getRenderSystem()).getViewport("PRIMARY");
		secondVP = (engine.getRenderSystem()).getViewport("SECONDARY");
		secondVP.setHasBorder(true);
		secondVP.setBorderWidth(4);
		secondVP.setBorderColor(0.5f, 0.25f, 0.5f);
		mainVLeft = (int)mainVP.getActualLeft();
		mainVRight = (int)mainVP.getActualWidth();
		mainVBot = (int)mainVP.getActualBottom();
		mainVTop = (int)mainVP.getActualHeight();
		secVLeft = (int)secondVP.getActualLeft();
		secVRight = (int)secondVP.getActualWidth();
		secVBot = (int)secondVP.getActualBottom();
		secVTop = (int)secondVP.getActualHeight();
	}

	@Override
	/**Continuously run game loop */
	public void update()
	{	
		lastFrameTime = currFrameTime;
		currFrameTime = System.currentTimeMillis();
 
		
		timePassed = (currFrameTime - lastFrameTime) / 1000.0;
		
		elapsTime += timePassed;

		//System.out.println(elapsTime);
		//System.out.println(timePassed);

		Vector3f loc, newLocation, fwd, up, right;
		Matrix4f currRoto, newRoto, xRot, yRot, zRot, New4fLoco;

		orbitController.updateCameraPosition();
	
		im.update((float)elapsTime);
		
		//Terrain Following
		loc = objPlayer.getWorldLocation();
		float height = terrain.getHeight(loc.x(), loc.z());
		objPlayer.setLocalLocation(new Vector3f(loc.x(), height -4.75f, loc.z()));
		

		/*Dolphins Diamond
		
		//objDiamond.setLocalLocation(loc);
		New4fLoco = objDiamond.getLocalTranslation();
		New4fLoco.translation(0, 1.75f, 0);
		//New4fLoco.mul(1.75f);
		//newLocation = loc.add(up.x(), up.y(), up.z());
		objDiamond.setLocalTranslation(New4fLoco);
		objDiamond.setLocalRotation((new Matrix4f()).rotation((float)elapsTime, 0, 1, 0));*/
		
		//Collision logic
		checkCollisionObjects();
		
		//Update any animations
		animPlayerS.updateAnimation();
		animGhostS.updateAnimation();
		
		//Update Light to Follow
		light2.setLocation(objPlayer.getWorldLocation());
		Vector3f tempDir = objPlayer.getWorldForwardVector();
		light2.setDirection(tempDir);
		
		//Sound
		spiderSound.setLocation(objSpider.getWorldLocation());
		//Calculate distance for rolloff
		float diff = calculateDistance(objSpider, objPlayer);
		if(diff > 60){ diff = 10000000;}
		spiderSound.setRollOff(diff * 0.09f);
		setEarParameters();
		
		//Update Physics
		if(runningPhysics){
			AxisAngle4f aa = new AxisAngle4f();
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			Matrix4f mat3 = new Matrix4f().identity();
			checkForCollisions();
			physicsEngine.update((float)elapsTime);
			
			for (GameObject go:engine.getSceneGraph().getGameObjects()){ 
				if (go.getPhysicsObject() != null){
					mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
					mat2.set(3,0,mat.m30());
					mat2.set(3,1,mat.m31());
					mat2.set(3,2,mat.m32());
					go.setLocalTranslation(mat2);
					mat.getRotation(aa);
					mat3.rotation(aa);
					go.setLocalRotation(mat3);
				} 
			} 
		}
		
		//Terrain Following
		/*
		loc = objPlayer.getWorldLocation();
		float height = terrain.getHeight(loc.x(), loc.z());
		float offset = 8f;
		System.out.println("height: "+height);
		if(runningPhysics){
			if(height > 8.5f){
				objPlayer.setLocalLocation(new Vector3f(loc.x(), height - offset, loc.z()));
			}
			offset = 8f;
		} else {
			
		} */
		loc = objSpider.getWorldLocation();
		height = terrain.getHeight(loc.x(), loc.z());
		//objSpider.lookAt(objPlayer);

		objSpider.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));

		//Resetting phys locations
		double[] transform = toDoubleArray(objSpider.getWorldTranslation().mul(objSpider.getWorldRotation()).get(vals));
		objSpider.getPhysicsObject().setTransform(transform);

		if(punching){
			
			if(!countingHitBox){
				countingHitBox = true;
				setHoldHB(System.currentTimeMillis());
			}
			long test = System.currentTimeMillis();
			//Number of seconds
			//int numSec = 1;
			if(test >= (getHoldHB() + 0.5 * 1000)){
				System.out.println("From 1s Passed" + System.currentTimeMillis());
				New4fLoco = objPlayerPunchBox.getLocalTranslation();
				New4fLoco.translation(-1f, 1f, 1f);

				objPlayerPunchBox.setLocalTranslation(New4fLoco); 

				transform = toDoubleArray(objPlayerPunchBox.getWorldTranslation().mul(objPlayerPunchBox.getWorldRotation()).get(vals));
				objPlayerPunchBox.getPhysicsObject().setTransform(transform);
	
				countingHitBox = false;
				punching = false;
			}

		}else{
			//Hitbox movement
			New4fLoco = objPlayerPunchBox.getLocalTranslation();
			New4fLoco.translation(-1f, -10000f, 0f);

			objPlayerPunchBox.setLocalTranslation(New4fLoco); 
			//newRoto = objPlayer.getLocalRotation();
			//objPlayerPunchBox.setLocalRotation(newRoto);
			transform = toDoubleArray(objPlayerPunchBox.getWorldTranslation().mul(objPlayerPunchBox.getWorldRotation()).get(vals));
			objPlayerPunchBox.getPhysicsObject().setTransform(transform);
		}

		loc = objPlayer.getWorldLocation();

		//Magnet placing
		if(visitedObj1){
			if(visitLock1==false){
				visitLock1 = true;
				rc1.toggle();
			}
			loc = dol.getWorldLocation(); //Aligns the vectors with dolphin
			fwd = dol.getLocalForwardVector();
			up = dol.getLocalUpVector();
			right = dol.getLocalRightVector();
			up.mul(1f); //Moves the vectors
			right.mul(-0.1f);
			newLocation = loc.add(right.x(), right.y(), right.z()).add(up.x(), up.y(), up.z());//Applies changes
			objMagnet1.setLocalLocation(newLocation); //Sets changes
			yRot = (new Matrix4f()).rotation((float)Math.toRadians(90), up); //Same run through for rotations
			currRoto = dol.getLocalRotation();
			newRoto = yRot.mul(currRoto);
			objMagnet1.setLocalRotation(newRoto);
		}
		
		if(visitedObj2){
			if(visitLock2==false){
				visitLock2=true;
				rc2.toggle();
			}
			loc = dol.getWorldLocation();
			fwd = dol.getLocalForwardVector();
			up = dol.getLocalUpVector();
			right = dol.getLocalRightVector();
			up.mul(1f);
			right.mul(0.1f);
			newLocation = loc.add(right.x(), right.y(), right.z()).add(up.x(), up.y(), up.z());
			objMagnet2.setLocalLocation(newLocation);
			yRot = (new Matrix4f()).rotation((float)Math.toRadians(90), up);
			currRoto = dol.getLocalRotation();
			newRoto = yRot.mul(currRoto);
			objMagnet2.setLocalRotation(newRoto);
		}
		
		if(visitedObj3){
			if(visitLock3==false){
				visitLock3 = true;
				rc3.toggle();
			}
			loc = dol.getWorldLocation();
			fwd = dol.getLocalForwardVector();
			up = dol.getLocalUpVector();
			right = dol.getLocalRightVector();
			up.mul(0.5f);
			right.mul(-0.2f);
			fwd.mul(-0.3f);
			newLocation = loc.add(right.x(), right.y(), right.z()).add(up.x(), up.y(), up.z()).add(fwd.x(), fwd.y(), fwd.z());
			objMagnet3.setLocalLocation(newLocation);
			yRot = (new Matrix4f()).rotation((float)Math.toRadians(0), up);
			currRoto = dol.getLocalRotation();
			newRoto = yRot.mul(currRoto);
			objMagnet3.setLocalRotation(newRoto);
		}

		if(visitedObj4){
			if(visitLock4 == false){
				visitLock4 = true;
				rc4.toggle();
			}
			loc = dol.getWorldLocation();
			fwd = dol.getLocalForwardVector();
			up = dol.getLocalUpVector();
			right = dol.getLocalRightVector();
			up.mul(0.5f);
			right.mul(0.2f);
			fwd.mul(-0.3f);
			newLocation = loc.add(right.x(), right.y(), right.z()).add(up.x(), up.y(), up.z()).add(fwd.x(), fwd.y(), fwd.z());
			objMagnet4.setLocalLocation(newLocation);
			yRot = (new Matrix4f()).rotation((float)Math.toRadians(0), up);
			currRoto = dol.getLocalRotation();
			newRoto = yRot.mul(currRoto);
			objMagnet4.setLocalRotation(newRoto);
		}

		if(getDistanceGameObjs(dol, objLava) < 2){
			touchingLava = true;
		}


		checkGameState();
		processNetworking((float)elapsTime);
		

		// build and set HUD
		int elapsTimeSec = Math.round((float)elapsTime);
		
		
	
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String dispScore = Integer.toString(score);
		String counterStr = Integer.toString(counter);
		String dispStr1 = "Time = " + elapsTimeStr;//"Score = " + score;
		String dispStr2 = ""; 
		String dispStr3 = "" + getAvatar().getWorldLocation();

		dispStr2 = "Health:  " ;
		int healthCheck = health;
		while(healthCheck > 0){
			dispStr2 += "|";
			healthCheck -= 5;
		}
	
		Vector3f hud1Color = new Vector3f(1,1,1);
		Vector3f hud2Color = new Vector3f(0,1,0);
		
		if(health > 99){
			hud2Color = new Vector3f(0,1,0);
		} else if(health > 80){
			hud2Color = new Vector3f(0.25f,0.75f,0);
		} else if(health > 50){
			hud2Color = new Vector3f(0.5f,0.5f,0);
		} else if(health > 20){
			hud2Color = new Vector3f(0.75f,0.25f,0);
		} else {
			hud2Color = new Vector3f(1,0,0);
		}
		
		Vector3f hud3Color = new Vector3f(1,1,1);

		//Vector3f hud4Color = new Vector3f(1,1,1);
		//Vector3f hud5Color = new Vector3f(1,1,1);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, mainVLeft + mainVRight/8, 20);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, mainVLeft + 2*(mainVRight/8), 20);
		(engine.getHUDmanager()).setHUD3(dispStr3, hud3Color, secVLeft + secVRight/7, mainVTop-secVBot);
		//(engine.getHUDmanager()).setHUD4(dispStr4, hud4Color, 500, 50);
		//(engine.getHUDmanager()).setHUD5(dispStr5, hud5Color, 15, 50);
		
		
		
	}

	/**Gets distance between dolphin and camera */
	public float getDistDolCam(){
		Camera cam = (engine.getRenderSystem().getViewport("MAIN").getCamera()); //Sets up camera object
		return (objPlayer.getWorldLocation()).distance(cam.getLocation()); 		
	}

	
	/**Checks to see if that game is still playing or if the player won  */
	public void checkGameState(){
		if( visitedObj1==true && visitedObj2==true && visitedObj3==true && visitedObj4==true){
			gameOver = true;
			if(allVisited==false){
				allVisited=true;
				ic.toggle();
			}

		}
		if( touchingLava ){
			gameOver = true;
			paused = true;
		}
	}

	/**Aligns location and x, y, z vectors with a certain GameObject's */
	public void resetVectors(Vector3f a, Vector3f b, Vector3f c, Vector3f d, GameObject e){
		a = e.getWorldLocation();
		b = e.getLocalRightVector();
		c = e.getLocalUpVector();
		d = e.getLocalForwardVector();
	}

	/**Gets distance between a specified object location and the camera*/
	public float getDistObjCam(Vector3f x){
		Camera cam = (engine.getRenderSystem().getViewport("MAIN").getCamera()); //Sets up camera object
		return x.distance(cam.getLocation());
	}

	/**Gets distance between two game objects */
	public float getDistanceGameObjs(GameObject a, GameObject b){
		Vector3f aVec = a.getWorldLocation();
		Vector3f bVec = b.getWorldLocation();
		return aVec.distance(bVec);
	}

	/**Returns game avatar */
	public GameObject getAvatar(){
		return objPlayer;
	}

	/**Returns engines elapsed time */
	public float getElapsedTime(){
		return (float)elapsTime;
	}

	/**Runs a constant "collision check" to see if the player has visited these sites, and keeps track */
	public void checkCollisionObjects() {
		Vector3f dolVec = dol.getWorldLocation();
		Vector3f loc, newLocation;
		Vector3f right = dol.getLocalRightVector();
		Vector3f up = dol.getLocalUpVector();
		Vector3f fwd = dol.getLocalForwardVector();
		Vector3f cubeVec = objCube.getWorldLocation(); //obj 1, <3 is a collision
		Vector3f planeVec = objPlane.getWorldLocation(); //obj 2, <5 is a collision>
		Vector3f sphereVec = objSphere.getWorldLocation(); //obj 3, <5.25 is a collision
		Vector3f torusVec = objTorus.getWorldLocation(); //obj 4, if same, 2.5 is a collision
		
		//Finish rest of code here on home machine
		if(getDistanceGameObjs(dol, objCube) < 3 && visitedObj1==false){
			visitedObj1 = true;
			score++;
		}
		if(getDistanceGameObjs(dol, objPlane) < 5 && visitedObj2==false){
			visitedObj2 = true;
			score++;
		}
		if(getDistanceGameObjs(dol, objSphere) < 5.25 && visitedObj3==false){
			visitedObj3 = true;
			score++;
		}
		if(getDistanceGameObjs(dol, objTorus) < 2.5 && visitedObj4==false){
			visitedObj4 = true;
			score++;
		}
	}

	@Override
	/**Input checking with keypressed method */
	public void keyPressed(KeyEvent e)
	{
		Vector3f loc, fwd, up, right, newLocation, testLoc; //Creates 3d vectors for locations and directions
		Matrix4f newRoto, currRoto, rot, xRot, yRot, zRot;
		Camera cam; //Initialize camera object
		float amtVal;
		

		
			switch (e.getKeyCode())
		{	
			case KeyEvent.VK_X:
				break;

			case KeyEvent.VK_1:
				health -= 10;
				//Now toggles world axis lines
				if(axisLines==true){
					objXLine.getRenderStates().disableRendering();
					objYLine.getRenderStates().disableRendering();
					objZLine.getRenderStates().disableRendering();
					axisLines=false;
				}else{
					objXLine.getRenderStates().enableRendering();
					objYLine.getRenderStates().enableRendering();
					objZLine.getRenderStates().enableRendering();
					axisLines=true;
				}
				break;

			case KeyEvent.VK_W:
			case KeyEvent.VK_2: //Move dolphin forward
				//dol.getRenderStates().setWireframe(true);
		
				fwd = objPlayer.getWorldForwardVector(); //Sets fwd to the world forward vector
				loc = objPlayer.getWorldLocation(); //Sets loc to current dolphin location
				newLocation = loc.add(fwd.mul((float)(timePassed * 136f))); //Sets new location to along the forward vector * .02 ahead //Was 12f
				testLoc = (new Vector3f(newLocation.x(), 0, newLocation.z()));
				//System.out.println(newLocation.y());
				//objPlayer.getPhysicsObject().setTransform(toDoubleArray(newLocation));
				
				objPlayer.setLocalLocation(newLocation);	//Actually sets dolphin location to new location
						protClient.sendMoveMessage(objPlayer.getWorldLocation());
				
				//animPlayerS.stopAnimation();
				if(!isRunning){
					animPlayerS.playAnimation("RUN", 0.45f, AnimatedShape.EndType.LOOP, 0);
					protClient.sendAnimationMessage("run");
					isRunning = true;
					runSound.play();
				}
				
				//double[] transform = toDoubleArray(objPlayer.getWorldTranslation().mul(objPlayer.getWorldRotation()).get(vals));
					//objPlayer.getPhysicsObject().setTransform(transform);
				
				
				break;

			case KeyEvent.VK_S:
			case KeyEvent.VK_3: //Move dolphin backward
				//dol.getRenderStates().setWireframe(false);
				
				fwd = objPlayer.getWorldForwardVector(); //Similar to backward moving
				loc = objPlayer.getWorldLocation();
				newLocation = loc.add(fwd.mul(-(float)(timePassed * 12f)));
				//objPlayer.getPhysicsObject().setTransform(toDoubleArray(newLocation));
				
				objPlayer.setLocalLocation(newLocation);	//Actually sets dolphin location to new location
				protClient.sendMoveMessage(objPlayer.getWorldLocation());
				
				//transform = toDoubleArray(objPlayer.getWorldTranslation().mul(objPlayer.getWorldRotation()).get(vals));
				//objPlayer.getPhysicsObject().setTransform(transform);

				break;
				
			// Yaw command
			case KeyEvent.VK_A:
				amtVal = (float)timePassed * 150f;
				if(!touchingLava){
					objPlayer.yaw(amtVal);	//was 50	
				}
				protClient.sendYawMessage(amtVal);

				//transform = toDoubleArray(objPlayer.getWorldTranslation().mul(objPlayer.getWorldRotation()).get(vals));
				//objPlayer.getPhysicsObject().setTransform(transform);
				break;
			case KeyEvent.VK_D:
				amtVal = -(float)timePassed * 150f;
				if(!touchingLava){
					objPlayer.yaw(amtVal);	//was 50
				}
				protClient.sendYawMessage(amtVal);

				//transform = toDoubleArray(objPlayer.getWorldTranslation().mul(objPlayer.getWorldRotation()).get(vals));
				//objPlayer.getPhysicsObject().setTransform(transform);
				break;
			// Pitch command
			case KeyEvent.VK_UP:
				if(!touchingLava)
					objPlayer.pitch((float)(timePassed * 50f));
				
				break;
				
			case KeyEvent.VK_DOWN:
				if(!touchingLava)				
					objPlayer.pitch(-(float)(timePassed * 50f));
				
				break;

			case KeyEvent.VK_SPACE:
				/*if(onDolphin){
					onDolphin = false;
					cam = (engine.getEngine().getRenderSystem()).getViewport("MAIN").getCamera();
					loc = dol.getWorldLocation();
					fwd = dol.getLocalForwardVector();
					up = dol.getLocalUpVector();
					right = dol.getLocalRightVector();
					right.mul(-1f);
					newLocation = loc.add(right.x(), right.y(), right.z());
					cam.setLocation(loc);
				}else{
					onDolphin = true;
				}*/
				break;


			case KeyEvent.VK_4: //Keys 4-6 open for commands
				animPlayerS.stopAnimation();
				animPlayerS.playAnimation("RUN", 0.45f, AnimatedShape.EndType.LOOP, 0);
				protClient.sendAnimationMessage("run");
				break;
			
			case KeyEvent.VK_5:
				animPlayerS.stopAnimation();
				animPlayerS.playAnimation("PUNCH", 0.3f, AnimatedShape.EndType.STOP, 0);
				punching = true;
				protClient.sendAnimationMessage("punch");
				punchSound.play();
				break;
				
			case KeyEvent.VK_6:
				animPlayerS.stopAnimation();
				protClient.sendAnimationMessage("stop");
				break;
			case KeyEvent.VK_7:
				runningPhysics = !runningPhysics;
				
				if(runningPhysics){
					protClient.sendPhysicsToggleMessage("true");
				} else {
					protClient.sendPhysicsToggleMessage("false");
				}
				
				
				System.out.print("Toggle Physics: ");
				if(runningPhysics){
					//objPlayer.setLocalRotation(objPlayer.getLocalRotation().rotate((float)Math.toRadians(90), 0, 0, 1));
					System.out.println("ON");
				} else {
					Matrix4f identityRotation = new Matrix4f().identity();
					
					//Keep player rotation but make it upright
					Vector3f eulerAngles = new Vector3f();
					(objPlayer.getWorldRotation()).getEulerAnglesXYZ(eulerAngles);
					float y = eulerAngles.y;
					
					//Reset Player Rotation
					identityRotation.rotation(y, 0.0f, 1.0f, 0.0f);
					objPlayer.setLocalRotation(identityRotation);
					//Reset Physics Object
					//transform = toDoubleArray(objPlayer.getWorldTranslation().mul(objPlayer.getWorldRotation()).get(vals));
					//objPlayer.getPhysicsObject().setTransform(transform);
					
					protClient.sendYawMessage((float)Math.toDegrees(y));
					
					//Reset Ghosts
					//gm.resetFromPhysics();
					
					System.out.println("OFF");
				}
				break;
			case KeyEvent.VK_8:
				protClient.sendAnimationMessage("run");
				break;
				
			case KeyEvent.VK_9:
				protClient.sendAnimationMessage("punch");
				break;
				
			case KeyEvent.VK_0:
				protClient.sendAnimationMessage("stop");
				break;
			
			case KeyEvent.VK_R:
				if(choosingModel){
					float playerX = objPlayer.getWorldLocation().x();
					float playerY = objPlayer.getWorldLocation().y();
					float playerZ = objPlayer.getWorldLocation().z();
					Matrix4f rotation = objPlayer.getWorldRotation();
					
					(engine.getSceneGraph()).removeGameObject(objPlayer);
					(engine.getSceneGraph()).removePhysicsObject(playerCapsulePhys);
					
					objPlayer = new GameObject(GameObject.root(), animPlayerS, playerRedTx);
					Matrix4f initialTranslation = (new Matrix4f()).translation(playerX, playerY, playerZ);
					Matrix4f initialScale = (new Matrix4f()).scaling(0.9f);
					Matrix4f initialRotation = rotation;
					objPlayer.setLocalTranslation(initialTranslation);
					objPlayer.setLocalScale(initialScale);
					objPlayer.setLocalRotation(rotation);
					
					choosingModel = false;
					protClient.sendModelMessage("red");
					initializeGame();
					break;
				}
			
				fwd = objSpider.getWorldForwardVector(); //Sets fwd to the world forward vector
				loc = objSpider.getWorldLocation(); //Sets loc to current dolphin location
				newLocation = loc.add(fwd.mul((float)(timePassed * 36f)));
				objSpider.setLocalLocation(newLocation);
				break;
			case KeyEvent.VK_E:
				fwd = objSpider.getWorldForwardVector(); //Sets fwd to the world forward vector
				loc = objSpider.getWorldLocation(); //Sets loc to current dolphin location
				newLocation = loc.add(fwd.mul(-(float)(timePassed * 36f)));
				objSpider.setLocalLocation(newLocation);
				break;
			case KeyEvent.VK_F:
				lightToggle = !lightToggle;
				if(lightToggle){
					protClient.toggleFlashlight("on");
					light2.setAmbient(0f, 0f, 0f);
					light2.setDiffuse(0f, 0f, 0f);
					light2.setSpecular(0f, 0f, 0f);
				} else {
					light2.setAmbient(0.3f, 0.3f, 0.3f);
					light2.setDiffuse(0.8f, 0.8f, 0.8f);
					light2.setSpecular(1.0f, 1.0f, 1.0f);
					protClient.toggleFlashlight("off");
				}
				break;
			case KeyEvent.VK_B:
				if(choosingModel){
					float playerX = objPlayer.getWorldLocation().x();
					float playerY = objPlayer.getWorldLocation().y();
					float playerZ = objPlayer.getWorldLocation().z();
					Matrix4f rotation = objPlayer.getWorldRotation();
					
					(engine.getSceneGraph()).removeGameObject(objPlayer);
					(engine.getSceneGraph()).removePhysicsObject(playerCapsulePhys);
					
					objPlayer = new GameObject(GameObject.root(), animPlayerS, playerBlueTx);
					Matrix4f initialTranslation = (new Matrix4f()).translation(playerX, playerY, playerZ);
					Matrix4f initialScale = (new Matrix4f()).scaling(0.9f);
					Matrix4f initialRotation = rotation;
					objPlayer.setLocalTranslation(initialTranslation);
					objPlayer.setLocalScale(initialScale);
					objPlayer.setLocalRotation(rotation);
					
					choosingModel = false;
					protClient.sendModelMessage("blue");
					initializeGame();
					break;
				}
				break;
			case KeyEvent.VK_T:
				if(choosingModel){
					float playerX = objPlayer.getWorldLocation().x();
					float playerY = objPlayer.getWorldLocation().y();
					float playerZ = objPlayer.getWorldLocation().z();
					Matrix4f rotation = objPlayer.getWorldRotation();
					
					(engine.getSceneGraph()).removeGameObject(objPlayer);
					(engine.getSceneGraph()).removePhysicsObject(playerCapsulePhys);
					
					objPlayer = new GameObject(GameObject.root(), animPlayerS, playerTanTx);
					Matrix4f initialTranslation = (new Matrix4f()).translation(playerX, playerY, playerZ);
					Matrix4f initialScale = (new Matrix4f()).scaling(0.9f);
					Matrix4f initialRotation = rotation;
					objPlayer.setLocalTranslation(initialTranslation);
					objPlayer.setLocalScale(initialScale);
					objPlayer.setLocalRotation(rotation);
					
					choosingModel = false;
					protClient.sendModelMessage("tan");
					initializeGame();
					break;
				}
				break;
			case KeyEvent.VK_G:
				if(choosingModel){
					float playerX = objPlayer.getWorldLocation().x();
					float playerY = objPlayer.getWorldLocation().y();
					float playerZ = objPlayer.getWorldLocation().z();
					Matrix4f rotation = objPlayer.getWorldRotation();
					
					(engine.getSceneGraph()).removeGameObject(objPlayer);
					(engine.getSceneGraph()).removePhysicsObject(playerCapsulePhys);
					
					objPlayer = new GameObject(GameObject.root(), animPlayerS, playerGreenTx);
					Matrix4f initialTranslation = (new Matrix4f()).translation(playerX, playerY, playerZ);
					Matrix4f initialScale = (new Matrix4f()).scaling(0.9f);
					Matrix4f initialRotation = rotation;
					objPlayer.setLocalTranslation(initialTranslation);
					objPlayer.setLocalScale(initialScale);
					objPlayer.setLocalRotation(rotation);
					
					choosingModel = false;
					protClient.sendModelMessage("green");
					initializeGame();
					break;
				}
				break;
			case KeyEvent.VK_C:
				if(choosingModel){
					float playerX = objPlayer.getWorldLocation().x();
					float playerY = objPlayer.getWorldLocation().y();
					float playerZ = objPlayer.getWorldLocation().z();
					Matrix4f rotation = objPlayer.getWorldRotation();
					
					(engine.getSceneGraph()).removeGameObject(objPlayer);
					(engine.getSceneGraph()).removePhysicsObject(playerCapsulePhys);
					
					objPlayer = new GameObject(GameObject.root(), animPlayerS, playerCyanTx);
					Matrix4f initialTranslation = (new Matrix4f()).translation(playerX, playerY, playerZ);
					Matrix4f initialScale = (new Matrix4f()).scaling(0.9f);
					Matrix4f initialRotation = rotation;
					objPlayer.setLocalTranslation(initialTranslation);
					objPlayer.setLocalScale(initialScale);
					objPlayer.setLocalRotation(rotation);
					
					choosingModel = false;
					protClient.sendModelMessage("cyan");
					initializeGame();
					break;
				}
				break;
		}

		super.keyPressed(e);
	}
	
	@Override
	/** Need Key Release to stop playing animations */
	public void keyReleased(KeyEvent e){
		switch (e.getKeyCode()){
			case KeyEvent.VK_W:
				animPlayerS.stopAnimation();
				protClient.sendAnimationMessage("stop");
				isRunning = false;
				runSound.stop();
				break;
		}
	}

	public AnimatedShape getGhostShape() { return animGhostS; }
	public TextureImage getGhostTexture() { return playerBlankTx; }
	public TextureImage getGhostTexture(int which_one){
		switch(which_one){
			case 1: return playerRedTx;
			case 2: return playerBlueTx;
			case 3: return playerGreenTx;
			case 4: return playerTanTx;
			case 5: return playerCyanTx;
		}
		return playerBlankTx;
	}
	public GhostManager getGhostManager() { return gm; }
	public Engine getEngine() { return engine; }
	public ObjShape getNPCShape(){return diamondS;}
	public TextureImage getNPCTexture(){return diamondTx;}
	
	//For locking time would need to be replicated for other timers
	public long getHoldHB(){return holdTimeHitBox;}
    public void setHoldHB(long l){holdTimeHitBox = l;}

	private void setupNetworking()
	{	isClientConnected = false;	
		try 
		{	protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} 	catch (UnknownHostException e) 
		{	e.printStackTrace();
		}	catch (IOException e) 
		{	e.printStackTrace();
		}
		if (protClient == null)
		{	System.out.println("missing protocol host");
		}
		else
		{	// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			System.out.println("val: " + serverAddress + serverPort + serverProtocol);
			protClient.sendJoinMessage();
			protClient.sendNeedNPC();
			//Handle ghost avatars
			System.out.println(gm.getGhosts());

		}
	}

	protected void processNetworking(float elapsTime)
	{	// Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	public Vector3f getPlayerPosition() { return objPlayer.getWorldLocation(); }

	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}

	//------------------Utility Functions for Physics --------------------------------//
	private float[] toFloatArray(double[] arr){ 
		if (arr == null) return null;
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++){ 
			ret[i] = (float)arr[i];
		}
		return ret;
	}
	
	//Was private, public so can be accessed by GhostManager
	public double[] toDoubleArray(float[] arr){ 
		if (arr == null) return null;
		int n = arr.length;
		double[] ret = new double[n];
		for (int i = 0; i < n; i++){ 
			ret[i] = (double)arr[i];
		}
		return ret;
	}
	
	private void checkForCollisions(){ 
		com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
		com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
		com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
		com.bulletphysics.dynamics.RigidBody object1, object2;
		com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;
	
		dynamicsWorld = ((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
		dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();
		for (int i=0; i<manifoldCount; i++){ 
			manifold = dispatcher.getManifoldByIndexInternal(i);
			object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
			object2 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
			JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
			JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
			for (int j = 0; j < manifold.getNumContacts(); j++){ 
				contactPoint = manifold.getContactPoint(j);
				if (contactPoint.getDistance() < 0.0f){ 
					System.out.println("---- hit between " + obj1 + " and " + obj2);
					break;
				} 
			} 
		} 
	}
	
	//Needed for Ghost Manager
	public float[] getVals(){
		return vals;
	}
	
	public void togglePhysics(){
		double[] transform = toDoubleArray(objPlayer.getWorldTranslation().mul(objPlayer.getWorldRotation()).get(vals));
		runningPhysics = !runningPhysics;
		
		if(runningPhysics){
					//objPlayer.setLocalRotation(objPlayer.getLocalRotation().rotate((float)Math.toRadians(90), 0, 0, 1));
					System.out.println("ON");
				} else {
					Matrix4f identityRotation = new Matrix4f().identity();
					
					//Keep player rotation but make it upright
					Vector3f eulerAngles = new Vector3f();
					(objPlayer.getWorldRotation()).getEulerAnglesXYZ(eulerAngles);
					float y = eulerAngles.y;
					
					//Reset Player Rotation
					identityRotation.rotation(y, 0.0f, 1.0f, 0.0f);
					objPlayer.setLocalRotation(identityRotation);
					//Reset Physics Object
					transform = toDoubleArray(objPlayer.getWorldTranslation().mul(objPlayer.getWorldRotation()).get(vals));
					objPlayer.getPhysicsObject().setTransform(transform);
					
					protClient.sendYawMessage((float)Math.toDegrees(y));
					
					//Reset Ghosts
					gm.resetFromPhysics();
					
					System.out.println("OFF");
				}
	}
	
	public void setPhysics(boolean toggle){
		double[] transform = toDoubleArray(objPlayer.getWorldTranslation().mul(objPlayer.getWorldRotation()).get(vals));
		runningPhysics = toggle;
		
		if(runningPhysics){
					//objPlayer.setLocalRotation(objPlayer.getLocalRotation().rotate((float)Math.toRadians(90), 0, 0, 1));
					System.out.println("ON");
				} else {
					Matrix4f identityRotation = new Matrix4f().identity();
					
					//Keep player rotation but make it upright
					Vector3f eulerAngles = new Vector3f();
					(objPlayer.getWorldRotation()).getEulerAnglesXYZ(eulerAngles);
					float y = eulerAngles.y;
					
					//Reset Player Rotation
					identityRotation.rotation(y, 0.0f, 1.0f, 0.0f);
					objPlayer.setLocalRotation(identityRotation);
					//Reset Physics Object
					transform = toDoubleArray(objPlayer.getWorldTranslation().mul(objPlayer.getWorldRotation()).get(vals));
					objPlayer.getPhysicsObject().setTransform(transform);
					
					protClient.sendYawMessage((float)Math.toDegrees(y));
					
					//Reset Ghosts
					gm.resetFromPhysics();
					
					System.out.println("OFF");
				}
	}
	
	private float calculateDistance(GameObject obj1, GameObject obj2){
		float x1 = (obj1.getWorldLocation()).x();
		float y1 = (obj1.getWorldLocation()).y();
		float z1 = (obj1.getWorldLocation()).z();
		float x2 = (obj2.getWorldLocation()).x();
		float y2 = (obj2.getWorldLocation()).y();
		float z2 = (obj2.getWorldLocation()).z();
		
		float dx = x2 - x1;
		float dy = y2 - y1;
		float dz = z2 - z1;
		
		float distance = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
		
		return distance;
	}
	
	private void modelResetFunc(){
		
		
		
	}
		

}
