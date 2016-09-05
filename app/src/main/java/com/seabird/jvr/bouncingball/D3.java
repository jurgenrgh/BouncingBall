package com.seabird.jvr.bouncingball;

/**
 * Created by jvr on 30.08.2016.
 */
// A 3-dimensional vector and some useful functions, namely
    // Cartesion and Polar coordinates in xy-plane and xyz,
    // Inner product, cross product etc.
    //
    // D3()         constructor sets zeros
    // D3(x,y,z)    initializing constructor
    // r()          3d length
    // r2d()        2d xy-length
    // set(D3)      transfer values from another instance
    // ip(D3)       inner product
    // xp(D3 in, D3 out) cross product
    // phi()        x = r2d*cos(phi), y = r2d*sin(phi)
    // theta()      z = r*sin(theta)
    //
public class D3
{
    public float    x, y, z;

    D3()
    {
        x = 0;  y = 0;  z = 0;
    }
    D3(float rx, float ry, float rz)
    {
        x = rx;
        y = ry;
        z = rz;
    }

    // 3D length
    public float r()
    {
        float r2 = x*x + y*y + z*z;
        r2 = (float) Math.sqrt( r2 );
        return r2;
    }

    public void set( D3 d )
    {
        x = d.x;
        y = d.y;
        z = d.z;
    }

    // 2D length
    public float r2d()
    {
        float r2 = x*x + y*y;
        r2 = (float) Math.sqrt( r2 );
        return r2;
    }

    // Inner product
    public float ip( D3 v )
    {
        float r2 = x*v.x + y*v.y + z*v.z;
        return r2;
    }

    // Crossproduct vout = (x,y,z) x vin
    public void xp( D3 vin, D3 wout )
    {
        wout.x = y*vin.z - vin.y*z;
        wout.y = z*vin.x - vin.z*x;
        wout.z = x*vin.y - vin.x*y;
    }
    // x = r*cos(phi), y = r*sin(phi)
    // 0 <= phi <= 2PI
    public float phi()
    {
        float p = 0;
        float r = (float) Math.sqrt(x*x + y*y);

        if( r > 10.*Float.MIN_NORMAL )
        {
            float c = x / r;
            float s = y / r;
            if( (c >= 0) && (s >= 0) )  p = (float) Math.asin( s );
            if( (c <= 0) && (s >= 0) )  p = (float) Math.asin( s ) + (float)(Math.PI/2.);
            if( (c <= 0) && (s <= 0) )  p = (float) Math.asin( s ) + (float)(Math.PI);
            if( (c >= 0) && (s <= 0) )  p = (float) Math.asin( s ) + (float)(3.*Math.PI/2.);
        }
        return p;
    }
    // z = r*cos(theta)
    // 0 <= theta <= PI
    public float theta()
    {
        float p = 0;

        if( r() > 10.*Float.MIN_NORMAL )
        {
            float s = z / r();
            p = (float) Math.acos( s );
        }
        return p;
    }
}
