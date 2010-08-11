/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package lynea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lynea.player.Player;

/**
 *
 * @author Olivier
 */
public class PhysicalEntity
{
    protected double x, y, z;
    protected double rx, ry, rz, w;
    protected String animation;
    private List<Integer> transformAlreadySent = new ArrayList<Integer>();
    private List<Integer> animationAlreadySent = new ArrayList<Integer>();
    private String name;

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
    synchronized public boolean transformHasChanged(Player transformReceiver)
    {
        int uid = transformReceiver.getUser().getUserId();
        if(!transformAlreadySent.contains(uid))
        {
            transformAlreadySent.add(uid);
            return true;
        }
        return false;
    }
    synchronized public void setTransform(double x, double y, double z, double rx, double ry, double rz, double w)
    {
        if(this.x!=x || this.y!=y || this.z!=z || this.rx!=rx || this.ry!=ry || this.rz!=rz || this.w!=w)
        {
            transformAlreadySent.clear();
            this.x = x;
            this.y = y;
            this.z = z;
            this.rx = rx;
            this.ry = ry;
            this.rz = rz;
            this.w = w;
        }
    }
    synchronized public void setTransform(PhysicalEntity e)
    {
        if(this.x!=e.getX() || this.y!=e.getY() || this.z!=e.getZ() || this.rx!=e.getRX() || this.ry!=e.getRY() || this.rz!=e.getRZ() || this.w!=e.getW())
        {
            transformAlreadySent.clear();
            this.x = e.getX();
            this.y = e.getY();
            this.z = e.getZ();
            this.rx = e.getRX();
            this.ry = e.getRY();
            this.rz = e.getRZ();
            this.w = e.getW();
        }
    }
    synchronized public void setPosition(double x, double y, double z)
    {
         if(this.x!=x || this.y!=y || this.z!=z)
            {
                transformAlreadySent.clear();
                this.x = x;
                this.y = y;
                this.z = z;
            }
    }
    synchronized public void setOrientation(double rx, double ry, double rz, double w)
    {
        if(this.rx!=rx || this.ry!=ry || this.rz!=rz || this.w!=w)
        {
            transformAlreadySent.clear();
            this.rx = rx;
            this.ry = ry;
            this.rz = rz;
            this.w = w;
        }
    }
    synchronized public double getX() {
        return x;
    }
    synchronized public void setX(double x) {
        if(this.x!=x)
        {
            transformAlreadySent.clear();
            this.x = x;
        }
    }
    synchronized public double getY() {
        return y;
    }
    synchronized public void setY(double y) {
        if(this.y!=y)
        {
            transformAlreadySent.clear();
            this.y = y;
        }
    }
    synchronized public double getZ() {
        return z;
    }
    synchronized public void setZ(double z) {
        if(this.z!=z)
        {
            transformAlreadySent.clear();
            this.z = z;
        }
    }
    synchronized public double getRX() {
        return rx;
    }
    synchronized public void setRX(double rx) {
        if(this.rx!=rx)
        {
            transformAlreadySent.clear();
            this.rx = rx;
        }
    }
    synchronized public double getRY() {
        return ry;
    }
    synchronized public void setRY(double ry) {
        if(this.ry!=ry)
        {
            transformAlreadySent.clear();
            this.ry = ry;
        }
    }
    synchronized public double getRZ() {
        return rz;
    }
    synchronized public void setRZ(double rz) {
        if(this.rz!=rz)
        {
            transformAlreadySent.clear();
            this.rz = rz;
        }
    }
    synchronized public double getW() {
        return w;
    }
    synchronized public void setW(double w) {
        if(this.w!=w)
        {
            transformAlreadySent.clear();
            this.w = w;
        }
    }


    
}
