package com.seabird.jvr.bouncingball;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;

import java.util.List;

/**
 * Created by jvr on 29.07.2016.
 * Basic dynamics of a ball rolling on a flat surface
 * The major simplification is that the ball does not slide
 * and, therefore the axis of rotation is perpendicular to the
 * axial velocity v of the c.m.; and the magnitude of the rotation is
 * v/r.
 * There is thus never a rotation component in the z-direction
 *
 * Methods:
 *
 * Constructor(table) sets arbitrary initial values and table parameters supplied externally
 * Constructor(position, radius, color, table) leaves v = 0 and NS = z, E = x
 * setVelocity
 */
public class RollingBall
{
    // the initial values have no significance except to provide validity
    // all distances are dp

    private int nRep = 0;

    private float sqrt2 = (float) Math.sqrt(2.);
    public float   R = 50;             // radius, actual 2.25in = 5.70cm
    public float   M = 0.17f;          // mass kg, 6oz = 170g
    public int     nColor = Color.RED; // int Color

    public D3 C = new D3(0,0,R);  //Ball center, device coordinates
    public D3 N = new D3(1f/sqrt2,0,1f/sqrt2);  //North (Z) fixed relative to the ball
    public D3 E = new D3(1f/sqrt2,0,-1f/sqrt2);  //East (X) fixed relative to ball
    public D3 Y = new D3(0,1,0);  //Normal to N and E (Y fixed in Ball)
    public D3 V = new D3(0,0,0);  //Velocity of the center sp/sec
    public D3 P = new D3(0,0,0);  //Angular velocity vector

    public BilliardTable table;    //Supplies physical parameters of the table


    //Table must always be passed; provides physical parameters
    //Other values set to defaults
   public RollingBall( BilliardTable table )
   {
       this.table = table;
       R = 50;
       C.x = 0;
       C.y = 0;
       C.z = R;
       nColor = Color.RED;
   }

    // Explicit initial values for center (x,y,z=R) and color
    // table must be given; provides physical parameters
    public RollingBall( float rx, float ry, float rR, int nC, BilliardTable table )
    {
        C.x = rx;
        C.y = ry;
        C.z = rR;
        R   = rR;
        nColor = nC;
        this.table = table;
    }

    //Impose axial velocity
    public void setVelocity( D3 dV)
    {
        V.set(dV);
    }

    // Shifts the center without changing anything else
    // but corrects for rail reflection
    public void shiftCenter( D3 dp )
    {
        C.x += dp.x;
        C.y += dp.y;
        C.z += dp.z;
        //ToDo Check rail reflection
        //ToDo - adjust moving frame
    }

    // Center is corrected for
    // rMS milliseconds elapsed
    // no acceleration
    public void doStep( double tMilli )
    {
        D3 A = new D3(0,0,0);
        doStep( A, tMilli );
    }

