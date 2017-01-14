package com.seabird.jvr.bouncingball;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

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

    public enum Compass{N,S,E,W}

    public float eps = 0.0001f;
    private float sqrt2 = (float) Math.sqrt(2.);
    public float   R = 50;             // radius, actual 2.25in = 5.70cm
    public float   M = 0.17f;          // mass kg, 6oz = 170g
    public int     nColor = Color.RED; // int Color
    public float   dpm = 6400;          // 6400dp/m

    public D3 C = new D3(0,0,R);  //Ball center, device coordinates
    public D3 N = new D3(1f/sqrt2,0,1f/sqrt2);  //North (Z) fixed relative to the ball
    public D3 E = new D3(1f/sqrt2,0,-1f/sqrt2);  //East (X) fixed relative to ball
    public D3 Y = new D3(0,1,0);  //Normal to N and E (Y fixed in Ball)
    public D3 V = new D3(0,0,0);  //Velocity of the center sp/sec
    public D3 P = new D3(0,0,0);  //Angular velocity vector

    public BilliardTable table;    //Supplies physical parameters of the table


    public class RailsCollision
    {
        boolean left;
        boolean right;
        boolean top;
        boolean bot;

        public RailsCollision( boolean l, boolean r, boolean t, boolean b)
        {
            left = l;
            right = r;
            top = t;
            bot = b;
        }
        public RailsCollision()
        {
            left = false;
            right = false;
            top = false;
            bot = false;
         }
    }
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

    // Center is corrected for rMS milliseconds elapsed
    // no acceleration
    public void doStep( double tMilli )
    {
        D3 A = new D3(0,0,0);
        doStep( A, tMilli );
    }

    // Increments the center coordinates given time increment
    // and acceleration. Acceleration is m/sec*sec. Time is ms.
    // Rail reflection is taken into account
    public void doStep( D3 A, double tMilli )
    {
        RailsCollision coll = new RailsCollision();
        double tSec = tMilli/1000.; // 1000ms/sec

        double dtMilli = tMilli;    // interval up to rail
        double dtSec = tSec;        //

        String strTem = new String("");
        int loopCount = 0;
        boolean bCheck = checkRailReflection( dtSec, A, coll );

        // If rail rebound; move only up to rail, reflect,
        // then do the rest both coordinates independently

//        strTem = String.format( "t= %1$8.5f  x= %2$8.2f  y= %3$8.2f  vx= %4$8.2f vy= %5$8.2f  ax= %6$8.2f  ay= %7$8.2f  tt= %8$8.5f", dtSec, C.x, C.y, V.x, V.y, A.x, A.y, tSec );
//        Log.i( "Strt", strTem );

        loopCount = 0;
        dtSec = tSec;
        while( (coll.left || coll.right) && (dtSec > eps) && (loopCount < 5)  )
        {
            loopCount++;
            D3 lRoots = new D3(-1,-1,-1);
            D3 rRoots = new D3(-1,-1,-1);

            float tLeft  = -1;
            float tRight = -1;

            // Time to each rail
            // Correct center and velocity; so now the ball is
            // exactly on the rail and velociy reversed.
            // Then do up to next collision if any
            if( coll.left )
            {
                tLeft = solveQuadratic(0.5f*dpm*A.x, V.x, C.x - table.xMin - R, lRoots);
                if( (lRoots.x > eps) && (lRoots.y > lRoots.x) ) tLeft = lRoots.x;
                if( (lRoots.y > eps) && (lRoots.x > lRoots.y) ) tLeft = lRoots.y;
                if( tLeft < 0 ) tLeft = eps;

                C.x = table.xMin + R;
                V.x = -(V.x + A.x * tLeft * dpm)* table.railRestitution;

                dtSec = dtSec - tLeft;

//                strTem = String.format( "t= %1$8.5f  x= %2$8.2f  y= %3$8.2f  vx= %4$8.2f vy= %5$8.2f  ax= %6$8.2f  ay= %7$8.2f  tt= %8$8.5f  n= %9$5d", tLeft, C.x, C.y, V.x, V.y, A.x, A.y, tSec, loopCount );
//                Log.i( "RLft", strTem );
            }
            if( coll.right )
            {
                tRight = solveQuadratic(0.5f*dpm*A.x, V.x, C.x - table.xMax + R, rRoots);
                if( (rRoots.x > eps) && (rRoots.y > rRoots.x) ) tRight = rRoots.x;
                if( (rRoots.y > eps) && (rRoots.x > rRoots.y) ) tRight = rRoots.y;
                if( tRight < 0 ) tRight = eps;

                C.x = table.xMax - R;
                V.x = -(V.x + A.x * tRight * dpm) * table.railRestitution;

                dtSec = dtSec - tRight;

//                strTem = String.format( "t= %1$8.5f  x= %2$8.2f  y= %3$8.2f  vx= %4$8.2f vy= %5$8.2f  ax= %6$8.2f  ay= %7$8.2f  tt= %8$8.5f  n= %9$5d", tRight, C.x, C.y, V.x, V.y, A.x, A.y, tSec,  loopCount);
//                Log.i( "RRgt", strTem );
            }

            bCheck = checkRailReflection( dtSec, A, coll );
        }
        if( loopCount < 5)
        {
            C.x = (float) (C.x + V.x * dtSec + 0.5 * dpm * A.x * dtSec * dtSec);
            V.x = (float) (V.x + A.x * dtSec * dpm);
        }

//        strTem = String.format( "t= %1$8.5f  x= %2$8.2f  y= %3$8.2f  vx= %4$8.2f vy= %5$8.2f  ax= %6$8.2f  ay= %7$8.2f  tt= %8$8.5f", dtSec, C.x, C.y, V.x, V.y, A.x, A.y, tSec, loopCount );
//        Log.i( "EndX", strTem );

        dtSec = tSec;
        loopCount = 0;
        while( (coll.top || coll.bot) && (dtSec > eps) && (loopCount < 5))
        {
            loopCount++;
            D3 tRoots = new D3(-1,-1,-1);
            D3 bRoots = new D3(-1,-1,-1);

            float tTop   = -1;
            float tBot   = -1;

            if( coll.top )
            {
                tTop = solveQuadratic(0.5f*dpm*A.y, V.y, C.y - table.yMin - R, tRoots);
                if( (tRoots.x > eps) && (tRoots.y > tRoots.x) ) tTop = tRoots.x;
                if( (tRoots.y > eps) && (tRoots.x > tRoots.y) ) tTop = tRoots.y;
                if( tTop < 0 ) tTop = eps;

                C.y = table.yMin + R;
                V.y = -(V.y + A.y * tTop * dpm) * table.railRestitution;

                dtSec = dtSec - tTop;

//                strTem = String.format( "t= %1$8.5f  x= %2$8.2f  y= %3$8.2f  vx= %4$8.2f vy= %5$8.2f  ax= %6$8.2f  ay= %7$8.2f  tt= %8$8.5f  n= %9$5d", tTop, C.x, C.y, V.x, V.y, A.x, A.y, tSec, loopCount );
//                Log.i( "RTop", strTem );
            }
            if( coll.bot )
            {
                tBot = solveQuadratic(0.5f*dpm*A.y, V.y, C.y - table.yMax + R, bRoots);
                if( (bRoots.x > eps) && (bRoots.y > bRoots.x) ) tBot = bRoots.x;
                if( (bRoots.y > eps) && (bRoots.x > bRoots.y) ) tBot = bRoots.y;
                if( tBot < 0 ) tBot = eps;

                C.y = table.yMax - R;
                V.y = -(V.y + A.y * tBot * dpm) * table.railRestitution;

                dtSec = dtSec - tBot;

//                strTem = String.format( "t= %1$8.5f  x= %2$8.2f  y= %3$8.2f  vx= %4$8.2f vy= %5$8.2f  ax= %6$8.2f  ay= %7$8.2f  tt= %8$8.5f  n= %9$5d", tBot, C.x, C.y, V.x, V.y, A.x, A.y, tSec, loopCount);
//                Log.i( "RBot", strTem );
            }

            bCheck = checkRailReflection( dtSec, A, coll );
        }

        if( loopCount < 5 )
        {
            C.y = (float) (C.y + V.y * dtSec + 0.5 * dpm * A.y * dtSec * dtSec);
            V.y = (float) (V.y + A.y * dtSec * dpm);
        }

//        strTem = String.format( "t= %1$8.5f  x= %2$8.2f  y= %3$8.2f  vx= %4$8.2f vy= %5$8.2f  ax= %6$8.2f  ay= %7$8.2f  tt= %8$8.5f  n= %9$5d", dtSec, C.x, C.y, V.x, V.y, A.x, A.y, tSec, loopCount );
//        Log.i( "EndY", strTem );

        C.x = Math.max(C.x, table.xMin + R);
        C.x = Math.min(C.x, table.xMax - R);
        C.y = Math.max(C.y, table.yMin + R);
        C.y = Math.min(C.y, table.yMax - R);
    }

    // Given center coordinates check if ball hits the rail
    // Returns specific rail
    boolean checkRailReflection( double tSec, D3 A, RailsCollision rc )
    {
        boolean bReply = false;
        rc.left = false;
        rc.right = false;
        rc.top = false;
        rc.bot = false;

        // Projected displacement
        float x = (float) (C.x + V.x * tSec + 0.5 * dpm * A.x * tSec*tSec);
        float y = (float) (C.y + V.y * tSec + 0.5 * dpm * A.y * tSec*tSec);

        if((y < (table.yMin + R)) )
        {
            bReply = true;
            rc.top = true;
        }
        if((y > (table.yMax - R)) )
        {
            bReply = true;
            rc.bot = true;
        }
        if((x < (table.xMin + R)) )
        {
            bReply = true;
            rc.left = true;
        }
        if((x > (table.xMax - R)) )
        {
            bReply = true;
            rc.right = true;
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

    //Return value is the smaller positive root, if any;
    //otherwise returns a negative value, roots returns both
    //roots
    public float solveQuadratic( float a, float b, float c, D3 roots )
    {
        float r = -1;
        float r1 = -1;
        float r2 = -1;
        float disc;

        roots.x = -1;
        roots.y = -1;
        roots.z = -1;

        disc = b*b - 4f*a*c;
        if( disc < 0 ) r = -1;
        else
        {
            disc = (float) Math.sqrt( disc );
            r1 = (-b + disc)/(2*a);
            r2 = (-b - disc)/(2*a);
            if( (r1 >= 0) && ((r2 >= r1) || (r2 < 0)) )
            {
                r = r1;
            }
            if( (r2 >= 0) && ((r1 >= r2) || (r1 < 0)) )
            {
                r = r2; //The smaller positive root
                r2 = r1;
                r1 = r;
            }

            roots.x = r1;
            roots.y = r2;
        }
        return r;
    }
}
