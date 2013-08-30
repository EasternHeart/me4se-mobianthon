// ME4SE - A MicroEdition Emulation for J2SE
//
// Copyright (C) 2001 Stefan Haustein, Oberhausen (Rhld.), Germany
//
// STATUS: API complete
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the
// License, or (at your option) any later version. This program is
// distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
// License for more details. You should have received a copy of the
// GNU General Public License along with this program; if not, write
// to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.

package javax.microedition.midlet;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.Manifest;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.media.Player;

import org.me4se.System;
import org.me4se.impl.JadFile;
import org.me4se.impl.Log;
import org.me4se.impl.MIDletChooser;
import org.me4se.scm.ScmContainer;
import org.me4se.scm.ScmWrapper;

import com.sixlegs.image.png.PngImage;
import org.me4se.Initializer;

/** 
 * This class is needed *here* in order to be able to call the
 * protected MIDlet startApp() method (etc.). It should perhaps be
 * hidden from the documentation.
 * 
 * @ME4SE INTERNAL 
 */
public class ApplicationManager {

	//List of J2ME Defined System Properties
    //
	//		JSR 		Property Name													Default Value		ME4SE Value
	//		30 		microedition.platform 										null						ME4SE
	//  				microedition.encoding 										ISO8859_1			ISO8859-1
	// 				microedition.configuration 								CLDC-1.0			CLDC-1.1
	// 				microedition.profiles 										null						MIDP-2.0
	//		37 		microedition.locale 											null
	//  				microedition.profiles 										MIDP-1.0
	//		75 		microedition.io.file.FileConnection.version 	1.0
	//  				file.separator 														(impl-dep)
	//  				microedition.pim.version 									1.0
	//		118 		microedition.locale 											null
	//  				microedition.profiles 										MIDP-2.0
	//  				microedition.commports 									(impl-dep)
	//  				microedition.hostname 									(impl-dep)
	//		120 		wireless.messaging.sms.smsc 							(impl-dep)
	//		139 		microedition.platform 										(impl-dep)
	//  				microedition.encoding 										ISO8859-1
	//  				microedition.configuration 								CLDC-1.1
	//  				microedition.profiles 										(impl-dep)
	//		177 		microedition.smartcardslots 							(impl-dep)
	//		179 		microedition.location.version 							1.0
	//		180 		microedition.sip.version 									1.0
	//		184 		microedition.m3g.version 								1.0
	//		185 		microedition.jtwi.version 									1.0
	//		195 		microedition.locale 											(impl-dep)
	//  				microedition.profiles 										IMP-1.0
	//		205 		wireless.messaging.sms.smsc 							(impl-dep)
	//			 		wireless.messaging.mms.mmsc 						(impl-dep)
	//		211 		CHAPI-Version 													1.0
	
    static final int DEFAULT_KEYCODE_UP = -1;
    static final int DEFAULT_KEYCODE_DOWN = -2;
    static final int DEFAULT_KEYCODE_LEFT = -3;
    static final int DEFAULT_KEYCODE_RIGHT = -4;
    static final int DEFAULT_KEYCODE_SELECT = -5;
    static final int DEFAULT_KEYCODE_LSK = -6;
    static final int DEFAULT_KEYCODE_RSK = -7;
    static final int DEFAULT_KEYCODE_CLEAR = -8;
    // -9, -10: additional softkeys (check with 9300 or similar)
    static public final int DEFAULT_KEYCODE_MENU = -100;
    static final int DEFAULT_KEYCODE_MODE = -101;
    
	static final String[] INITIALIZERS = {"media.MediaInitializer"};
		
    static final String DEFAULT_PLATFORM = "ME4SE";
    static final String DEFAULT_CONFIGURATION = "CLDC-1.1";
    static final String DEFAULT_PROFILES = "MIDP-2.0";
    static final String DEFAULT_ENCODING = "ISO8859_1";
    
	// IMPROVE:  Should we try to get rid of this static?
	// e.g. by caching and a method getManager(Midlet midlet)? 
	// Problem: Canvas wants to know its size even before assignment to MIDlet.... :(
	// Assignment to thread(s) is not feasible (no method to figure out parent
	// 
    // Possible "heuristics": getManager(null) returns "last known" manager
     
