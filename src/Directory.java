import java.io.Serializable;
import java.util.ArrayList;

public class Directory implements Serializable {
    protected ArrayList<File> files;
    protected String directoryPath;
    protected ArrayList<Directory> directories;
    protected String name;

    Directory() {
        files = new ArrayList<File>();
        directories = new ArrayList<Directory>();
    }
    /*

     */

    Directory(String name) {
        files = new ArrayList<File>();
        directories = new ArrayList<Directory>();
        this.name = name;
    }
}