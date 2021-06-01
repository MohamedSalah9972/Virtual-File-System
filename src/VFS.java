
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import java.io.*;





public class VFS implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    int n;
    int emptySpace;
    int allocatedSpace;
    String blocks;
    Allocator allocator;
    String saveDataPath;
    private Directory root;

    VFS(int n, int allocatorType) {
        if (allocatorType == 0) {
            allocator = new ContiguousAllocator();
        } else if (allocatorType == 1) {
            allocator = new LinkedAllocator();
        } else {
            allocator = new IndexedAllocator();
        }

        emptySpace = n;
        allocatedSpace = 0;
        this.n = n;
        blocks = "0".repeat(n);
        allocator.setBlocks(blocks);
        root = new Directory("root");
    }

    VFS(int n, int allocatorType, String saveDataPath) {
        if (allocatorType == 0) {
            allocator = new ContiguousAllocator();
        } else if (allocatorType == 1) {
            allocator = new LinkedAllocator();
        } else {
            allocator = new IndexedAllocator();
        }

        emptySpace = n;
        allocatedSpace = 0;
        this.n = n;
        blocks = "0".repeat(n);
        allocator.setBlocks(blocks);
        root = new Directory("root");
        this.saveDataPath = saveDataPath;
    }





    public boolean execCommand(String command) {
        Scanner scan = new Scanner(command);
        String firstWord = scan.next();
        switch (firstWord.toLowerCase()) {
            case "createfile": {
                String path = scan.next();
                int blocksSize = scan.nextInt();
                return createFileExec(path, blocksSize);
            }
            case "createfolder": {
                String path = scan.next();
                return createFolderExec(path);
            }
            case "deletefile": {
                String path = scan.next();
                return deleteFileExec(path);
            }
            case "deletefolder": {
                String path = scan.next();
                return deleteFolderExec(path);
            }
            case "displaydiskstatus": {
                displayDiskStatus();
                break;
            }
            case "displaydiskstructure": {
                displayDiskStructure();
                break;
            }
            case "exit": {
                exit();
                break;
            }
            default: {
                System.out.println("Invalid command");
                return false;
            }
        }
        return true;
    }

    public boolean createFolderExec(String path) {
        if (checkFolder(path)) {
            System.out.println("This path already exists");
            return false;
        }
        return createFolder(root, fetchPath(path), 0);
    }

    public boolean createFolder(Directory root, String[] path, int i) {
        if (i == path.length - 2) {// my parent
            if (!root.name.equalsIgnoreCase(path[i]))
                return false;
            root.directories.add(new Directory(path[i + 1]));
            return true;
        }
        for (Directory directory : root.directories) {
            if (directory.name.equalsIgnoreCase(path[i + 1])) {
                return createFolder(directory, path, i + 1);
            }
        }
        return false;
    }

    public boolean deleteFileExec(String path) {
        if (!checkFile(path)) {
            System.out.println("Not found");
            return false;
        }
        deleteFile(root, fetchPath(path), 0);

        boolean ret = allocator.deAllocate(path);
        this.blocks = allocator.getBlocks();
        return ret;
    }


    public void deleteFile(Directory root, String[] path, int index) {
        if (path.length - 2 == index) {
            File deletedFile = null;
            for (File file : root.files) {
                if (file.name.equalsIgnoreCase(path[index + 1])) {
                    deletedFile = file;
                    break;
                }
            }
            root.files.remove(deletedFile);
            return;
        }
        for (Directory directory : root.directories) {
            if (directory.name.equalsIgnoreCase(path[index + 1])) {
                deleteFile(directory, path, index + 1);
                return;
            }
        }
    }

    public void updateSpace() {
        this.emptySpace = 0;
        this.allocatedSpace = 0;
        for (int i = 0; i < blocks.length(); i++) {
            if (blocks.charAt(i) == '1')
                ++this.allocatedSpace;
            else
                ++this.emptySpace;
        }
    }

    public boolean deleteFolderExec(String path) {
        if (!checkFolder(path)) {
            System.out.println("Not found");
            return false;
        }
        deleteFolder(root, fetchPath(path), 0, false);
        return true;
    }

    public void deleteFolder(Directory root, String[] path, int i, boolean isFound) {
        if (i == path.length || root == null) {
            return;
        }
        if (i == path.length - 1) {
            isFound = true;
        }
        if (isFound) {
            for (File file : root.files) {
                allocator.deAllocate(file.path);
                this.blocks = allocator.getBlocks();
            }
        }
        if (!isFound)
            for (Directory directory : root.directories) {
                if (directory.name.equalsIgnoreCase(path[i + 1])) {
                    deleteFolder(directory, path, i + 1, isFound);
                }
            }
        else {
            for (Directory directory : root.directories) {
                deleteFolder(directory, path, i, isFound);
            }
        }

        if (i == path.length - 2) { // parent
            for (Directory directory : root.directories) {
                if (directory.name.equalsIgnoreCase(path[i + 1])) {
                    root.directories.remove(directory);
                    return;
                }
            }
        }
    }

    public boolean createFileExec(String path, int fileSize) {
        if (checkFile(path)) {
            System.out.println("This file already exists");
            return false;
        }
        if (allocator.allocate(path, fileSize)) {
            this.blocks = allocator.getBlocks();
            boolean ret = createFile(root, fetchPath(path), 0, fileSize, path);
            if (!ret) {
                allocator.deAllocate(path);
                this.blocks = allocator.getBlocks();
                System.out.println("Allocation failed");
                return false;
            }
            System.out.println("Allocated successfully");
            return true;
        } else {
            System.out.println("Allocation failed");
            return false;
        }
    }

    private boolean createFile(Directory root, String[] path, int i, int size, String srcPath) {
        if (!path[i].equalsIgnoreCase(root.name))
            return false;
        if (path.length - 2 == i) {
            File newFile = new File(srcPath, path[i + 1], size);
            root.files.add(newFile);
            return true;
        }

        for (Directory directory : root.directories) {
            if (directory.name.equalsIgnoreCase(path[i + 1])) {
                createFile(directory, path, i + 1, size, srcPath);
                return true;
            }
        }
        return false;
    }

    public boolean checkFolder(String path) {
        return checkFolderPath(root, fetchPath(path), 0);
    }

    public boolean checkFile(String path) {
        return checkFilePath(root, fetchPath(path), 0);
    }

    public String[] fetchPath(String path) {
        return path.split("/");
    }

    public boolean checkFilePath(Directory root, String[] path, int indexPath) {
        if (path.length < 2 || !path[indexPath].equalsIgnoreCase(root.name))
            return false;
        if (indexPath == path.length - 2) {
            for (File file : root.files) {
                if (file.name.equalsIgnoreCase(path[indexPath + 1])) {
                    return true;
                }
            }
            return false;
        } else if (indexPath == path.length)
            return true;

        for (Directory directory : root.directories) {
            if (path[indexPath + 1].equalsIgnoreCase(directory.name)) {
                return checkFilePath(directory, path, indexPath + 1);
            }
        }
        return false;
    }

    public boolean checkFolderPath(Directory root, String[] path, int i) {
        if (path.length == i + 1)
            return path[i].equalsIgnoreCase(root.name);
        if (!path[i].equalsIgnoreCase(root.name))
            return false;
        boolean ret = false;
        for (Directory directory : root.directories) {
            ret |= checkFolderPath(directory, path, i + 1);
        }
        return ret;
    }

    public void displayDiskStructure() {
        displayDiskStructureHelper(root, 0);
    }

    private void displayDiskStructureHelper(Directory root, int level) {
        System.out.println("\t".repeat(level) + root.name);
        for (File file : root.files) {
            System.out.println("\t".repeat(level + 1) + file.name);
        }
        for (Directory directory : root.directories) {
            displayDiskStructureHelper(directory, level + 1);
        }
    }

    public void displayDiskStatus() {
        updateSpace();
        System.out.println("Empty space: " + this.emptySpace);
        System.out.println("Allocated space: " + this.allocatedSpace);
        System.out.println("Blocks state: " + blocks); // including empty blocks and allocated blocks
    }


    private void save() {
        WriteObjectToFile(this);
    }

    public void WriteObjectToFile(Object serObj) {

        try {
            PrintWriter writer = new PrintWriter(saveDataPath);
            writer.print(""); // clear file context
            writer.close();
            FileOutputStream fileOut = new FileOutputStream(saveDataPath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serObj);
            objectOut.close();
            System.out.println("Saved!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void exit() {
        Scanner in = new Scanner(System.in);
        System.out.println("Do you want to exit? Y\\N");
        if (in.next().equalsIgnoreCase("n"))
            return;
        System.out.println("Want to save the current state? Y\\N");
        if (in.next().equalsIgnoreCase("y")) {
            save();
            System.exit(0);
        } else
            System.exit(0);
    }
}
