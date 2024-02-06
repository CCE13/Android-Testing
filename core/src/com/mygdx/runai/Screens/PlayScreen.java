package com.mygdx.runai.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.runai.LibGDX;
import com.mygdx.runai.Scenes.HUD;

public class PlayScreen implements Screen {

    private final LibGDX game;
    private final OrthographicCamera _gameCam;

    private final Viewport _gamePort;

    private HUD _hud;
    public PlayScreen(LibGDX game){
        this.game = game;
        _gameCam = new OrthographicCamera();
        _gamePort = new ScreenViewport(_gameCam);
        _hud = new HUD(game.Batch);
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta)
    {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.Batch.setProjectionMatrix(_hud.Stage.getCamera().combined);
        _hud.Stage.draw();

    }

    @Override
    public void resize(int width, int height)
    {
        _gamePort.update(width,height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