    public static final String ME4SE_VERSION_NUMBER = "2.5.1";
    public static ApplicationManager manager;

    public MIDlet active;
    public Applet applet;

    public int colorCount = 256*256*256;
    public boolean isColor = true;
    
    /** Please do not access directly, use getProperty instead! */

    public Properties properties; 
    public java.awt.Color bgColor = java.awt.Color.white;
    public ScmContainer skin;
    
    /* Logs calls to paint, etc. 
    public boolean eventLog;*/

    public JadFile jadFile = new JadFile();

    public java.awt.Image offscreen;
    
 //   public java.awt.Dimension offscreenSize;
    public ScmContainer displayContainer;
    public ScmWrapper wrapper;
    public java.awt.Frame frame;
    public java.awt.Container awtContainer;

    public int screenWidth;
    public int screenHeight;

    /** Actually shown displayable, regardless of the active MIDlet */
    public Displayable currentlyShown;
    
    public String documentBase;

  //  public boolean isApplet;

    public ClassLoader classLoader;

    /** Maps VK_ codes to button names. Filled in class Skin */

    public Hashtable virtualKeyMap = new Hashtable(); 
    
    /** Maps device key codes to game action codes. Filled in class Skin */
    
    public Hashtable gameActions = new Hashtable(); 
    
    private java.awt.MediaTracker tracker = null;
    private int trackerID = 0;
    private Hashtable imageCache = new Hashtable();
	private Class activeClass;

    public int keyStates;

    /** List of active media players -- need to be stopped when an applet is terminated */
    
    public Vector activePlayers = new Vector();
    
    public java.awt.Image createImage(byte[] data, int start, int len) {

        if ((data[start] == -119)
            && (data[start + 1] == 80)
            && (data[start + 2] == 78)
            && (data[start + 3] == 71)
            && (data[start + 4] == 13)
            && (data[start + 5] == 10)
            && (data[start + 6] == 26)
            && (data[start + 7] == 10)) {

            InputStream is = new ByteArrayInputStream(data, start, len);
            PngImage pi = new com.sixlegs.image.png.PngImage(is);

            pi.getEverything();
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return java.awt.Toolkit.getDefaultToolkit().createImage(pi);
        }
        else
            return java.awt.Toolkit.getDefaultToolkit().createImage(
                data,
                start,
                len);
    }

    public java.awt.Image createImage(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int count = is.read(buffer);
            if (count <= 0)
                break;
            os.write(buffer, 0, count);

        }
        is.close();
        os.close();

        if (os.size() == 0)
            throw new RuntimeException("empty image stream!");

