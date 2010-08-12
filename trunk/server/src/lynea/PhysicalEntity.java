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
    private float speedForCurrentAnimation = 0;

    synchronized public long getHeadingUpdateTime() {
        return this.lastUpdateTime;
    }
    synchronized public void setHeadingUpdateTime(long t)
    {
        this.lastUpdateTime = t;
    }
    synchronized public void setHeadingUpdateTime()
    {
        this.lastUpdateTime = Clock.getTime();
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
            animationAlreadySent.add(uid);
            return true;
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
            headingAlreadySent.add(uid);
            return true;
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
            this.lastUpdateTime = Clock.getTime();
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
            this.lastUpdateTime = Clock.getTime();
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
                this.lastUpdateTime = Clock.getTime();
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
                this.lastUpdateTime = Clock.getTime();
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
            this.lastUpdateTime = Clock.getTime();
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
            this.lastUpdateTime = Clock.getTime();
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
            this.lastUpdateTime = Clock.getTime();
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
            this.lastUpdateTime = Clock.getTime();
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
            this.lastUpdateTime = Clock.getTime();
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
            this.lastUpdateTime = Clock.getTime();
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
            this.lastUpdateTime = Clock.getTime();
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
            this.lastUpdateTime = Clock.getTime();
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

            this.lastUpdateTime = Clock.getTime();
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
     * at a speed given as by getSpeedForCurrentAnimation() and during a period which is
     * the elasped time since the last heading update. 
     *
     * This movement does not result in a clearing of the headingAlreadySent vector
     */
    synchronized public void updateHeading()
    {
        float elapsedTime = (float) (Clock.getTime() - this.lastUpdateTime);
        this.x += (float) Math.sin(this.angle)*getSpeedForCurrentAnimation()*elapsedTime/1000.0f;
        this.z += (float) Math.cos(this.angle)*getSpeedForCurrentAnimation()*elapsedTime/1000.0f;
        this.lastUpdateTime = Clock.getTime();
    }

     synchronized public float getSpeedForCurrentAnimation()
    {
        if(getAnimation() != null)
        {
            return this.speedForCurrentAnimation;
        }
        return 0;
    }

     synchronized protected void SetSpeedForCurrentAnimation(float speed)
    {
          if(speed != this.speedForCurrentAnimation)
          {
            this.lastUpdateTime = Clock.getTime();
            if(!headingAlreadySent.isEmpty())
                headingAlreadySent.clear();
            this.speedForCurrentAnimation = speed;
          }
    }


    
}
