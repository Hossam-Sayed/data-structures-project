
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
        boolean inValue = false;
        String currentLeafTag = null;
        slicedXML = new ArrayList<String>();
        char[] xmlchars = xml.toCharArray();
        String s = "";
        for (int i = 0; i < xmlchars.length; i++) {
            if (xmlchars[i] == '\n') { //slice when you find a new line (made to terminate data)
                inValue = false;
                slicedXML.add(s);
                s = "";
            } else if (!inValue && xmlchars[i] == '>') { //slice when you find an ending of a tag
                s += xmlchars[i];
                slicedXML.add(s);
                s = "";
                //skip white spaces after the tag
                while (i + 1 < xmlchars.length
                        && (xmlchars[i + 1] == '\n' || xmlchars[i + 1] == ' ' || xmlchars[i + 1] == '\t')) {
                    i++;
                }
                //check whether next String is value or tag
                if (i + 1 < xmlchars.length && xmlchars[i + 1] != '<') {
                    currentLeafTag = slicedXML.get(slicedXML.size() - 1);
                    inValue = true;
                }
            } else if (!inValue && (xmlchars[i] == ' ' || xmlchars[i] == '\t')) {//Skip white spaces outside values
            } else if (inValue && xmlchars[i + 1] == '<') { //slice before the closing tag
                s += xmlchars[i];
                slicedXML.add(s);
                s = "";
                inValue = false;
            } //these commented else if replaces the above else if, if the XML attribute value can contain <,>.
            /*else if (i + 1 < xmlchars.length
                    && inValue && xmlchars[i] == '<' && xmlchars[i + 1] == '/') {//check if char '<' is inside the data or start of the closing tag
                int j = 2; //2 to skip "</" and start checking the tagname
                //currentLeafTag.charAt(j - 1) the -1 because it doesn't contain '/'
                while (j - 1 < currentLeafTag.length() && currentLeafTag.charAt(j - 1) == xmlchars[i + j]) {
                    j++;
                }
                //if we reached the end if LeafTag then the two tags matches, check for '>' char
                if (j - 1 == currentLeafTag.length()) {
                    slicedXML.add(s);
                    s = "";
                    inValue = false;
                    //decrement i to avoid skipping the '<'
                    i--;
                }else{ //Normal case : add char to the slice string, '<' is inside the data not a start of the tag
                    s += xmlchars[i];
                }
            }*/ else { //normal case: add char to the slice string,
                s += xmlchars[i];
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        XMLfile xml = new XMLfile(new File("sample.xml"));
        xml.sliceXML();
        for (String s : xml.slicedXML) {
            System.out.println(s);
        }
    }
}
