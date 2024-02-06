package com.mygdx.runai;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.runai.characters.BaseCharacterClass;
import com.mygdx.runai.gamemodes.Mode;
import com.mygdx.runai.gamemodes.PacerMode;
import com.mygdx.runai.gamemodes.RankMode;

import java.util.ArrayList;
import java.util.List;

public class LibGDX extends Game implements RunAIInterface
{

	public SpriteBatch Batch;

	public Stage stage;

	public FrameBuffer fbo;
	public Texture fboText;

	public static AssetManager assetManager;
	private Texture enemyTexture;
	private Texture playerTexture;
	private List<Sprite> enemySprites = new ArrayList<>();

	private Sprite playerSprite;
	private boolean isRunningInBackground;

	public Vector2 playerPosition;
	public float playerDistanceMeters;
	long runStartTimeMillis;

	Preferences prefs;
	Leaderboard leaderboard;

	private SoundInterface soundInterface;
	private PlayerDataInterface playerDataInterface;

	public static int playerPlacement;

	public float targetDistance;
	public float voiceVolumeValue = 0;
	public float footstepVolumeValue = 0;
	public float feedbackVolumeValue = 0;
	public static float staticVoiceVolumeValue = 0;
	public static float staticFootstepVolumeValue = 0;
	public static float staticFeedbackVolumeValue = 0;

	private Mode mode;

	public int modeValue;
	public int paceMinute;
	public int paceSecond;
	private ArrayList<Float> aiDistanceTravelled = new ArrayList<>();
	private ArrayList<Integer> aiTime = new ArrayList<>();

	public LibGDX(SoundInterface soundInterface, PlayerDataInterface playerDataInterface, int modeValue){
		this.modeValue = modeValue;
		this.soundInterface = soundInterface;
		this.playerDataInterface = playerDataInterface;
		this.targetDistance = playerDataInterface.getPlayerDistanceToRun();
	}

	@Override
	public void create () {

		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		prefs = Gdx.app.getPreferences("com.runai.settings");
		//The queue is for the two threads to communicate

		Batch = new SpriteBatch();
		buildFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		assetManager = new AssetManager();

		assetManager.load("leftfoot.wav", Sound.class);



		//Loads the enemy texture
		assetManager.load("bad.png", Texture.class);
		assetManager.load("player.png", Texture.class);
		//Wait for the texture to finish loading
		assetManager.finishLoading();

		enemyTexture = assetManager.get("bad.png", Texture.class);
		playerTexture = assetManager.get("player.png", Texture.class);


		CreateAndRunAI();


	}

	private void buildFBO(int width, int height) {
		fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
	}

	@Override
	public void pause() {
		isRunningInBackground = true;
		super.pause();
	}

	public void resume(){
		isRunningInBackground = false;
		super.resume();
	}


	public void CreateAndRunAI(){

		staticVoiceVolumeValue = voiceVolumeValue;
		staticFeedbackVolumeValue = voiceVolumeValue;
		staticFootstepVolumeValue = voiceVolumeValue;



		//Creates a leaderboard and sort the AI by their distance ran
		leaderboard = new Leaderboard();
		leaderboard.sortAIByYAxis();

		if(modeValue == 0){

			mode = new RankMode(soundInterface, leaderboard, playerTexture, playerDataInterface);
		} else if (modeValue == 1) {

			mode = new PacerMode(soundInterface, playerTexture, paceMinute, paceSecond);
		}

		//Selects the mode of the Run
	}

	public void UpdatePacerPace(int paceMinutes,int paceSeconds){
		this.paceMinute = paceMinutes;
		this.paceSecond = paceSeconds;
		PacerMode pacerMode = (PacerMode)mode;
		pacerMode.UpdatePacerPace(paceMinutes,paceSeconds);
	}

	public void UpdateVolumes(float voiceVolumeValue,float feedbackVolumeValue,float footstepVolumeValue){
		this.voiceVolumeValue = voiceVolumeValue;
		this.feedbackVolumeValue = feedbackVolumeValue;
		this.footstepVolumeValue = footstepVolumeValue;
	}


	public ArrayList<Float> RunTheAI() {

		staticVoiceVolumeValue = voiceVolumeValue;
		staticFeedbackVolumeValue = feedbackVolumeValue;
		staticFootstepVolumeValue = footstepVolumeValue;

		leaderboard.sortAIByYAxis();
		playerPlacement = leaderboard.getPlayerPlacement();

		long currentTimeMillis = System.currentTimeMillis();
		long timeElapsedMillis = currentTimeMillis - runStartTimeMillis;
		float time = (float) timeElapsedMillis / 1000.0f; // Convert milliseconds to seconds

		mode.update(time, playerDistanceMeters);


		aiDistanceTravelled = new ArrayList<>();
			for (BaseCharacterClass aiCharacter: mode.runners) {
				aiDistanceTravelled.add(aiCharacter.runnerDistanceTravelled);
			}
		return aiDistanceTravelled;
	}
	public ArrayList<Float> AIDistances(){
		ArrayList<Float> aiDistances = new ArrayList<>();
		for (BaseCharacterClass aiCharacter: mode.runners) {
			aiDistances.add(aiCharacter.distanceTravelled);

		}
		return aiDistances;
	}

	@Override
	public ArrayList<Integer> GetTheTime() {
		 aiTime = new ArrayList<>();
			for (BaseCharacterClass aiCharacter : mode.runners) {
				aiTime.add(aiCharacter.TimeTaken);
			}
		return aiTime;
	}

	public float randomizeXValue(int index){
		//Sets the position based on the index of the list
		float minusValue = 50 * index;
		//Makes sure that the x value is not to far from the player
		float xValue = 700 - minusValue;
		return xValue;
	}






	@Override
	public void dispose () {
		if(Batch != null){
			Batch.dispose();
		}
		if(stage != null){

			stage.dispose();
		}

		if(fboText != null){

			fboText.dispose();
		}
		for (BaseCharacterClass aiCharacter : mode.runners){
			aiCharacter.dispose();
		}
		aiDistanceTravelled.clear();
		aiTime.clear();
		soundInterface.disposeSoundinterface();
	}

}
