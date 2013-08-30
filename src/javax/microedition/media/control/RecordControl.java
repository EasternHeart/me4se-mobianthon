/*
 * Created on 02.07.2005
 */
package javax.microedition.media.control;

import javax.microedition.media.Control;

/**
 * @author Michael Kroll
 * @API MMAPI-1.0
 */
public interface RecordControl extends Control {
    
    /**
     * @API MMAPI-1.0
     */     
    public void commit();

    /**
     * @API MMAPI-1.0
     */     
    public String getContentType();

    /**
     * @API MMAPI-1.0
     */ 
    public void reset();

    /**
     * @API MMAPI-1.0
     */     
    public void setRecordLocation(java.lang.String locator);
    
    /**
     * @API MMAPI-1.0
     */ 
    public int setRecordSizeLimit(int size);
    
    /**
     * @API MMAPI-1.0
     */ 
    public void setRecordStream(java.io.OutputStream stream);
    
    /**
     * @API MMAPI-1.0
     */ 
    public void startRecord();
    
    /**
     * @API MMAPI-1.0
     */ 
    public void stopRecord(); 
}
