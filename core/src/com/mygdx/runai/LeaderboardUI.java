package com.mygdx.runai;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;

public class LeaderboardUI extends Stage {
    private Leaderboard leaderboard;
    private Table table;
    private Skin skin;

    public LeaderboardUI(Viewport viewport, Batch batch, Leaderboard leaderboard, Skin skin){
        super(viewport, batch);
        this.leaderboard = leaderboard;
        this.skin = skin;

        table = new Table();
        table.setFillParent(true);
        addActor(table);
    }

//    public void updateLeaderboard(){
//        table.clearChildren();
//
//        //Create table headers
//        Label nameLabel = new Label("Player Name", skin);
//        Label scoreLabel = new Label("Distance", skin);
//        table.add(nameLabel).pad(10);
//        table.add(scoreLabel).pad(10);
//        table.row();
//
//        //Create and position the leaderboard labels
//        for (int i = 0; i < leaderboard.getAICharacters().size(); i++){
//            RunnerInterface runner = leaderboard.getAICharacters().get(i);
//            Label playerNameLabel = new Label( runner.name() , skin);
//            Label scoreValueLabel = new Label(String.valueOf(runner.returnDistance()), skin);
//            table.add(playerNameLabel).pad(10);
//            table.add(scoreValueLabel).pad(10);
//            table.row();
//
//        }
//
//    }

}
