/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package lynea;

import java.util.ArrayList;
import java.util.List;
import lynea.player.Player;

/**
 *
 * @author Olivier
 */

public class PhysicalEntity
{
    protected float x, y, z;
    protected float rx, ry, rz, w;
    protected String animation;
    private List<Integer> headingAlreadySent = new ArrayList<Integer>();
    private List<Integer> animationAlreadySent = new ArrayList<Integer>();
    private String name;
    protected long lastUpdateTime; //server time (in ms) of the last heading update
    private float angle = 0; //angle between the local z axis and the global z axis
    private float speed = 0;

    private float endSpeed = -1;
    private long accelerationTime = -1;
    private float acceleration = 0;
    private float initialSpeed = -1;
    private long initialTime = -1;
    private Vector3 initialPosition = null;


    synchronized public long getHeadingUpdateTime() {
        return this.lastUpdateTime;
    }
    synchronized public void setHeadingUpdateTime(long t)
    {
        this.lastUpdateTime = t;
    }
    synchronized public void setHeadingUpdateTime()
    {
        this.lastUpdateTime = WorldUpdater.getInstance().getSimulationTime();
    }
    synchronized public void setName(String name){
        this.name = name;
    }
    synchronized public String getName(){
        return name;
    }
    synchronized public boolean animationHasChanged(Player animationReceiver)
    {
        int uid = animationReceiver.getUser().getUserId();
        if(!animationAlreadySent.contains(uid))
        {
            if(Player.connected.get(uid).canReceive())
            {
                animationAlreadySent.add(uid);
                return true;
            }
        }
        return false;
    }
    synchronized public void setAnimation(String animation)
    {
        if(this.animation == null || !this.animation.equals(animation))
        {
            this.animation = animation;
            animationAlreadySent.clear();
        }
    }
    synchronized public String getAnimation()
    {
        return this.animation;
    }
    synchronized public boolean headingHasChanged(Player headingReceiver)
    {
        int uid = headingReceiver.getUser().getUserId();
        if(!headingAlreadySent.contains(uid))
        {
            if(Player.connected.get(uid).canReceive())
            {
                headingAlreadySent.add(uid);
                return true;
            }
        }
        return false;
    }
    synchronized public void setTransform(float x, float y, float z, float rx, float ry, float rz, float w)
    {
        if(this.x!=x || this.y!=y || this.z!=z || this.rx!=rx || this.ry!=ry || this.rz!=rz || this.w!=w)
        {
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            setX(x);
            setY(y);
            setZ(z);
            setRX(rx);
            setRY(ry);
            setRZ(rz);
            setW(w);
            setHeadingUpdateTime();
        }
    }
    synchronized public void setTransform(PhysicalEntity e)
    {
        if(this.x!=e.getX() || this.y!=e.getY() || this.z!=e.getZ() || this.rx!=e.getRX() || this.ry!=e.getRY() || this.rz!=e.getRZ() || this.w!=e.getW())
        {
            if(!headingAlreadySent.isEmpty())
            {
                headingAlreadySent.clear();
            }
            setX(e.getX());
            setY(e.getY());
            setZ(e.getZ());
            setRX(e.getRX());
            setRY(e.getRY());
            setRZ(e.getRZ());
            setW(e.getW());
            setHeadingUpdateTime();
        }
    }
    synchronized public void setPosition(float x, float y, float z)
    {
         if(this.x!=x || this.y!=y || this.z!=z)
            {
                if(!headingAlreadySent.isEmpty())
                    headingAlreadySent.clear();
                setX(x);
                setY(y);
                setZ(z);
                setHeadingUpdateTime();
            }
    }
        synchronized public void setPosition(PhysicalEntity e)
    {
         if(this.x!=e.getX() || this.y!=e.getY() || this.z!=e.getZ())
            {
                if(!headingAlreadySent.isEmpty())
                    headingAlreadySent.clear();
                setX(e.getX());
                setY(e.getY());
                setZ(e.getZ());
                setHeadingUpdateTime();
            }
    }

    synchronized public void setOrientation(float rx, float ry, float rz, float w)
    {
        if(this.rx!=rx || this.ry!=ry || this.rz!=rz || this.w!=w)
        {
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            setRX(rx);
            setRY(ry);
            setRZ(rz);
            setW(w);
            setHeadingUpdateTime();
        }
    }
    synchronized public float getX() {
        return x;
    }
    synchronized public void setX(float x) {
        if(this.x!=x)
        {
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            this.x = x;
            setHeadingUpdateTime();
        }
    }
    synchronized public float getY() {
        return y;
    }
    synchronized public void setY(float y) {
        if(this.y!=y)
        {
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            this.y = y;
            setHeadingUpdateTime();
        }
    }
    synchronized public float getZ() {
        return z;
    }
    synchronized public void setZ(float z) {
        if(this.z!=z)
        {
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            this.z = z;
            setHeadingUpdateTime();
        }
    }
    synchronized public float getRX() {
        return rx;
    }
    synchronized public void setRX(float rx) {
        if(this.rx!=rx)
        {
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            this.rx = rx;
            setHeadingUpdateTime();
        }
    }
    synchronized public float getRY() {
        return ry;
    }
    synchronized public void setRY(float ry) {
        if(this.ry!=ry)
        {
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            this.ry = ry;
            //recalculate angle
            this.angle = 2 * (float) Math.atan2(this.ry, this.w);
            setHeadingUpdateTime();
        }
    }
    synchronized public float getRZ() {
        return rz;
    }
    synchronized public void setRZ(float rz) {
        if(this.rz!=rz)
        {
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            this.rz = rz;
            setHeadingUpdateTime();
        }
    }
    synchronized public float getW() {
        return w;
    }
    synchronized public void setW(float w) {
        if(this.w!=w)
        {
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            this.w = w;
            //recalculate angle
            this.angle = 2 * (float) Math.atan2(this.ry, this.w);
            setHeadingUpdateTime();
        }
    }
    synchronized public float getAngle()
    {
        return this.angle;
    }
    synchronized public void setAngle(float a)
    {
        if(this.angle != a)
        {
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();

            setHeadingUpdateTime();
            this.angle = a;

            //convert to (left handed) quaternion
            float newRY = (float) Math.sin(this.angle/2);
            float newW = (float) Math.cos(this.angle/2);

            //update quaternion
            setRY(newRY);
            setW(newW);
        }
    }

