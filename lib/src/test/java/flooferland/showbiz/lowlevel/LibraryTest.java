package flooferland.showbiz.lowlevel;

import flooferland.showbiz.lowlevel.show.ShowData;
import flooferland.showbiz.lowlevel.transformers.RshowFormat;
import flooferland.showbiz.lowlevel.transformers.ZshowFormat;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO: Add more error handling for the formats: Check if the version of the file matches (and exists), etc

class LibraryTest {
    // private static final String IntermediatePath = "C:/Users/FlooferLand/Desktop/looney.bin";
    private static final String inPath = "C:/Users/FlooferLand/Desktop/rshow_compatibility/easy/meow.rshw";
    private static final String outPath = "C:/Users/FlooferLand/Desktop/rshow_compatibility/out.zshw";
    
    @Test void test() {
        System.out.println();
        ShowData showData = null;
        
        // Reading RSHW
        System.out.printf("-- reading rshow from \"%s\"%n", inPath);
        RshowFormat rshow = new RshowFormat();
        try {
            InputStream stream = getFile(inPath);
            showData = rshow.readFromStream(stream).unwrap();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(showData != null, "Rshow data is null");

        // Writing zshow to file
        System.out.printf("-- writing zshow to \"%s\" %n", outPath);
        ZshowFormat zshow = new ZshowFormat();
        try {
            File zshowFile = new File(outPath);
            FileOutputStream stream = new FileOutputStream(zshowFile, false);
            zshow.writeToStream(showData, stream);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Reading zshow from file
        /*System.out.printf("-- reading zshow from \"%s\" %n", outPath);
        try {
            File zshowFile = new File(outPath);
            FileInputStream stream = new FileInputStream(zshowFile);
            zshow.readFromStream(stream);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        
        System.out.println();
    }
    
    FileInputStream getFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + path);
        }
        
        try {
            return new FileInputStream(file);
        } catch (Exception err) {
            throw new RuntimeException(err.toString());
        }
    }
}