        return createImage(os.toByteArray(), 0, os.size());
    }

    /** 
     * loads an image from the given file name, using the 
     * openInputStream() method. If the file extension is PNG or png,
     * the image is loaded using the sixlegs png library. */

    public java.awt.Image getImage(String fileName) throws IOException {

    	//System.out.println(imageCache);
    	
        java.awt.Image img = (java.awt.Image) imageCache.get(fileName);
        
        if (img != null)
            return img;

        InputStream is = openInputStream(fileName);
        if (is == null)
            throw new RuntimeException("null stream opening: " + fileName);

        img = createImage(is);
        imageCache.put(fileName, img);
        return img;
    }

    /** 
     * Tries to instantiate the class with the name
     * org.me4se.psi.java1.%name%
     * 
     * @param prefix
     * @param name
     * @return
     */
    
    public Object instantiate(String name) throws ClassNotFoundException {
        
        String implementation = getProperty("me4se.implementation");
        
        if(implementation != null) {
        	implementation += ";";
        }
        else {
        	implementation = "";
        }
        implementation += "org.me4se.psi.java1";

        int pos = 0;
        do{
            int cut = implementation.indexOf(';', pos);
            if(cut == -1) cut = implementation.length();
            
            String qName = implementation.substring(pos, cut)+"."+name;
            
            try{
                Class clazz = Class.forName(qName);
                return clazz.newInstance();
            }
            catch(Exception e){
            }
            
            pos = cut+1;
        }
        while(pos < implementation.length());
        
        throw new ClassNotFoundException("No implementation class found for suffix "+name);

    }
    
    public static InputStream openInputStream(Class clazz, String name) {
//            System.out.println("*** openInputStream(" + clazz + ", " + name + ")");
        
        if (name.startsWith("/")) {
            try {
                return ApplicationManager.manager.openInputStream(name);
            }
            catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }
        else {
            try {
                Package p = clazz.getPackage();
                String s = "";
                if (p != null) {
                    s = p.getName().replace('.', '/');
                }
                
                System.out.println(s);
                
                return ApplicationManager.manager.openInputStream(s + "/" + name);
            }
            catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
    
    /**  Read the whole stream and return a byteArrayInputStream */ 
    
    public InputStream openInputStream(String fileName) throws IOException{
        
        InputStream is = _openInputStream(fileName);      
        if(is == null) return null;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[2048];
        
        while(true){
            int count = is.read(buf);
            if(count <= 0) break;
            baos.write(buf, 0, count);
        }
        
        return new ByteArrayInputStream(baos.toByteArray());
    }
    
    /** 
     * opens an input stream on the given resource; if the path is
     * not absolute and not an URL, the documentBase is taken into
     * account. */

    public InputStream _openInputStream(String fileName) throws IOException {
        Log.log(Log.IO, "open stream: " + fileName);
        
        fileName = concatPath(documentBase, fileName);

            // System.out.println ("concatenated: "+fileName);

        if (isUrl(fileName)) {
            //System.out.println("opening url: " + fileName);
            return new URL(fileName).openStream();
        }
        // try resource

        try { /*
        		       			fileName = fileName.replace('\\', '/');
        		       			 if (!fileName.startsWith("/"))
        		       				 fileName = "/" + fileName;
        		               	*/
            InputStream result = null;

            //System.out.println("Try to use activeClass ...");
            if(activeClass != null) {
        			result = activeClass.getResourceAsStream(fileName);
        			if(result != null) return result;
            }
            
            //System.out.println("Try to use classLoader ...");
            if(classLoader != null) {
                result = classLoader.getResourceAsStream(fileName);
                if(result != null) return result;
            }
            
            //System.out.println("Try to use ApplicationManager  ...");
            result = ApplicationManager.class.getResourceAsStream(fileName);
            if(result != null) return result;
        }
        catch (Exception e) {
        	e.printStackTrace();
        }

        // file otherwise
        //System.out.println("opening file: " + fileName);

        if(applet != null){
            System.out.println("WARNING: Resource not found: "+fileName);
            return null;
        }
        else {
            return new FileInputStream(fileName);
        }
    }

    public String getProperty(String name) {
        String result = properties.getProperty(name.toLowerCase());
        return result == null 
        	? (applet != null
        			? applet.getParameter(name)
        			: java.lang.System.getProperty(name))
            : result;
    }

    public String getProperty(String name, String dflt) {
    	String result = getProperty(name);
        return result != null ? result : dflt;
    }
    
    public static String[] split(String s){
    	
    	if(s == null) return new String[0];
    	s = s.trim();
    	if(s.length() == 0) return new String[0];
    	
    	Vector v = new Vector();
    	
    	while(true){
    		int cut = s.indexOf(' ');
    		if(cut == -1) break;
    		v.addElement(s.substring(0, cut));
    		s = s.substring(cut+1).trim();
    	}
    	
    	String[] res = new String[v.size()+1];
    	for(int i = 0; i < res.length-1; i++){
    		res[i] = (String) v.elementAt(i);
    	}
    	res[res.length-1] = s;
    	return res;
    }
    
    
    /** determines whether path starts with http:// or file:// */

    public static boolean isUrl(String path) {
        String test = path.toLowerCase();
        return test.startsWith("http://") || test.startsWith("file:");
    }

    /** if file is absolute (starts with /, http:// or file://, 
     * or the base path is null, file is returned; otherwise the 
     * last index of / or \ is searched in base and 
     * the strings are concatenated. Please note that this is not equivalent to
     * kobjects.buildUrl; buildUrl always adds "file://" for 
     * urls with no location type given. 
     * 
     * @IMPROVE: It may make sense to use Util.buildUrl here and 
     * map file:/// secretly to "resource:" in openStream??  */

    public static String concatPath(String base, String file) {
        if (base == null
            || file.startsWith("/")
            || file.startsWith("\\")
            || (file.length() >= 2 && file.charAt(1) == ':')
            || isUrl(file))
            return file;

        int cut = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));

        return base.substring(0, cut + 1) + file;
    }

    java.awt.Image lastWait;
    

    public int getImageWidth(java.awt.Image image, String info) {
        while (true) {
            int w = image.getWidth(null);
            if (w != -1)
                return w;
            waitForImage(image, info);
        }
    }

    public int getImageHeight(java.awt.Image image, String info) {
        while (true) {
            int h = image.getHeight(null);
            if (h != -1)
                return h;
            waitForImage(image, info);
        }
    }

    public void drawImage(
        java.awt.Graphics g,
        java.awt.Image image,
        int x,
        int y,
        String info) {
        int cnt = 0;
        while (!g.drawImage(image, x, y, null)) {
            waitForImage(image, info);
        }
    }

    public void waitForImage(java.awt.Image image, String info) {
        //System.out.println ("enter waitforimage" +manager.displayContainer);

        if (image == null)
            throw new RuntimeException("Image " + info + " is null");

        //System.out.println("wait for img: " + info);
        if (manager.tracker == null)
            manager.tracker =
                new java.awt.MediaTracker(
                    ApplicationManager.manager.awtContainer);

        int id = manager.trackerID;
        try {

            if (lastWait != image) {
                id = ++manager.trackerID;
                manager.tracker.addImage(image, id);
                lastWait = image;
            }

            //   System.out.println("status of ID "+id+" is:" +manager.tracker.statusID(id, true));

            // manager.awtContainer.getGraphics().drawImage(image, 0, 0, null);

            manager.tracker.waitForID(id);
            //            System.out.println("ok for image "+info);

            if (manager.tracker.isErrorID(id)) {
                throw new RuntimeException(
                    "error loading image '"
                        + info
                        + "'; tracker: "
                        + manager.tracker);
                //Thread.sleep(100);
            }

            manager.tracker.removeImage(image);

        }
        catch (InterruptedException ie) {
        }

        //System.out.println("image ok: " + info);
        //System.out.println ("leave waitforimage");
    }

    public ApplicationManager(
        java.awt.Panel container,
        Properties properties) {

    	//TODO Version Number of ME4SE !
    	System.out.println("---=== ME4SE Version " + ME4SE_VERSION_NUMBER + " ===---");
    	
        if (manager != null)
            manager.destroy(true, true);  // notify, may not exit, killall

        manager = this;
        
        this.properties = properties;
        
        String locale = Locale.getDefault().getLanguage()
            +"-"+Locale.getDefault().getCountry();

        if(container instanceof Applet){
            applet = (Applet) container;
        	    documentBase = applet.getDocumentBase().toString();
        }

        setSystemProperty("microedition.platform", DEFAULT_PLATFORM);
        setSystemProperty("microedition.encoding", DEFAULT_ENCODING);
        setSystemProperty("microedition.locale", locale);
        setSystemProperty("microedition.configuration", DEFAULT_CONFIGURATION);
        setSystemProperty("microedition.profiles", DEFAULT_PROFILES);
        
        
        for(int i = 0; i < INITIALIZERS.length; i++){
        	String name = INITIALIZERS[i];
        	
        	try {
				Initializer init = (Initializer) instantiate(name);
				System.out.println("initializer found for "+name+": "+init.getClass());
				init.initialize(this);
			} catch (ClassNotFoundException e) {
				System.out.println("cannot initialize module: "+name);
			} catch(Exception e){
				e.printStackTrace();
			}
        	
        }
        
        
        
        wrapper =
            new ScmWrapper(this,
                new Float(getProperty("scale", "1")).floatValue());
        //FontInfo.cache = new Hashtable ();

        
        if (container != null) {
            awtContainer = container;
            
            container.setLayout(new java.awt.BorderLayout());

            screenWidth = container.getSize().width;
            screenHeight = container.getSize().height;
        }
        else {
            frame = new java.awt.Frame("ME4SE MIDlet");
            
            screenWidth =
                getIntProperty("width", getIntProperty("screen.width", 150));
            screenHeight =
                getIntProperty("height", getIntProperty("screen.height", 200));


            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent ev) {
                    destroy(true, true); // notify, mayExit, killAll
                }
            });
            frame.addComponentListener(new java.awt.event.ComponentAdapter() {
                public void componentResized(java.awt.event.ComponentEvent e) {
                }
            });

            awtContainer = frame;
            //frame.show ();
        }

        if (getProperty("skin") != null) {
            try {
                skin =
                    (ScmContainer) Class
                        .forName("org.me4se.impl.skins.Skin")
                        .newInstance();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            displayContainer = skin;

            // access to skin properties available now

            screenWidth = getIntProperty("screen.width", screenWidth);
            screenHeight = getIntProperty("screen.height", screenHeight);
        }
        else {
            displayContainer = new ScmContainer() {
                public Dimension getPreferredSize() {
                    return getMinimumSize();
                }
                public Dimension getMinimumSize() {
                    return new Dimension(screenWidth, screenHeight);
                }
                public void paint(java.awt.Graphics g) {
                    if (getComponentCount() == 0) {
                        g.drawString("Loading; please wait...", 20, 20);
                    }
                    else {
                        super.paint(g);
                    }
                }
            };
            displayContainer.setBackground(new java.awt.Color(0x0ffffff));
        }

        // create list to force up/down image loading before midlet start
        
     //   new List("", List.IMPLICIT);
        
        wrapper.setComponent(displayContainer);
        //	wrapper.setFocusable (true);

        awtContainer.add(wrapper, java.awt.BorderLayout.CENTER);

        if (skin != null && frame != null) {
            frame.pack();
            frame.show();
            frame.setResizable(true);
        }
        else if (applet != null) {
            wrapper.invalidate();
            awtContainer.validate();
        }
	if(frame != null)
	{
	    frame.setResizable(true);
	}
        
        // the MIDP API does not permit runtime size changes
        // runtime size changes may result in unpredictable
        // behaviour
        
        Log.mask  = getIntProperty("me4se.eventlog", 0);
    }

    
    /** not called if MIDlet provides a main method */

    public void launch(JadFile jadFileParam) {
        
        /* all the stuff below should be done inside the 
         * connection implementation in a static method or on the first call.

        if (!isApplet && getProperty("me4se.comm.enable") != null) {
            try {
                ConnectionImpl connectionImpl =
                    (ConnectionImpl) Class
                        .forName("org.me4se.psi.javgcf.ConnectionImpl_comm")
                        .newInstance();
                connectionImpl.initialise(null);
            }
            catch (Exception e) {
                throw new RuntimeException(
                    "Error initializing CommConnection:" + e);
            }

        }

        // Initialization code necessary for the WMA implementation.

        if (!isApplet && System.getProperty("me4se.wma.enable") != null) {
            try {
                ConnectionImpl connectionImpl =
                    (ConnectionImpl) Class
                        .forName("org.me4se.impl.gcf.ConnectionImpl_sms")
                        .newInstance();

                Properties props = new Properties();
                props.put(
                    "me4se.wma.enable",
                    System.getProperty("me4se.wma.enable", "true"));
                props.put(
                    "me4se.wma.d211enable",
                    System.getProperty("me4se.wma.d211enable", "false"));
                props.put(
                    "me4se.wma.commport",
                    System.getProperty("me4se.wma.commport", "COM1"));
                props.put(
                    "me4se.wma.baudrate",
                    System.getProperty("me4se.wma.baudrate", "19200"));
                props.put(
                    "me4se.wma.debug",
                    System.getProperty("me4se.wma.debug", "false"));
                connectionImpl.initialise(props);
            }
            catch (Exception e) {
            	e.printStackTrace();
                throw new RuntimeException(
                    "Error initializing WMA implememtation:" + e);
            }
        }
         */
    	
    	
    	// laod jadfile if possible

        if(jadFileParam != null){
            jadFile = jadFileParam;
        }
        else{
        	jadFile = new JadFile();
            String jadUrl = getProperty("jad");

            if (jadUrl != null){
                try {
                	jadFile.load(openInputStream(jadUrl));
                }
                catch (Exception e) {
                    System.err.println("JAD access error: "+e+"; trying midlet/jam property");
                }
            }
        }        
        
        
        // now find manifest
        
    	JadFile manifest = new JadFile();

    	try {
    		if(applet != null){
    			manifest.load(openInputStream("/META-INF/MANIFEST.MF"));
    		}
    		else{
    			Enumeration e = this.getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
    			while(e.hasMoreElements()){
    				URL url = (URL) e.nextElement();
    				manifest.load(url.openStream());

    				if(manifest.getMIDletCount() != 0) {
    					break;
    				}
    			}
    		} 
    	}
    	catch (Exception e) {
			System.out.println("Error while reading/searchin MANIFEST.MF");
			e.printStackTrace();
		}

    	// merge manifest
    	
    	if(manifest.getMIDletCount() > 0){
    		for(int i = 0; i < manifest.size(); i++){
    			String key = manifest.getName(i);
    		
    			if(key != null && jadFile.getValue(key) == null){
    				jadFile.setValue(key, manifest.getValue(key));
    			}
    		}
    	}

    	
        if(jadFile.getValue("MIDlet-1")==null){
            String midlet = getProperty("MIDlet");
            if(midlet != null){
            	jadFile.setValue("MIDlet-1", midlet+ ",,"+midlet);
            }
            else {        	
            	throw new RuntimeException("Invalid MIDlet, or jad specified");
            }
        }

        // care about rms location

        if (applet != null) {
            org.me4se.impl.rms.RecordStoreImpl_file.rmsDir = null;
        }
        else {
            //			The directory is now named .rms/ by default.
            // and created only if RecrodStores are used
            // in the MIDlet.
            File rmsDir = new File(getProperty("rms.home", ".rms"));
            org.me4se.impl.rms.RecordStoreImpl_file.rmsDir = rmsDir;
        }

        try {
            if (jadFile.getMIDletCount() == 1){
            		activeClass = Class
                    .forName(jadFile.getMIDlet(1).getClassName());
                active =
                    ((MIDlet) activeClass.newInstance());
            }
            else
                active = new MIDletChooser();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
	if(frame != null)
        {
            frame.setResizable(true);
        }
    }

    public int getIntProperty(String name, int dflt) {
        try {
            return Integer
                .decode((String) ApplicationManager.manager.getProperty(name))
                .intValue();
        }
        catch (Exception e) {
            return dflt;
        }
    }
    
    int discretize(int value, int steps){
    	double v = value / 255.0;                // 0..1
		v = (int) ((steps - 1) * v + 0.5);   // 0..step
		v *= 255.0 / (steps - 1);            // 0..255

	//	System.out.println("discretize ("+value+","+steps+")="+(int)v);

		return (int) v;		    	
    }

    public int getDeviceColor(int color) {

		if(isColor && colorCount > 65536 && java.awt.Color.white.equals(ApplicationManager.manager.bgColor))
			return color;

		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = (color & 255);

		if (isColor){
			int scnt = 2;
			int cnt = 8;
			while(cnt < colorCount){
				scnt *= 2;
				cnt *= 8;
			}

			r = discretize(r, scnt);
			g = discretize(g, scnt);
			b = discretize(b, scnt);
		}
		else {
	        double grayscale = (r+g+b) / (3.0 * 255.0); 

   	        grayscale = (int) ((colorCount - 1) * grayscale + 0.5);
       	    grayscale /= (colorCount - 1);

	        java.awt.Color bg = ApplicationManager.manager.bgColor;

	        r = (int) grayscale * bg.getRed();
        	g = (int) grayscale * bg.getGreen();
        	b = (int) grayscale * bg.getBlue();
		}
        return (color & 0x0ff000000) | (r << 16) | (g << 8) | b;
    }

    //void setRmsDir(String dflt) {

    //}

    public void start() {
        if (active != null){
            // execute possibly pending setCurrent 
            Displayable d = Display.getDisplay(active).getCurrent();
            if(d != null){
                Display.getDisplay(active).setCurrent(d);
            }
                
            try{
                active.startApp();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void pause() {

        if (active != null){
            try{
                active.pauseApp();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

	/** Destruction requested "externally" or by midlet */

    public void destroy(boolean notifyMIDlet, boolean killAll) {

		//imageCache = new Hashtable();

    	ApplicationManager manager = ApplicationManager.manager;

    	
		String activeName = active == null ? "" : manager.active.getClass().getName();

		if(notifyMIDlet && active != null) {
	        try {
	        	active.inDestruction = true;
                active.destroyApp(true);
                active = null;
                
			}
			catch(Exception e){
				e.printStackTrace();				
			}
		}

		// stop pending sounds
        
        for(int i = manager.activePlayers.size()-1; i >= 0; i--){
    		((Player) manager.activePlayers.elementAt(i)).close();
    	}
    	
        
		// Try to fall back to something 

		int midletCount = manager.jadFile.getMIDletCount();

		try {
			if ((midletCount > 1 || applet != null) 
                    && !killAll 
                    && !activeName.equals("org.me4se.impl.MIDletChooser")) {
					manager.startMIDlet("org.me4se.impl.MIDletChooser");
			}
			else {  
				active = null;
				org.me4se.System.exit(0);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			active = null;
			org.me4se.System.exit(0);
		}
    }

    
    /**
     * Instantiates and launches the given MIDlet. DestroyApp is called for any running MIDlet.
     */
    public void startMIDlet(String name)
        throws
            InstantiationException,
            ClassNotFoundException,
            IllegalAccessException {
            	
        if(active != null && !active.inDestruction){
            try{
        		    active.inDestruction = true;
        		    active.destroyApp(true);
            }
        	    catch(Exception e){
        	        e.printStackTrace();
        	    }
        }
            	
        MIDlet midlet = null;

        if (classLoader != null) {
            midlet = ((MIDlet) classLoader.loadClass(name).newInstance());
        }
        else {
            midlet = ((MIDlet) Class.forName(name).newInstance());
        }

        // Throw away everything in the current DisplayContainer. Tell
        // the ApplicationManager that the newly created MIDlet is the
        // active one. Then run it.
        //        displayContainer.removeAll();
        //displayContainer.setLayout(new java.awt.BorderLayout());
        //displayContainer.setBackground(bgColor);

        active = midlet;
        start();
    }

    public Object getComponent(String name) {
        String custom = getProperty(name + ".component");
        if (custom == null)
            return null;
        try {
            return Class
                .forName("javax.microedition.lcdui." + custom)
                .newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    public boolean getFlag(String flag) {
        String f = getProperty("me4se.flags");
        return f == null
            ? false
            : f.toLowerCase().indexOf(flag.toLowerCase()) != -1;
    }

    
    public boolean getBooleanProperty(String name, boolean dflt){
        String v = getProperty(name);
        return v == null ? dflt :  "true".equalsIgnoreCase(v.trim());
    }
    
    /** Maps a virtual key code to a button name */
    
    public String getButtonName(KeyEvent e){
    	
    	 
    	int vk = e.getKeyCode();

		String name = (String) virtualKeyMap.get(new Integer(vk));
		if (name != null){
			return name;
		}

		if (vk >= KeyEvent.VK_F1 && vk <= KeyEvent.VK_F8) {
			return "SOFT" + (vk - KeyEvent.VK_F1 + 1);
		}

		switch (vk) {
		case KeyEvent.VK_ESCAPE:
			return "BACK";
		case KeyEvent.VK_DELETE:
		case KeyEvent.VK_BACK_SPACE:
			return "CLEAR";
		case KeyEvent.VK_ENTER:
			return "SELECT";
		case KeyEvent.VK_LEFT:
			return "LEFT";
		case KeyEvent.VK_RIGHT:
			return "RIGHT";
		case KeyEvent.VK_UP:
			return "UP";
		case KeyEvent.VK_DOWN:
			return "DOWN";
		}

		char c = e.getKeyChar();
		
		return (c >= ' ' && c < 256) ? "" + c : "VK_" + vk;
    }
    
    
    /** Translate a device key code to a game action */
    
    public int getGameAction(int keyCode){
    		Integer ga = ((Integer) gameActions.get(new Integer(keyCode)));
    		if (ga != null){
    		   return ga.intValue(); 
    		}  
     
         switch(keyCode){
         case DEFAULT_KEYCODE_UP: return Canvas.UP;
         case DEFAULT_KEYCODE_DOWN: return Canvas.DOWN;
         case DEFAULT_KEYCODE_LEFT: return Canvas.LEFT;
         case DEFAULT_KEYCODE_RIGHT: return Canvas.RIGHT;
         case DEFAULT_KEYCODE_SELECT: return Canvas.FIRE;
         default:
             return keyCode;
         }
            
    }
    
    
    /** Maps a button name to a device key code */
    
    public int getKeyCode(String buttonName){

    	//	System.out.println("getKeyCode: "+buttonName);
		int i = getIntProperty("keycode."+buttonName, -12345);
    		if (i != -12345) return i;
    	
   		if(buttonName.length() == 1) return buttonName.charAt(0);

   		buttonName = buttonName.toUpperCase();
    	
    	if(buttonName.equals("POUND")) return '#';
    	if(buttonName.equals("ASTERISK")) return '*';
    	if(buttonName.equals("UP")) return DEFAULT_KEYCODE_UP;
    	if(buttonName.equals("DOWN")) return DEFAULT_KEYCODE_DOWN;
    	if(buttonName.equals("LEFT")) return DEFAULT_KEYCODE_LEFT;
    	if(buttonName.equals("RIGHT")) return DEFAULT_KEYCODE_RIGHT;
    	if(buttonName.equals("SELECT")) return DEFAULT_KEYCODE_SELECT;
    	if(buttonName.equals("SOFT1")) return DEFAULT_KEYCODE_LSK;
    	if(buttonName.equals("SOFT2")) return DEFAULT_KEYCODE_RSK;
    	if(buttonName.equals("SOFT3")) return -9;
    	if(buttonName.equals("SOFT4")) return -10;
    	if(buttonName.equals("CLEAR")) return DEFAULT_KEYCODE_CLEAR;
    	if(buttonName.equals("MENU")) return DEFAULT_KEYCODE_MENU;
    	if(buttonName.equals("MODE")) return DEFAULT_KEYCODE_MODE;
        
    	return i;
    }
    
    
	 public static String decodeJavaEscape(String encoded){
	 	if(encoded.indexOf('\\') == -1) return encoded;
	 	
	 	StringBuffer buf = new StringBuffer();
	 	for(int i = 0; i < encoded.length(); i++){
	 		if(encoded.charAt(i) == '\\'){
	 			char c = encoded.charAt(++i);
	 			switch(c){
	 			case 't': 
	 				buf.append('\t'); 
	 				break;
	 			case 'n': 
	 				buf.append('\r'); 
	 				break;
	 			case 'r': 
	 				buf.append('\n'); 
	 				break;
	 			case '\\': 
	 				buf.append('\\'); 
	 				break;
	 			case 'u': 
	 				buf.append((char) Integer.parseInt(encoded.substring(i+1, i+5), 16));
	 				i+=4;
	 				break;
	 			default: 
	 				buf.append('\\');
	 				buf.append(c);
	 			}
	 		}
	 		else{
	 			buf.append(encoded.charAt(i));
	 		}
	 	}
	 	return buf.toString();
	 }

	/** This method must be called to set system properties because of the applet security issues */
	 
	public void setSystemProperty(String key, String value) {
		if(applet != null){
			org.me4se.System.properties.setProperty(key, value);
		}
		else {
			java.lang.System.setProperty(key, value);
		}
	}
	 
   /* public int getKeyCode(String name){
    	
    }*/
    
}
