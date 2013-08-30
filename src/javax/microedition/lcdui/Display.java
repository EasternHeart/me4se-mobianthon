// ME4SE - A MicroEdition Emulation for J2SE 
//
// Copyright (C) 2001 Stefan Haustein, Oberhausen (Rhld.), Germany
//
// Contributors:
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

// should we remove the dependency on the application manager?

package javax.microedition.lcdui;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.midlet.ApplicationManager;
import javax.microedition.midlet.MIDlet;

import org.me4se.scm.ScmComponent;

/**
 * @API MIDP-1.0
 * @API MIDP-2.0  
 */
public class Display {

	/**
	 * @API MIDP-2.0
	 */
	public static final int LIST_ELEMENT = 1;
	
	/**
	 * @API MIDP-2.0
	 */
	public static final int CHOICE_GROUP_ELEMENT = 2;

	/**
	 * @API MIDP-2.0
	 */
	public static final int ALERT = 3;
	
	/**
	 * @API MIDP-2.0
	 */
	public static final int COLOR_BACKGROUND = 0;
	
	/**
	 * @API MIDP-2.0
	 */
	public static final int COLOR_FOREGROUND = 1;
	
	/**
	 * @API MIDP-2.0
	 */
	public static final int COLOR_HIGHLIGHTED_BACKGROUND = 2;
	
	/**
	 * @API MIDP-2.0
	 */
	public static final int COLOR_HIGHLIGHTED_FOREGROUND = 3;
	
	/**
	 * @API MIDP-2.0
	 */
	public static final int COLOR_BORDER = 4;

	/**
	 * @API MIDP-2.0
	 */	
	public static final int COLOR_HIGHLIGHTED_BORDER = 5;
	
	private static Hashtable midlets = new Hashtable();

	private MIDlet midlet;
	protected Displayable current;
	private static ScmDisplayable currentContainer;
	protected Vector callSerially = new Vector();
	protected TickerThread tickerThread = new TickerThread(this);

	/**
	 * @ME4SE INTERNAL
	 */
	protected Display(MIDlet midlet) {
		this.midlet = midlet;
		tickerThread.start();
	}

	/**
	 * @API MIDP-1.0 
	 */
	public void callSerially(Runnable r) {
		callSerially.addElement(r);
		if (current != null)
			current.container.repaint();
	}

	/**
     * @API MIDP-2.0
     * @ME4SE UNSUPPORTED
	 */
	public boolean flashBacklight(int duration) {
		return false;
	}

	/**
	 * Requests operation of the device's vibrator. The vibrator is intended 
	 * to be used to attract the user's attention or as a special effect for games. 
	 * The return value indicates if the vibrator can be controlled by the application. 
	 * This method switches on the vibrator for the requested duration, or switches it off
	 * if the requested duration is zero. If this method is called while the vibrator 
	 * is still activated from a previous call, the request is interpreted as setting 
	 * a new duration. It is not interpreted as adding additional time to the original 
	 * request. This method returns immediately; that is, it must not block the caller 
	 * while the vibrator is running.
	 * 
	 * Calls to this method are honored only if the Display is in the foreground. 
	 * This method MUST perform no action and return false if the Display is in 
	 * the background.
	 * 
	 * The device MAY limit or override the duration. For devices that do not include a 
	 * controllable vibrator, calls to this method return false.
	 * 
	 * @param duration - the number of milliseconds the vibrator should be run, 
	 *                   or zero if the vibrator should be turned off
	 * @return true if the vibrator can be controlled by the application and this
	 *         display is in the foreground, false otherwise
	 * @throws IllegalArgumentException - if duration is negative
	 * 
	 * @API MIDP-2.0
	 * @ME4SE UNIMPLEMENTED
	 */
	public boolean vibrate(int duration) {
		System.out.println("Display.vibrate() called with no effect !");
		return false;		
	}

	/**
	 * @API MIDP-1.0 
	 */
	public static Display getDisplay(MIDlet midlet) {

		Display display = (Display) midlets.get(midlet);
		if (display == null) {
			display = new Display(midlet);
			midlets.put(midlet, display);
		}

		return display;
	}

