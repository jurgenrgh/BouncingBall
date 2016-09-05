package com.seabird.jvr.bouncingball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by jvr on 28.08.2016.
 */
public class BallSurfaceView extends SurfaceView
        implements SurfaceHolder.Callback

{
    private SurfaceHolder holder;
    private Paint paint = new Paint( Paint.ANTI_ALIAS_FLAG);
    AnimationThread thread;
    Context mainContext;
    Handler handler = new Handler();

    BilliardTable table = new BilliardTable();


    private long nowTime, oldTime;
    private boolean color = false;
    private float rFriction = 0.05f;

    private float ballRadius = 80; // Ball's radius
    private float ballX = ballRadius + 20;  // Ball's center (x,y)
    private float ballY = ballRadius + 40;
    private float ballSpeedX = 0;  // Ball's speed (x,y)
    private float ballSpeedY = 0;  // nominal sp/sec

    private RectF ballBounds;      // Needed for Canvas.drawOval

    // These 3 constructors are required
    //
    public BallSurfaceView(Context context)
    {
        super(context);
        Initialize(context);
    }

    public BallSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        Initialize(context);
    }

    public BallSurfaceView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        Initialize(context);
    }

    private void Initialize(Context context)
    {
        mainContext = context;
        ballBounds = new RectF();
        nowTime = System.currentTimeMillis();
        oldTime = System.currentTimeMillis();


        holder = getHolder();
        holder.addCallback(this);
        setFocusable(true); // make sure we get key events
    }

    // The real entry point and thread creation
    //
    public void surfaceCreated(SurfaceHolder holder)
    {
        //Start the thread
        //
        holder = getHolder();
        table.xMax = holder.getSurfaceFrame().width();
        table.yMax = holder.getSurfaceFrame().height();

        thread = new AnimationThread( holder, mainContext, handler, table );
        thread.setRunning(true);
        thread.start();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        table.xMin = 0;
        table.xMax = width;
        table.yMin = 0;
        table.yMax = height;

        thread.setSurfaceSize(width, height);
    }
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        //Kill the thread
        //
        boolean retry = true;
        thread.setRunning(false); //Ends the endless thread loop
        while (retry)
        {
            try
            {
                thread.join();
                retry = false;
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    public AnimationThread getThread()
    {
        return thread;
    }
}
