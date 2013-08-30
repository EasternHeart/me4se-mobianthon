// ME4SE - A MicroEdition Emulation for J2SE
//
// Copyright (C) 2001 Stefan Haustein, Oberhausen (Rhld.), Germany
//
// Contributors: Sebastian Vastag
//
// STATUS:
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

package org.me4se;

//import java.net.*;
import java.applet.*;
//import java.awt.*;
import javax.microedition.midlet.*;

import org.me4se.impl.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class MIDletRunner extends Applet {

  //  public static boolean isApplet;

	static int startCount;
	
    public void start() {
        System.out.println("Applet start was called. Application Mangaer: "+ApplicationManager.manager);

        startCount ++;
        
        if (ApplicationManager.manager == null) {
            new ApplicationManager(this, new Properties()).launch(null);
            ApplicationManager.manager.start();
        }
        else {
            boolean restartable = ApplicationManager.manager.getBooleanProperty("me4se.restartable", true);
        
            Log.log(Log.APPLICATION_EVENT, "Calling ApplicationManager.manager.start(); restartable: "+restartable);

            if(restartable || startCount > 2){ // avoid death spiral
	            ApplicationManager.manager.destroy(true, true); // notify, killall
              
	            new ApplicationManager(this, new Properties()).launch(null);
                ApplicationManager.manager.start();
            }
            else { 
            	String containerUrl = getDocumentBase().toString();
            	int cut = containerUrl.indexOf("frld=");
            	if(cut == -1){
            		containerUrl += containerUrl.indexOf('?') == -1 ? '?' : '&'; 
            	}
            	else {
            		containerUrl = containerUrl.substring(0, cut);
            	}
            	
                try {
					getAppletContext().showDocument(
							new URL(containerUrl+"frld="+Long.toString(System.currentTimeMillis(), 36)));
				} catch (MalformedURLException e) {
					e.printStackTrace();
					throw new RuntimeException(""+e);
				}
            }
        }
    }

    
    public void stop() {
        boolean restartable = ApplicationManager.manager.getBooleanProperty("me4se.restartable", true);
        
        Log.log(Log.APPLICATION_EVENT, "Applet.stop() called. restartable: "+restartable);
        
        if(restartable){
            ApplicationManager.manager.pause();
        }
    }
    

    public void destroy() {
        ApplicationManager.manager.destroy(true, true); // notify,  killall
    }

    public boolean isFocusTraversable() {
        return false;
    }

    public static void main(String[] argv) {

        Properties param = new Properties();
        //String midlet = null;
        //String jadfile = null;

        for (int i = 0; i < argv.length; i++) {

            if (argv[i].startsWith("-")) {
                param.put(argv[i].substring(1), argv[i + 1]);
                i++;
            }
            else{
            int p = argv[i].indexOf(".jad");
            if (p!=-1 && p == argv[i].length() - 4)
                param.put("jad", argv[i]);
            else
                param.put("midlet", argv[i]);
            }
        }

        if (param.get("midlet") == null && param.get("jad") == null) {
            param.put("jam", "org.me4se.impl.jam.MIDletSuiteManager");
            param.put("midlet", "org.me4se.impl.jam.MIDletSuiteManager");
        }

        new ApplicationManager(null, param).launch(null);
        ApplicationManager.manager.start();
    }
}
