import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Scanner;

public class Main {
    static String saveDataPath;
    static Scanner in = new Scanner(System.in);

    private static VFS load() {
        FileInputStream fileIn = null;
        ObjectInputStream objectIn = null;
        try {
            fileIn = new FileInputStream(saveDataPath);

            objectIn = new ObjectInputStream(fileIn);
            return (VFS) objectIn.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println("--------------------------Virtual File System------------------------------");
        System.out.println("Enter file path");
        saveDataPath = in.nextLine();
        int n = 0, type = 0;
        VFS vfs = null;
        System.out.println("Do you want to load? Y\\N");
        if (in.nextLine().equalsIgnoreCase("y"))
            vfs = load();
        else {
            System.out.println("Enter number of blocks");
            n = in.nextInt();
            System.out.println("Enter type of allocator: ");
            type = in.nextInt();
            in.nextLine();
        }
        while (true) {

            System.out.println("Enter a command: ");
            String command = in.nextLine();
            if (vfs == null)
                vfs = new VFS(n, type, saveDataPath);
            if (!vfs.execCommand(command)) {
                System.out.println("Command execution failed!");
            } else
                System.out.println("Executed");
        }

    }
}
/*
C:\Users\Mo\Desktop\Edu\Year3_Second_Term\Advanced OS\Virtual_File_System\vfs.txt

CreateFile roots/file2.txt 2
CreateFolder root/folder1/Folder2
deleteFile root/folder1/Folder2/sss.txt 2
DisplayDiskStatus
DisplayDiskStructure
 */