/* ===========================================================================
 * JadFile.java
 * ===========================================================================
 *
 * This file copyright (c) 2002 Joerg Pleumann (joerg@pleumann.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.me4se.impl;

import java.io.*;
import java.util.*;

import javax.microedition.midlet.ApplicationManager;

/**
 * A simple class to access the contents of a JAD file. The JAD file is held in
 * memory for easy access and has to be written back to disk later for the
 * changes to take effect. Actually this class contains a lot more methods than
 * needed for JAD management, but I didn't have the time to strip it down. :-)
 * The really important ones are load(), save(), getValue() and setValue().
 *
 * @author Joerg Pleumann (joerg@pleumann.de)
 */
public class JadFile {

	/**
	 * Inner class that represents the definition of a single MIDlet.
	 */
	public class MIDletData {
		/**
		 * The MIDlet's number.
		 */
		private int number;

		/**
		 * The MIDlet's name.
		 */
		private String name;


		/**
		 * The MIDlet's icon.
		 */
		private String icon;

        /**
         * The MIDlet's main class.
         */
        private String cls;

		/**
		 * Creates a new instance of the inner class.
		 */
		private MIDletData(int number, String name, String icon, String cls) {
			this.number = number;
			this.name = name;
            this.icon = icon;
			if (cls != null) 
                this.cls = cls; //.replace('/', '.');
		}

		/**
		 * Returns the MIDlet's number.
		 */
		public int getNumber() {
			return number;
		}

		/**
		 * Returns the MIDlet's name, or null, if the MIDlet doesn't have a name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the MIDlet's main class, or null, if the MIDlet doesn't
		 * have a name (which would be an error in the JAR file, of course.
		 */
		public String getClassName() {
			return cls;
		}

		/**
		 * Returns the MIDlet's icon, or null, if the MIDlet doesn't have an
		 * icon.
		 */
		public String getIcon() {
			return icon;
		}
	}

	/**
	 * Holds the lines of the JAD file.
	 */
	private Vector strings = new Vector();

	/**
	 * Adds a line to the JAD file.
	 */
	public int add(String s) {
		int result = size();
		insert(result, s);
		return result;
	}

	/**
	 * Clear the JAD file.
	 */
	public void clear() {
		strings = new Vector();
	}

	/**
	 * Deletes a line from the JAD file.
	 */
	public void delete(int index) {
		strings.removeElementAt(index);
	}

	/**
	 * Returns a line from the JAD file.
	 */
	public String get(int index) {
		return (String) strings.elementAt(index);
	}

	/**
	 * Gets the key stored in the given line (everything before the first ':'),
	 * or null if the line doesn't contain a key.
	 */
	public String getName(int i) {
		String result = get(i);
		int p = result.indexOf(':');
		if (p != -1) {
			result = result.substring(0, p).trim();
		}
		else
			result = null;

		return result;
	}

	/**
	 * Gets the value belonging to the given key, or null if the key is not
	 * found.
	 */
	public String getValue(String name) {
		int i = indexOfName(name);
		if (i != -1) {
			String result = get(i);
			i = result.indexOf(':');
			result = result.substring(i + 1);
			return ApplicationManager.decodeJavaEscape(result.trim());
		}
		else {
			return null;
		}
	}

	/**
	 * Finds a whole line in a JAD file. Returns either the index at which the
	 * line was found, or -1 if the line doesn't exist.
	 */
	public int indexOf(String s) {
		String t = s.toLowerCase();
		for (int i = 0; i < size(); i++) {
			if (get(i).toLowerCase().equals(t)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Finds a key in a JAD file. Returns either the index at which the
	 * key was found, or -1 if the line doesn't exist.
	 */
	public int indexOfName(String name) {
		String s = name.toLowerCase() + ':';
		int i = 0;
		while (i < size()) {
			if (get(i).toLowerCase().startsWith(s)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * Inserts a line into the JAD file.
	 */
	public void insert(int index, String s) {
		strings.insertElementAt(s, index);
	}

	/**
	 * Replaces a line in the JAD file.
	 */
	public void set(int index, String s) {
		strings.setElementAt(s, index);
	}

	/**
	 * Sets the value of the given key, replacing a previous definition if one
	 * exists.
	 */
	public void setValue(String name, String value) {
		int i = indexOfName(name);
		if (i == -1) {
			if ((value != null) && (!value.equals(""))) {
				add(name + ": " + value);
			}
		}
		else {
			if ((value != null) && (!value.equals(""))) {
				set(i, name + ": " + value);
			}
			else {
				delete(i);
			}
		}
	}

	/**
	 * Returns the number of lines in the JAD file.
	 */
	public int size() {
		return strings.size();
	}

	/**
	 * Returns the number of MIDlet's in the JAD file.
	 */
	public int getMIDletCount() {
		int i = 1;

		while (indexOfName("MIDlet-" + i) != -1) {
			i++;
		}

		return i - 1;
	}

	/**
	 * Returns the definition of the given MIDlet, or null, if the MIDlet
	 * doesn't exist. Note that MIDlet numbering starts at 1.
	 */
	public MIDletData getMIDlet(int i) {
		String value = getValue("MIDlet-" + i);

		if (value == null)
			return null;

		int p1 = value.indexOf(',');

		String name = null;
        String icon = null;
		String cls = null;

		if (p1 != -1) {
			name = value.substring(0, p1).trim();
 
            int p2 = value.indexOf(',', p1 + 1);

			if (p2 != -1) {
                icon = value.substring(p1 + 1, p2).trim();
				cls = value.substring(p2 + 1).trim();
			}
			else {
				icon = value.substring(p1 + 1).trim();
			}
		}
		else {
			name = value.trim();
		}

        //System.out.println ("name: "+name+" icon: "+icon+ " cls: "+cls);

        if ("".equals(name)) name = null;
        if ("".equals(cls)) cls = null;
        if ("".equals(icon)) icon = null;

		return new MIDletData(i, name, icon, cls);
	}

	

    public void load(InputStream is) throws IOException {
    	clear();
        BufferedReader reader = new BufferedReader (new InputStreamReader (is));
        String s = reader.readLine();
        while (s != null) {
            if (s.startsWith(" ")) {
                set(size() - 1, get(size() - 1) + s.trim());
            }
            else {
                add(s);
            }
            s = reader.readLine();
        }

        reader.close();        
    }


    
    
	/**
	 * Loads the JAD file from a physical disk file.
	 */
	public void load(String filename) throws IOException {
        load (new FileInputStream(filename));
	}

	/**
	 * Save the JAD file to a physical disk file.
	 */
	public void save(String filename) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename)));

		for (int i = 0; i < size(); i++) {
			writer.write(get(i));
			writer.newLine();
		}

		writer.close();
	}

    /*
	public static void main(String[] args) {
	    try {
	        JadFile2 jad = new JadFile2();
	        jad.load(args[0]);
	        
	        int c = jad.getMIDletCount();
	        for (int i = 1; i <= c; i++) {
	            MIDletData m = jad.getMIDlet(i);
	            
	            System.out.println(m.getNumber() +  " " + m.getName() + " " + m.getClassName() + " " + m.getIcon());
	        }
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	}
    */
}