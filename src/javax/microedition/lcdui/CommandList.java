package javax.microedition.lcdui;

import java.util.Vector;

import javax.microedition.midlet.ApplicationManager;

//import javax.microedition.midlet.*;
//import java.awt.*;
//import java.awt.event.*;
//import org.me4se.impl.lcdui.*;

class CommandList extends List implements CommandListener {

    private Displayable owner;
    private Command cancel = new Command("Cancel", Command.CANCEL, 0);
	ApplicationManager manager = ApplicationManager.manager;

    protected CommandList(Displayable d) {

        super("Select", Choice.IMPLICIT);
        
        String t = manager.getProperty("menu.text.title");
        if(t != null)
        	setTitle(t);
        
        owner = d;

        addCommand(cancel);
       

        for (int i = 0; i < d.commandMenuEntries.size(); i++) {
            CmdInfo c = (CmdInfo) d.commandMenuEntries.elementAt(i);
            append(c.command.getLabel(), null);
        }

        setCommandListener(this);
    }

    public void commandAction(Command cmd, Displayable d) {
        //System.out.println ("commandAction received!!!");
        display.setCurrent(owner);
        if (cmd != cancel){
        	CmdInfo ci = (CmdInfo) owner.commandMenuEntries.elementAt(getSelectedIndex());
            owner.container.displayable.handleCommand(ci.command, ci.item);
		}
    }

}
