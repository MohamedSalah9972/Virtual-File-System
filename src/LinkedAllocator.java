import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

class LinkedAllocator implements Allocator, Serializable {
    HashMap<String, LinkedList<Integer>> pathToList;
    String blocks;

    public void setBlocks(String blocks) {
        this.blocks = blocks;
    }

    public String getBlocks() {
        return this.blocks;
    }

    LinkedAllocator() {
        blocks = new String();
        pathToList = new HashMap<String, LinkedList<Integer>>();
    }

    @Override
    public boolean allocate(String path, int fileSize) {
        int emptyBlocks = 0;
        for (int i = 0; i < blocks.length(); i++) {
            if (blocks.charAt(i) == '0')
                emptyBlocks++;
        }
        if (emptyBlocks < fileSize)
            return false;

        LinkedList<Integer> linkedList = new LinkedList<Integer>();
        StringBuilder stringBuilder = new StringBuilder(blocks);
        for (int i = 0; i < stringBuilder.length(); i++) {
            if (stringBuilder.charAt(i) == '0') {
                linkedList.addLast(i);
                stringBuilder.setCharAt(i, '1');
            }
            if (linkedList.size() == fileSize) {
                break;
            }
        }

        pathToList.put(path, linkedList);
        blocks = stringBuilder.toString();
        return true;
    }

    @Override
    public boolean deAllocate(String path) {
        LinkedList<Integer> list = pathToList.get(path);
        StringBuilder stringBuilder = new StringBuilder(blocks);
        while (list.size() > 0) {
            stringBuilder.setCharAt(list.getFirst(), '0');
            list.removeFirst();
        }
        blocks = stringBuilder.toString();
        pathToList.remove(path);
        return true;
    }


}
