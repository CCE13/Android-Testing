package com.mygdx.runai.Scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


public class HUD {
    public Stage Stage;
    private Viewport _viewport;
    private Integer _worldTimer;
    private Label _timeLabel;

    private BitmapFont _poppins;


    public HUD(SpriteBatch batch){
        BitmapSetup();
        _worldTimer = 300;

        _viewport = new ScreenViewport(new OrthographicCamera());
        Stage = new Stage(_viewport,batch);
        Table table = new Table();
        table.top();
        table.setFillParent(true);

        _timeLabel = new Label(String.format("%03d",_worldTimer),new Label.LabelStyle(_poppins, Color.WHITE));

        table.add(_timeLabel).expandX().padTop(10);
        Stage.addActor(table);
    }

    private void BitmapSetup(){
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Poppins SemiBold 600.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 100;
        parameter.characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.!'()>?: ";

        _poppins = generator.generateFont(parameter);
        generator.dispose();
    }
}
