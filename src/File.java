import java.io.Serializable;

public class File implements Serializable {
    File(String path, String name, int size) {
        this.name = name;
        this.path = path;
        this.size = size;
    }

    protected String name;
    protected String path;
    protected int size;
}