    /*
     * Move the entity position in the direction defined by the angle attribute
     * at a speed given as by getSpeed() and during a period which is
     * the elasped time since the last heading update. 
     *
     * This movement does not result in a clearing of the headingAlreadySent vector
     */
    synchronized public void updateHeading(int deltaTime)
    {

        //float lastUpdateTimeInSec = ((float) getHeadingUpdateTime()) / 1000.0f;
        setHeadingUpdateTime(WorldUpdater.getInstance().getSimulationTime());
        float currentUpdateTimeInSec = ((float) getHeadingUpdateTime()) / 1000.0f;

        float elapsedTimeInSec = ((float) deltaTime) / 1000.0f;
        float timeSinceAccelerationInSec = currentUpdateTimeInSec - ((float)initialTime) / 1000.0f;
        //float lastTimeSinceAccelerationInSec = lastUpdateTimeInSec - ((float)initialTime) / 1000.0f;

        
        if (isAccelerating())
        {

            this.accelerationTime -= deltaTime;
            if(this.accelerationTime > 0)
                this.speed += this.acceleration * elapsedTimeInSec;
            else
            {
                this.speed = this.endSpeed;
                this.acceleration = 0;
                this.accelerationTime = -1;
                this.initialSpeed = -1;
                this.initialTime = -1;
            }
        }
        //float savex = this.x;
        if(isAccelerating())
        {
            //these commented equations can be used instead of the ones not commented below
            //this.x += (float) Math.sin(this.angle)* this.initialSpeed * elapsedTimeInSec;
            //this.x += (float) Math.sin(this.angle)* this.acceleration/2.0f * elapsedTimeInSec * (timeSinceAccelerationInSec+lastTimeSinceAccelerationInSec);
            //this.z +=(float) Math.cos(this.angle)* this.initialSpeed * elapsedTimeInSec;
            //this.z += (float) Math.cos(this.angle)* this.acceleration/2.0f * elapsedTimeInSec * (timeSinceAccelerationInSec+lastTimeSinceAccelerationInSec);
            this.x = initialPosition.x
            + (float) Math.sin(this.angle)* this.initialSpeed * timeSinceAccelerationInSec
            + (float) Math.sin(this.angle)* this.acceleration/2 * (float) Math.pow(timeSinceAccelerationInSec, 2);

            this.z = initialPosition.z
            + (float) Math.cos(this.angle)* this.initialSpeed * timeSinceAccelerationInSec
            + (float) Math.cos(this.angle)* this.acceleration/2 * (float) Math.pow(timeSinceAccelerationInSec, 2);

        }
        else
        {
            this.x +=(float) Math.sin(this.angle)* this.speed * elapsedTimeInSec;
            this.z +=(float) Math.cos(this.angle)* this.speed * elapsedTimeInSec;
        }

        // System.out.println(
        //        "x: "+savex+"->"+this.x+" speed_x:"+speed
        //       );
  
    }


     synchronized public float getSpeed()
    {
        return this.speed;
    }

     synchronized public void setSpeed(float speed)
    {
          if(speed != this.speed)
          {
            setHeadingUpdateTime();
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            this.speed = speed;
          }
    }

    synchronized public void setEndSpeed(float endSpeed)
    {
        if(this.endSpeed != endSpeed)
        {
            this.endSpeed = endSpeed;
            setHeadingUpdateTime();
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            setupAcceleration();
         }
    }

    synchronized public void setAccelerationTime(long accelerationTime)
    {
        
        if(this.accelerationTime != accelerationTime)
        {
            this.accelerationTime = accelerationTime;
            setHeadingUpdateTime();
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            setupAcceleration();
        }
    }

    private void setupAcceleration()
    {
        boolean isAccelerating = (this.endSpeed >= 0 && this.accelerationTime > 0);
        this.acceleration = isAccelerating ? (this.endSpeed - getSpeed())/((float)this.accelerationTime/1000.0f) : 0;
        this.initialSpeed = isAccelerating ? getSpeed() : -1;
        this.initialTime = isAccelerating ? WorldUpdater.getInstance().getSimulationTime() : -1;
        this.initialPosition = new Vector3(this.x, this.y, this.z);
    }

    synchronized public boolean isAccelerating()
    {
        return (this.acceleration != 0);
    }
    
    synchronized public long getAccelerationTime()
    {
        return this.accelerationTime;
    }
    
    synchronized public float getEndSpeed()
    {
        return this.endSpeed;
    }


    public float getAcceleration() {
        return acceleration;
    }

    //TODO : integrate this class everywhere it can be 
    public class Vector3
    {
        public float x,y,z;
        public Vector3(float x, float y, float z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public float distance(PhysicalEntity other) {
        return (float) Math.sqrt((other.getX()-x)*(other.getX()-x)+(other.getY()-y)*(other.getY()-y)+(other.getZ()-z)*(other.getZ()-z));
     }
    
}
