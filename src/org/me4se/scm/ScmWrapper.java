/* Copyright (c) 2002,2003,2004 Stefan Haustein, Oberhausen, Rhld., Germany
 */


package org.me4se.scm;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.microedition.midlet.ApplicationManager;

import org.me4se.impl.Log;

import java.text.*;
import java.awt.font.TextHitInfo;

/** A buffered wrapper for including scm components in 
    an AWT container */

public class ScmWrapper
    extends Canvas
    implements MouseMotionListener, MouseListener, ComponentListener, KeyListener, InputMethodListener, java.awt.im.InputMethodRequests {
    
    public ScmComponent component;
    boolean invalid;

    //@MH
    int pressedX = -1;
    int pressedY = -1;
    float scale = 1;
    
    /** Currently pressed key, used for keyRepeated events */
    String pressing;  

    public int paintCount;
    
    // static only for test!!!
    public static BufferedImage offScreenCache;
//    Graphics offScreenGraphics;
    
    class Helper extends ScmContainer {
        public void repaint(int x, int y, int w, int h) {
            Log.log(Log.DRAW_EVENTS, "sending repaint request to AWT: " + x + "," + y + "," + w + "," + h);
            ScmWrapper.this.repaint(
                (int) (x * scale),
                (int) (y * scale),
                (int) (w * scale),
                (int) (h * scale));
            Log.log(Log.DRAW_EVENTS, "repaint request sent to AWT");

        }

        public Graphics getGraphics() {
            return getOffScreen().getGraphics();
        }

        public void invalidate() {
            if (!invalid) {
                //   System.out.println("repaint scheduled!");
                invalid = true;
                // coordinates neccessary, otherwise macos assumes old site :-(
                ScmWrapper.this.repaint(
                    0,
                    0,
                    ScmWrapper.this.getSize().width,
                    ScmWrapper.this.getSize().height);
            }
        }
    }

    Helper helper = new Helper();
    //Image offScreen;

    ApplicationManager manager;
    
    public ScmWrapper(ApplicationManager manager) {
        this(manager, 1);
    }

    public ScmWrapper(ApplicationManager manager, float scale) {
        this.scale = scale;
        this.manager = manager;
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
        addKeyListener(this);
        addInputMethodListener(this);
/*        try {
               Toolkit toolkit = Toolkit.getDefaultToolkit();
                        boolean shouldEnable = false;
                        // 验证当前环境是否支持输入法调用
                        if (toolkit instanceof InputMethodSupport) {
                                shouldEnable = ((InputMethodSupport) toolkit)
                                                .enableInputMethodsForTextComponent();
                        }
                        enableInputMethods(shouldEnable);
                } catch (Exception e) {
                }*/
         enableInputMethods(true);
    }

    public void componentResized(ComponentEvent ev) {
        invalid = false;
        Dimension d = getSize();
        component.setBounds(
            0,
            0,
            (int) (d.width / scale),
            (int) (d.height / scale));
        /*  System.out.println(
              "resized: "
                  + d.width
                  + ", "
                  + d.height
                  + " invalid: "
                  + invalid); */

    }

    public void componentMoved(ComponentEvent ev) {
    }

    public void componentHidden(ComponentEvent ev) {
    }

    public void componentShown(ComponentEvent ev) {
    }

    public void setComponent(ScmComponent component) {
        if (this.component != null)
            helper.remove(this.component);

        if (component.parent != null)
            throw new RuntimeException("component already assigned");

        this.component = component;
        helper.add(component);
        repaint();
    }

    public void update(Graphics g) {
        paint(g);
    }
    
    public BufferedImage getOffScreen(){
		Dimension size = getSize();
		if (offScreenCache == null
			|| offScreenCache.getWidth(this) != size.width
			|| offScreenCache.getHeight(this) != size.height) {

            offScreenCache =
                new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
/*                 createImage(
                        (int) (size.width / scale),
                        (int) (size.height / scale));*/
   		}
		return offScreenCache;
    }
    

    public void paint(java.awt.Graphics g) {
        Log.log(Log.DRAW_EVENTS, "AWT paint entered: "+g.getClipRect());
        if (invalid) {
            component.doLayout();
            invalid = false;
        }

        Dimension size = getSize();
        if (size.width <= 0 || size.height <= 0)
            return;

		BufferedImage offScreen = getOffScreen();
        

        
        Graphics offScreenGraphics = offScreen.getGraphics();
        
        offScreenGraphics.setClip(0, 0, offScreenCache.getWidth(null), offScreenCache.getHeight(null));
        offScreenGraphics.setColor(Color.BLACK);
        
        helper.paint(offScreenGraphics);


        
        paintCount++;

/*        offScreenGraphics.setColor(Color.BLUE);
        
        for(int i = 15; i < size.width; i += 30){
            offScreenGraphics.drawLine((paintCount+i) % size.width, 0, (paintCount+i) % size.width, size.height);
        }*/
 
      
        int orig = offScreen.getRGB(0, 0);
        offScreen.setRGB(0, 0, orig ^ 0x0ffffff);        
        if(offScreen.getRGB(0, 0) == orig){
            System.out.println("***** Cannot draw to offscreen (Press F12 to reallocate) ****");
            // reallocate here!
        }
        
        if(!g.drawImage(
            offScreen,
            0,
            0,
            size.width,
            size.height,
            0,
            0,
            (int) (size.width / scale),
            (int) (size.height / scale),
            this)){
            System.out.println("DRAWIMAGE WAS RETURNING FALSE!!!");
        }
        //		g.drawImage(offScreen, 0, 0, this);

     /*   g.setColor(Color.RED);
        
        for(int i = 0; i < size.width; i += 30){
            g.drawLine((paintCount+i) % size.width, 0, (paintCount+i) % size.width, size.height);
        }*/

        
        Log.log(Log.DRAW_EVENTS, "AWT paint left");
    }

    public void mouseDragged(MouseEvent ev) {
        component.mouseDragged(
            (int) (ev.getX() / scale),
            (int) (ev.getY() / scale),
            ev.getModifiers());
    }

    int getMouseButton(InputEvent ev) {

        int modifiers = ev.getModifiers();
        if ((modifiers & InputEvent.BUTTON1_MASK) != 0)
            return 1;
        if ((modifiers & InputEvent.BUTTON2_MASK) != 0)
            return 2;
        if ((modifiers & InputEvent.BUTTON3_MASK) != 0)
            return 3;

        return 0;
    }

    public void mouseMoved(MouseEvent ev) {
        component.mouseMoved(
            (int) (ev.getX() / scale),
            (int) (ev.getY() / scale),
            ev.getModifiers());
    }

    //	public void mousePressed(MouseEvent ev) {
    //		component.mousePressed(getMouseButton(ev), (int)(ev.getX()/scale), (int)(ev.getY()/scale), ev.getModifiers());
    //	}


    public void mousePressed(MouseEvent ev) {
        component.mousePressed(
            getMouseButton(ev),
            (int) (ev.getX() / scale),
            (int) (ev.getY() / scale),
            ev.getModifiers());
        pressedX = ev.getX();
        pressedY = ev.getY();
    }

    public void mouseReleased(MouseEvent ev) {
        component.mouseReleased(
            getMouseButton(ev),
            (int) (ev.getX() / scale),
            (int) (ev.getY() / scale),
            ev.getModifiers());
        //@MH
        int maxdelta = 6;
        if ((pressedX != -1) && (pressedY != -1)) {
            if (((Math.abs(pressedX - ev.getX())) < maxdelta)
                && (((Math.abs(pressedX - ev.getX())) > 0)
                    || (((Math.abs(pressedY - ev.getY())) > 0)))
                && ((Math.abs(pressedY - ev.getY())) < maxdelta)) {
                mouseClicked(ev);
            }
        }
        pressedX = -1;
        pressedY = -1;
        //@MH             

    }
    public void mouseClicked(MouseEvent ev) {
        component.mouseClicked(
            getMouseButton(ev),
            (int) (ev.getX() / scale),
            (int) (ev.getY() / scale),
            ev.getModifiers(),
            ev.getClickCount());
    }

    public java.awt.Dimension getPreferredSize() {
        return getMinimumSize();
    }

    public void mouseExited(MouseEvent ev) {
        //System.out.println ("mouse exited!");
        mouseMoved(ev);
    }

    public void keyPressed(KeyEvent ev) {
    		String name = manager.getButtonName(ev);
        if(name != null) {
        		if(name.equals(pressing)){
        			component.keyRepeated(name);
        		}
        		else {
        			pressing = name;
        			component.keyPressed(name);
        		}
        }
    }

    public void keyReleased(KeyEvent ev) {
    		pressing = null;
    	String name = manager.getButtonName(ev);
        if(name != null) {
        	component.keyReleased(name);
        }
    }

    public void keyTyped(KeyEvent ev) {
        //component.keyTyped(ev.getKeyChar());
    }

    public void mouseEntered(MouseEvent ev) {
    }

    public java.awt.Dimension getMinimumSize() {
        Dimension d = component.getMinimumSize();
        d.width = (int) (d.width * scale);
        d.height = (int) (d.height * scale);
        return d;
    }

    public javax.microedition.lcdui.ScmTextComponent getTextComponent(ScmComponent comp)
    {
        if(comp instanceof javax.microedition.lcdui.ScmTextComponent)
            return (javax.microedition.lcdui.ScmTextComponent)comp;
        if(comp instanceof ScmContainer)
        {
            ScmContainer cont = (ScmContainer) comp;
            for(int i = 0;i<cont.childCount;i++)
            {
                javax.microedition.lcdui.ScmTextComponent ret = getTextComponent(cont.children[i]);
                if(ret != null)return ret;
            }
        }
        return null;
    }

    public void caretPositionChanged(InputMethodEvent event) {
        javax.microedition.lcdui.ScmTextComponent textComp = getTextComponent(component);
        if(textComp != null)
        {
            textComp.setCharPos(event.getCaret().getCharIndex());
        }
    }

    public void inputMethodTextChanged(InputMethodEvent event) {
        javax.microedition.lcdui.ScmTextComponent textComp = getTextComponent(component);
        if(textComp != null)
        {
            String str = "";
            AttributedCharacterIterator iter = event.getText();
            char curr = iter.current();
            while(curr != java.text.CharacterIterator.DONE)
            {
                str += curr;
                curr = iter.next();
            }
            textComp.setText(textComp.getText().substring(0,textComp.getCharPos()) + str + textComp.getText().substring(textComp.getCharPos()));
            textComp.doLayout();
            textComp.setCharPos(textComp.getCharPos()+str.length());
        }
    }

    public java.awt.im.InputMethodRequests getInputMethodRequests() {
        return this;
    }
    
    public AttributedCharacterIterator 	cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributes)
    {
        return null;
    }
    
    public AttributedCharacterIterator 	getCommittedText(int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes)
    {
        javax.microedition.lcdui.ScmTextComponent textComp = getTextComponent(component);
        if(textComp != null)
        {
            return new java.text.AttributedString(textComp.getText()).getIterator();
        }
        else return new java.text.AttributedString("").getIterator();
    }
    
    public int 	getCommittedTextLength()
    {
        javax.microedition.lcdui.ScmTextComponent textComp = getTextComponent(component);
        if(textComp != null)
        {
        return textComp.getText().length();
        }else return 0;
    }

    public int 	getInsertPositionOffset()
    {
        javax.microedition.lcdui.ScmTextComponent textComp = getTextComponent(component);
        if(textComp != null)
        {
        return textComp.getCharPos();
        }return 0;
    }
    
    public TextHitInfo 	getLocationOffset(int x, int y)
    {
        return null;
    }
    
    public AttributedCharacterIterator 	getSelectedText(AttributedCharacterIterator.Attribute[] attributes)
    {
        return new java.text.AttributedString("").getIterator();
    }
    
    public Rectangle 	getTextLocation(TextHitInfo offset)
    {
        return new Rectangle();
    }
}
