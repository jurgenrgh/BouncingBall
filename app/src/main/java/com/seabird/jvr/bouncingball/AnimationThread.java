package com.seabird.jvr.bouncingball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;

/**
 * Created by jvr on 28.08.2016.
 */
public class AnimationThread extends Thread
{
    private final Paint paint = new Paint( Paint.ANTI_ALIAS_FLAG );
    private int canvasWidth = 200;
    private int canvasHeight = 400;
    private boolean running = false;

    private float accelX = 0;
    private float accelY = 0;
    private float accelZ = 0;

    private long nowTime, newTime, delTime;

    SurfaceHolder threadHolder;
    Context threadContext;
    Handler threadHandler;
    BilliardTable table;
    RollingBall ball;

    public AnimationThread( SurfaceHolder surfaceHolder, Context context, Handler handler, BilliardTable table )
    {
        threadHolder = surfaceHolder;
        threadHandler = handler;
        threadContext = context;
        this.table = table;
        int clr = ContextCompat.getColor( threadContext, R.color.ball_ivory);
        ball = new RollingBall(300,300,50,clr,table);
    }

    public void doInitParams()
    {
        synchronized( threadHolder )
        {
            nowTime = System.currentTimeMillis();
            delTime = 0;
        }
    }

    //Set and reset running flag
    public void setRunning( boolean b )
    {
        running = b;
    }

    public void setSurfaceSize( int width, int height )
    {
        synchronized( threadHolder )
        {
            canvasWidth = width;
            canvasHeight = height;

            table.xMin = 0;
            table.xMax = width;
            table.yMin = 0;
            table.yMax = height;

            doInitParams();
        }
    }

    public void run()
    {
        // forever - as long as running flag set
        while( running )
        {
            Canvas c = null;
            try
            {
                c = threadHolder.lockCanvas( null );
                synchronized( threadHolder )
                {
                    if( c != null )
                    {
                        synchronized(((MainActivity) threadContext).accelValues)
                        {
                            accelX = ((MainActivity) threadContext).accelValues.x;
                            accelY = ((MainActivity) threadContext).accelValues.y;
                            accelZ = ((MainActivity) threadContext).accelValues.z;
                        }
                        doDrawing( c );
                    }
                }
            }
            finally
            {
                if( c != null )
                {
                    threadHolder.unlockCanvasAndPost( c );
                }
            }
            try{ Thread.sleep( 10 ); }
            catch(InterruptedException ex){ Thread.currentThread().interrupt(); }
        }
    }

    //This runs synchronized and does the actual drawing
    //
    private void doDrawing( Canvas canvas )
    {
        newTime = System.currentTimeMillis();
        delTime = newTime - nowTime;
        nowTime = newTime;

        canvas.drawColor( 0xFF2E7D32 );
        paint.setStyle( Paint.Style.FILL );
        fillArrow(canvas, canvasWidth/2, canvasHeight/2, (canvasWidth/2) + 20*accelX, (canvasHeight/2) + 20*accelY);
        D3 a = new D3(accelX, accelY, accelZ);
        ball.doStep( a, delTime );
        ball.railReflection( );
        ball.draw(canvas);
    }

    private void fillArrow(Canvas canvas, float x0, float y0, float x1, float y1)
    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth( 6f );

        float deltaX = x1 - x0;
        float deltaY = y1 - y0;
        float frac = (float) 0.1;

        float point_x_1 = x0 + (float) ((1 - frac) * deltaX + frac * deltaY);
        float point_y_1 = y0 + (float) ((1 - frac) * deltaY - frac * deltaX);

        float point_x_2 = x1;
        float point_y_2 = y1;

        float point_x_3 = x0 + (float) ((1 - frac) * deltaX - frac * deltaY);
        float point_y_3 = y0 + (float) ((1 - frac) * deltaY + frac * deltaX);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        path.moveTo(x0,y0);
        path.lineTo(point_x_2, point_y_2);
        path.lineTo(point_x_3, point_y_3);
        path.lineTo(point_x_1, point_y_1);
        path.lineTo(point_x_2, point_y_2);

        path.close();

        canvas.drawPath(path, paint);
    }
}
