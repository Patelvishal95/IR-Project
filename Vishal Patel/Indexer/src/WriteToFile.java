
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WriteToFile {
private File filetowrite;    
private BufferedWriter b = null;
    public WriteToFile(File filetowrite) {
        
    try {
        this.filetowrite = filetowrite;
        
        b = new BufferedWriter(new FileWriter(filetowrite));
    } catch (FileNotFoundException ex) {
        Logger.getLogger(WriteToFile.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
        Logger.getLogger(WriteToFile.class.getName()).log(Level.SEVERE, null, ex);
    } 
    }
    public void write(String write){
    try {
        b.write(write);
        b.flush();
    } catch (IOException ex) {
        Logger.getLogger(WriteToFile.class.getName()).log(Level.SEVERE, null, ex);
    }
    }
    public void close(){
    try {
        b.close();
    } catch (IOException ex) {
        Logger.getLogger(WriteToFile.class.getName()).log(Level.SEVERE, null, ex);
    }
        b=null;
    }
}
