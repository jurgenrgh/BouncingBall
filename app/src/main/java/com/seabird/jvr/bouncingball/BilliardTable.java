package com.seabird.jvr.bouncingball;

/**
 * Created by jvr on 30.08.2016.
 * Provides the pysical parameters relevant to the motion
 * of a billiard ball on a bounded flat surface
 */
public class BilliardTable
{
    public  int     xMin, xMax, yMin, yMax;
    public  float   rollingFriction;    //0.005-0.015 proportion of vertical force
    public  float   slidingFriction;    //0.15-0.4 proportion of vertical force
    public  float   spinDeceleration;   //5-15 rad/sec^2
    public  float   railRestitution;    //0.6-0.9 Ratio of velocity after to before collision
                                        // this is also sqrt(h/H), height after to before of dropped ball

    public BilliardTable()
    {
        // Arbitrary initialization
        // Each instance should overwrite these values
        xMin = 0;
        xMax = 200;
        yMin = 0;
        yMax = 300;

        // Mid values
        rollingFriction     = 0.01f;    //0.005-0.015 proportion of vertical force
        slidingFriction     = 0.25f;    //0.15-0.4 proportion of vertical force
        spinDeceleration    = 10f;      //5-15 rad/sec^2
        railRestitution     = 0.75f;    //0.6-0.9 Ratio of velocity after to before collision
    }

    public BilliardTable( int xMin, int xMax, int yMin, int yMax )
    {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;

        // Mid values
        rollingFriction     = 0.01f;    //0.005-0.015 proportion of vertical force
        slidingFriction     = 0.25f;    //0.15-0.4 proportion of vertical force
        spinDeceleration    = 10f;      //5-15 rad/sec^2
        railRestitution     = 0.75f;    //0.6-0.9 Ratio of velocity after to before collision
    }

    public BilliardTable( int xMin, int xMax, int yMin, int yMax,
                          float rollingFriction,
                          float slidingFriction,
                          float spinDeceleration,
                          float railRestitution )
    {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;

        this.rollingFriction  = rollingFriction;
        this.slidingFriction  = slidingFriction;
        this.spinDeceleration = spinDeceleration;
        this.railRestitution  = railRestitution;
    }
}