	/**
	 * @API MIDP-1.0 
	 */
	public synchronized void setCurrent(Displayable d) {

		if(d == null) return;

		current = d;
		
		ApplicationManager manager = ApplicationManager.manager;

		if (manager.currentlyShown == d || ApplicationManager.manager.active != midlet)
			return;

		if (currentContainer != null)
			manager.displayContainer.remove(currentContainer);

		if (manager.currentlyShown instanceof Canvas)
			 ((Canvas) manager.currentlyShown).hideNotify();

		d.container.setX(manager.getIntProperty("screen.x", 0));
		d.container.setY(manager.getIntProperty("screen.y", 0));

		if (d instanceof Alert) {
			Alert alert = (Alert) d;
			if (alert.next == null)
				alert.next = current;
		}

		manager.displayContainer.add(d.container);
		d.display = this;
		manager.currentlyShown = d;
		currentContainer = d.container;
		//currentContainer.requestFocus();
		current._showNotify();
		//System.out.println("Display.Current: " + current);
		manager.wrapper.requestFocus();
		currentContainer.repaint();
	}

	/**
	 * @API MIDP-1.0 
	 */
	public void setCurrent(Alert alert, Displayable next) {
		alert.next = next;
		setCurrent(alert);
	}

	/**
	 * @API MIDP-2.0
	 */
	public void setCurrentItem(Item item) {
        setCurrent(item.form);
        ((ScmComponent) item.lines.elementAt(item.lines.size() == 0 ? 0 : 1)).requestFocus();
	}
 

	/**
	 * @API MIDP-1.0 
	 */
	public boolean isColor() {
		return true;
	}
    
    /**
     * @API MIDP-2.0
     * TODO: Fix this: read property!
     */
    public int numAlphaLevels(){
        return 256;
    }

	/**
	 * @API MIDP-1.0 
	 */
	public int numColors() {
		return ApplicationManager.manager.colorCount;
	}

	/**
	 * @API MIDP-1.0 
	 */
	public Displayable getCurrent() {
		return current;
	}

    /**
     * @API MIDP-2.0 
     */
    public int getColor(int type) {
        switch(type){
        case COLOR_BACKGROUND: return ApplicationManager.manager.getIntProperty("item.background", 0x0ffffff);   
        case COLOR_FOREGROUND: return ApplicationManager.manager.getIntProperty("item.foreground", 0);   
        case COLOR_HIGHLIGHTED_BACKGROUND: return ApplicationManager.manager.getIntProperty("item.focus.background", 0x00000ff);   
        case COLOR_HIGHLIGHTED_FOREGROUND: return ApplicationManager.manager.getIntProperty("item.focus.foreground", 0);   
        case COLOR_BORDER: return ApplicationManager.manager.getIntProperty("item.border", 0x0ffffff);
        case COLOR_HIGHLIGHTED_BORDER: return ApplicationManager.manager.getIntProperty("item.focus.border", 0x08888ff);
        }
        return 0x08888ff;
    }

    
	/**
	 * Returns the best image width for a given image type. The image type must be one 
	 * of LIST_ELEMENT, CHOICE_GROUP_ELEMENT, or ALERT.
	 * 
	 * @param imageType the image type
	 * @return the best image width for the image type, may be zero if there is 
	 *         no best size; must not be negative
	 * @throws IllegalArgumentException - if imageType is illegal
	 * 
	 * @API MIDP-2.0
	 * @ME4SE UNSUPPORTED
	 */
	public int getBestImageWidth(int imageType) {
		return 0;
	}
	
	/**
	 * Returns the best image height for a given image type. The image type 
	 * must be one of LIST_ELEMENT, CHOICE_GROUP_ELEMENT, or ALERT.
	 * 
	 * @param imageType the image type
	 * @return the best image height for the image type, may be zero if there 
	 *         is no best size; must not be negative
	 * @throws IllegalArgumentException - if imageType is illegal
	 * 
	 * @API MIDP-2.0
	 * @ME4SE UNSUPPORTED
	 */
	public int getBestImageHeight(int imageType) {
		System.out.println("Display.getBestImageHeight() with no effect!");
		return 0;
	}

	/**
	 * @ME4SE INTERNAL
	 */
	protected static void check() {
		if (ApplicationManager.manager.frame != null && !ApplicationManager.manager.frame.isVisible()) {
			java.awt.Frame frame = ApplicationManager.manager.frame;
			frame.pack();
			frame.show();
			frame.setResizable(true);
		}
	}

}
