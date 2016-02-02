package com.example.pranav.vibeblue2;

import android.location.Location;

/*Created by bharatbatra on 11/14/15.
        */
public class Direction {

    Location myLoc;
    String turn;

    public Direction(Location myLoc, String turn) {
        this.myLoc = myLoc;
        this.turn = turn;
    }

    public Location getMyLoc() {
        return myLoc;
    }

    public void setMyLoc(Location myLoc) {
        this.myLoc = myLoc;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

}

