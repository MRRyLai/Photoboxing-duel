package com.google.mlkit.vision.demo.java.facedetector;

import android.util.Log;

import java.util.Observable;

class Ob_timer extends Observable {
    private String track_id="0";

    public String getTrack_id(){
        return track_id;
    }
    void setTrack_id(String track_id){
        super.setChanged();
        super.notifyObservers(track_id);
        this.track_id = track_id;
        Log.v("ObTimer","現在Track id:"+this.track_id);
    }

    @Override
    public String toString() {
        return "Ob_timer{" +
                "track_id=" + track_id +
                '}';
    }
}

