package liujing.magdownload.gwt.client.util;

import java.util.*;
import java.io.*;
import java.util.logging.*;

/**
 BoxBounds
 @author Break(Jing) Liu
*/
public class BoxBounds{
    
    public int left = -1;
    public int top = -1;
    public int width = -1;
    public int height = -1;
    public int right = -1;
    public int bottom = -1;
    
    public BoxBounds(){
    }
    
    public void clearAll(){
        left = -1;
        top = -1;
        width = -1;
        height = -1;
        right = -1;
        bottom = -1;
    }
    
    /** get left
     @return left
    */
    public int getLeft(){
        return left;
    }

    /** set left
     @param left left
    */
    public void setLeft(int left){
        this.left = left;
    }

    /** get top
     @return top
    */
    public int getTop(){
        return top;
    }

    /** set top
     @param top top
    */
    public void setTop(int top){
        this.top = top;
    }

    /** get width
     @return width
    */
    public int getWidth(){
        return width;
    }

    /** set width
     @param width width
    */
    public void setWidth(int width){
        this.width = width;
    }

    /** get height
     @return height
    */
    public int getHeight(){
        return height;
    }

    /** set height
     @param height height
    */
    public void setHeight(int height){
        this.height = height;
    }

    /** get right
     @return right
    */
    public int getRight(){
        return right;
    }

    /** set right
     @param right right
    */
    public void setRight(int right){
        this.right = right;
    }

    /** get bottom
     @return bottom
    */
    public int getBottom(){
        return bottom;
    }

    /** set bottom
     @param bottom bottom
    */
    public void setBottom(int bottom){
        this.bottom = bottom;
    }


}

