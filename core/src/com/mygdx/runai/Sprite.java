package com.mygdx.runai;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Sprite {
    public Texture texture;
    private Vector2 position;
    private Vector2 velocity;

    public Sprite(float x, float y){
        //this.texture = texture;
        this.position = new Vector2(x,y);
        this.velocity = new Vector2(0,0);

    }

    public void update(float delta){
       position.add(0, velocity.y * delta );
    }

    public void draw(SpriteBatch batch){
        batch.draw(texture, position.x, position.y);
    }

    public void setVelocity(float x, float y) {
        velocity.set(x, y);
    }

    public Vector2 getPosition(){
        return  position;
    }


    public void setPosition(float x, float y) {

        position = new Vector2(x,y);
    }
}