    // Increments the center coordinates given time increment
    // and acceleration. Acceleration is m/sec*sec. Time is ms.
    // Rail reflection is taken into account
    //
    public void doStep( D3 A, double tMilli )
    {
        double tSec = tMilli/1000.; // 1000ms/sec
        float  dpm = 6400;          // 6400dp/m
        double dtMilli = tMilli;    // interval up to rail
        double dtSec = tSec;        //
        int ixRail = 0;             //which rail 0,1,2,3,4 = none, top,bot,lft,rt

        // Projected displacement
        float x = (float) (C.x + V.x * tSec + 0.5 * dpm * A.x * tSec*tSec);
        float y = (float) (C.y + V.y * tSec + 0.5 * dpm * A.y * tSec*tSec);
        float z = (float) (C.z + V.z * tSec + 0.5 * dpm * A.z * tSec*tSec);

//        Log.i("doStep", "x,y = " + String.valueOf(x) +","+ String.valueOf(y));
//        Log.i("doStep", "v,v = " + String.valueOf(V.x) +","+ String.valueOf(V.y));

        Boolean bCheck = checkRailReflection(x,y);

//        Log.i("doStep", "Check = " + bCheck);


        // If rail rebound; move only up to rail, reflect,
        // then do the rest recursively
        if( bCheck )
        {
            nRep++;
            if(nRep > 1)
            {
                Log.i("doStep", "cx,cy = " + String.valueOf(x) +","+ String.valueOf(y));
                Log.i("doStep", "vx,vy = " + String.valueOf(V.x) +","+ String.valueOf(V.y));
                Log.i("doStep", "ax,ay = " + String.valueOf(A.x) +","+ String.valueOf(A.y));
            }

            // Time to each rail
            float tTop      = solveQuadratic(0.5f*dpm*A.y, V.y, C.y - table.yMin - R);
            float tBot      = solveQuadratic(0.5f*dpm*A.y, V.y, C.y - table.yMax + R);
            float tLeft     = solveQuadratic(0.5f*dpm*A.x, V.x, C.x - table.xMin - R);
            float tRight    = solveQuadratic(0.5f*dpm*A.x, V.x, C.x - table.xMax + R);

            //choose the smallest time - there could be more than one collision
            if( (tTop > 0f) && (dtSec > tTop) && (V.y < 0) )
            {
                dtSec = tTop;
                ixRail = 1;
            }
            if( (tBot > 0f) && (dtSec > tBot) && (V.y > 0))
            {
                dtSec = tBot;
                ixRail = 2;
            }
            if( (tLeft > 0f) && (dtSec > tLeft) && (V.x < 0) )
            {
                dtSec = tLeft;
                ixRail = 3;
            }
            if( (tRight > 0f) && (dtSec > tRight) && (V.x > 0) )
            {
                dtSec = tRight;
                ixRail = 4;
            }

            // Correct center and velocity; so now the ball is
            // exactly on the rail
            C.x = (float) (C.x + V.x * dtSec + 0.5 * dpm * A.x * dtSec*dtSec);
            C.y = (float) (C.y + V.y * dtSec + 0.5 * dpm * A.y * dtSec*dtSec);
            C.z = (float) (C.z + V.z * dtSec + 0.5 * dpm * A.z * dtSec*dtSec);

            V.x += A.x * dtSec * dpm;
            V.y += A.y * dtSec * dpm;
            V.z += A.z * dtSec * dpm;

            // Invert the velocity component normal to the rail
            if((ixRail == 1) || (ixRail == 2) ) V.y = -V.y;
            if((ixRail == 3) || (ixRail == 4) ) V.x = -V.x;

            // Recursive call to handle the remaining time interval
            double tRest = tMilli - dtSec*1000;
            doStep( A, tRest );
        }
        else //No reflection; do the whole time interval
        {
            nRep = 0;

            C.x = (float) (C.x + V.x * tSec + 0.5 * dpm * A.x * tSec*tSec);
            C.y = (float) (C.y + V.y * tSec + 0.5 * dpm * A.y * tSec*tSec);
            C.z = (float) (C.z + V.z * tSec + 0.5 * dpm * A.z * tSec*tSec);

            V.x += A.x * tSec * dpm;
            V.y += A.y * tSec * dpm;
            V.z += A.z * tSec * dpm;
        }
    }

    // Given center coordinates check if ball hits the rail
    public Boolean checkRailReflection( float x, float y )
    {
        Boolean bReply = false;

        if( (y < (R + table.yMin)) || (y > (table.yMax - R)) ||
            (x < (R + table.xMin)) || (x > (table.xMax - R)) )
        {
            if(nRep > 0)
            {
                Log.i( "doStep", "cx,cy = " + String.valueOf( x ) + "," + String.valueOf( y ) );
            }
            bReply = true;
        }
        return bReply;
    }

    //Draw accto current values
    public void draw( Canvas canvas )
    {
        Paint paint = new Paint();
        paint.setColor( nColor );
        canvas.drawCircle( C.x, C.y, R, paint );
        paint.setColor( Color.RED );
        canvas.drawCircle(C.x + R*N.x, C.y +  R*N.y, R/10, paint );
        paint.setStyle( Paint.Style.STROKE );
        //ToDo - draw the coordinate ellipses
    }

    /**
     * Reflection of the ball against rectangular borders
     * Both position af the center and velocity are corrected
     *
     */
    public void railReflection( )
    {
        float rTop = table.yMin;
        float rBot = table.yMax;
        float rLeft = table.xMin;
        float rRight = table.xMax;

        if( C.y <= (R + rTop - 1.) )
        {
            C.y = 2*R + 2*rTop - C.y;
            V.y = -V.y * table.railRestitution;
        }
        if( C.y >= (rBot - R + 1.) )
        {
            C.y = -2*R + 2*rBot - C.y;
            V.y = -V.y * table.railRestitution;
        }
        if( C.x <= (R + rLeft - 1.) )
        {
            C.x = 2*R + 2*rLeft - C.x;
            V.x = -V.x * table.railRestitution;
        }
        if( C.x >= (rRight - R + 1.) )
        {
            C.x = -2*R + 2*rRight - C.x;
            V.x = -V.x * table.railRestitution;
        }
        //ToDo - update moving frame
    }

