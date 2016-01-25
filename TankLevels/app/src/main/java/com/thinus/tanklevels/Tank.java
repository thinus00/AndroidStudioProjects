package com.thinus.tanklevels;

/**
 * Created by thinus on 2015/11/25.
 */
public class Tank {
    private double FULL_VALUE = 966;
    private double EMPTY_VALUE = 11000;
    private double FACTOR = -58;
    private double RADIUS_FACTOR = 828100;//(1820/2) * (1820/2);

    private int _tankID;
    private String _timestamp;
    private int _value;
    private int _persentage;
    private int _volume;
    private int _level;

    public int getTankID() { return _tankID; }
    public String getTimeStamp() { return _timestamp; }
    public int getPersentage() { return _persentage; }
    public int getVolume() { return _volume; }
    public int getValue() { return _value; }
    public int getLevel() { return _level; }

    public void setTankID(int tankId) { _tankID = tankId; }
    public void setTimeStamp(String timestamp) { _timestamp = timestamp; }
    public void setValue(int value) { _value = value; calcLevelPercVolume(); }

    Tank(int id, String timeStamp, int value) {
        this._tankID = id;
        this._timestamp = timeStamp;
        this._value = value;
        calcLevelPercVolume();
    }

    private void calcLevelPercVolume(){
        _level = (int)(((double)_value - EMPTY_VALUE) / FACTOR);
        _persentage = (int)(((EMPTY_VALUE - (double)_value) / EMPTY_VALUE) * 100);
        _volume = (int)(Math.PI * RADIUS_FACTOR * ((double)_level / 100000));
    }
}
