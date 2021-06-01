import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

class IndexedAllocator implements Allocator, Serializable {
    HashMap<Integer, ArrayList<Integer>> indexToList;
    HashMap<String, Integer> pathToIndex;
    String blocks;

    public void setBlocks(String blocks) {
        this.blocks = blocks;
    }

    public String getBlocks() {
        return this.blocks;
    }

    IndexedAllocator() {
        blocks = new String();
        pathToIndex = new HashMap<String, Integer>();
        indexToList = new HashMap<Integer, ArrayList<Integer>>();
    }


    /*
        block -> ArrayList
     */
    @Override
    public boolean allocate(String path, int fileSize) {
        // fileSize + 1
        int emptyBlocks = 0;
        for (int i = 0; i < blocks.length(); i++) {
            if (blocks.charAt(i) == '0')
                emptyBlocks++;
        }
        if (emptyBlocks < fileSize + 1)
            return false;

        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        int index = -1;
        StringBuilder stringBuilder = new StringBuilder(blocks);
        for (int i = 0; i < stringBuilder.length(); i++) {
            if (stringBuilder.charAt(i) == '0') {
                if (index == -1)
                    index = i;
                else
                    arrayList.add(i);

                stringBuilder.setCharAt(i, '1');
            }
            if (arrayList.size() == fileSize)
                break;
        }
        indexToList.put(index, arrayList);
        pathToIndex.put(path, index);
        blocks = stringBuilder.toString();
        return true;
    }

    @Override
    public boolean deAllocate(String path) {
        if (!pathToIndex.containsKey(path))
            return false;
        int index = pathToIndex.get(path);
        ArrayList<Integer> arrayList = indexToList.get(index);
        StringBuilder stringBuilder = new StringBuilder(blocks);
        stringBuilder.setCharAt(index, '0');
        for (int i = 0; i < arrayList.size(); i++) {
            stringBuilder.setCharAt(arrayList.get(i), '0');
        }
        blocks = stringBuilder.toString();
        pathToIndex.remove(path);
        indexToList.remove(index);
        return true;
    }
}