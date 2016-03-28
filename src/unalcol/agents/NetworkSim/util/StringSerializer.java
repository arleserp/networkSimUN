/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unalcol.agents.NetworkSim.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import edu.uci.ics.jung.graph.Graph;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import unalcol.agents.NetworkSim.SyncronizationMain;
import static unalcol.agents.NetworkSim.SyncronizationMain.graphMode;

/**
 *
 * @author Arles Rodriguez
 */
public class StringSerializer {

    public static String serialize(Object h) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        String s = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(h);
            s = baos.toString("ISO-8859-1");
            oos.reset();
            oos.close();
            oos=null;
            baos=null;            
        } catch (IOException ex) {
            Logger.getLogger(StringSerializer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }

    public static Object deserialize(String s) {
        Object h = null;
        try {
            ObjectInputStream ois;
            try (ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes("ISO-8859-1"))) {
                ois = new ObjectInputStream(bais);
                h = (Object) ois.readObject();
            }
            ois.close();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(StringSerializer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return h;
    }

      public static void saveSerializedObject(String filename, Object g) {
        try {
            OutputStream file = new FileOutputStream(filename);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutputStream oos;
            try {
                oos = new ObjectOutputStream(buffer);
                oos.writeObject(g);
                oos.close();
            } catch (IOException ex) {
                Logger.getLogger(HashtableOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphSerialization.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Object loadDeserializeObject(String filename) {
        InputStream file;
        Object h = null;
        System.out.println("file" + filename);
        try {
            file = new FileInputStream(filename);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            h = input.readObject();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphSerialization.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GraphSerialization.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GraphSerialization.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("h" + h.toString());
        return h;
    }
    
}