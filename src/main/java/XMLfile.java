
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class XMLfile {

    private String xml;
    private ArrayList<String> slicedXML;
    private boolean valid = false;

    XMLfile(File file) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            xml = "";
            while (line != null) {
                if (xml.equals("")) {
                    xml = line;
                } else {
                    xml = xml + "\n" + line;
                }
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    XMLfile(String s) {
        this.xml = s;
    }

    void sliceXML() {
        if (!valid) {
            return;
        }
        
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        XMLfile xml = new XMLfile(new File("sample.xml"));
        System.out.println(xml.xml);
    }
}