    // Checks whether ball[nBix] would collide (i.e. overlap)
    // any of the other balls in the list. If so position and
    // velocity of both is corrected
    public void checkBallOverlap( float dx, float dy, int nBix, List<RollingBall> balls )
    {
        float rCx = balls.get(nBix).C.x;
        float rCy = balls.get(nBix).C.y;
        float rR  = balls.get(nBix).R;

        for( int i = 0; i < balls.size(); i++ )
        {
            if( i != nBix )
            {
                // if overlap
                if( Math.pow( (balls.get(i).C.x - rCx), 2)
                    + Math.pow( (balls.get(i).C.y - rCy), 2)
                        < (Math.pow( balls.get(i).R + rR, 2)))
                {
                    undoOverlap( i, nBix, balls );
                    doBallCollision( i, nBix, balls );
                }
            }
        }
    }

    // The two balls touch. The velocity components in the
    // direction of the line joining the centers are interchanged
    // This is correct only for equal masses
    private void doBallCollision( int i, int nBix, List<RollingBall> balls )
    {
        float rC1x = balls.get(nBix).C.x;
        float rC1y = balls.get(nBix).C.y;
        float rC2x = balls.get(i).C.x;
        float rC2y = balls.get(i).C.y;

        float rD  = (float) Math.sqrt( Math.pow( rC2x - rC1x, 2 ) + Math.pow( rC2y - rC1y, 2 ));
        float rDx = (rC2x - rC1x)/rD;
        float rDy = (rC2y - rC1y)/rD;

        D3 V1 = balls.get(nBix).V;
        D3 V2 = balls.get(i).V;

        float rProj1 = V1.x * rDx + V1.y * rDy;
        float rProj2 = V2.x * rDx + V2.y * rDy;

        V1.x = V1.x - rProj1*rDx + rProj2*rDx;
        V1.y = V1.y - rProj1*rDy + rProj2*rDy;
        V2.x = V2.x - rProj2*rDx + rProj1*rDx;
        V2.y = V2.y - rProj2*rDy + rProj1*rDy;

        balls.get(nBix).setVelocity(V1);
        balls.get(i).setVelocity(V2);
        //ToDo - adjust moving frame
    }

    // Two balls overlap (or might overlap). Step back
    // to correct so they just touch.
    private void undoOverlap( int i, int nBix, List<RollingBall> balls )
    {
        float rC1x = balls.get(nBix).C.x;
        float rC1y = balls.get(nBix).C.y;
        float rC2x = balls.get(i).C.x;
        float rC2y = balls.get(i).C.y;
        float rR1  = balls.get(nBix).R;
        float rR2  = balls.get(i).R;
        float rDelta, rDeltaX, rDeltaY;
        float rD;
        D3 prDelta1, prDelta2;

        rD = (float) Math.sqrt( Math.pow( rC2x - rC1x, 2 ) + Math.pow( rC2y - rC1y, 2 ));
        rDelta = rR2 + rR1 - rD;

        rDeltaX = rDelta*(rC2x - rC1x)/rD;
        rDeltaY = rDelta*(rC2y - rC1y)/rD;

        prDelta1 = new D3( -rDeltaX/2, -rDeltaY/2, 0 );
        prDelta2 = new D3( rDeltaX/2, rDeltaY/2, 0 );

        balls.get(nBix).shiftCenter(prDelta1);
        balls.get(i).shiftCenter(prDelta2);
        //ToDo - adjust moving frame
    }

    //Returns the smaller positive root, if any;
    //otherwise returns a negative value
    public float solveQuadratic( float a, float b, float c )
    {
        float root = -1;
        float r1 = -1;
        float r2 = -1;

        float disc;

        disc = b*b - 4f*a*c;
        if( disc < 0 ) root = -1;
        else
        {
            disc = (float) Math.sqrt( disc );
            r1 = (-b + disc)/(2*a);
            r2 = (-b - disc)/(2*a);
            if( (r1 >= 0) && ((r2 >= r1) || (r2 < 0)) ) root = r1;
            if( (r2 >= 0) && ((r1 >= r2) || (r1 < 0)) ) root = r2; //The smaller positive root
        }
        return root;
    }
}
