package flooferland.showbiz.showformat;

import flooferland.showbiz.showformat.data.ShowData;
import flooferland.showbiz.showformat.formats.RshowFormat;
import flooferland.showbiz.showformat.formats.ZshowFormat;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

// TODO: Add more error handling for the formats: Check if the version of the file matches (and exists), etc

class LibraryTest {
    // private static final String IntermediatePath = "C:/Users/FlooferLand/Desktop/looney.bin";
    private static final String RshowPath = "D:/Creative Engineering/Show tapes/Welcome in to Showbiz.rshw";
    private static final String zPath = "C:/Users/FlooferLand/Desktop/looney.zshw";
    
    @Test void test() {
        System.out.println();
        ShowData showData = null;

        // Reading RSHW
        System.out.println("-- reading RR files");
        RshowFormat rshow = new RshowFormat();
        try {
            InputStream stream = getFile(RshowPath);
            showData = rshow.readFromStream(stream);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(showData != null, "Rshow data is null");
        
        // Writing native files
        System.out.println("-- writing native files");
        ZshowFormat zshow = new ZshowFormat();
        try (InputStream zStream = zshow.writeToStream(showData)) {
            // Reading native files
            System.out.println("-- reading native files");
            try {
                /*InputStream binStream = getFile(IntermediatePath);
                new BinShowFormat().readFromStream(binStream);
                binStream.close();*/
                //InputStream zStream = getFile(zPath);
                new ZshowFormat().readFromStream(zStream);
                zStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
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
