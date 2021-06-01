import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

class ContiguousAllocator implements Allocator, Serializable {
    private HashMap<String, ArrayList<Integer>> map; // path, <start,size>
    String blocks;

    public void setBlocks(String blocks) {
        this.blocks = blocks;
    }

    public String getBlocks() {
        return this.blocks;
    }

    ContiguousAllocator() {
        blocks = new String();
        map = new HashMap<String, ArrayList<Integer>>();
    }

    @Override
    public boolean allocate(String path, int fileSize) {
        int max = 0;
        int idx = 0;
        int cnt = 0;
        for (int i = 0; i < blocks.length(); i++) {
            if (blocks.charAt(i) == '1') {
                if (cnt > max) {
                    idx = i - cnt;
                    max = cnt;
                }
                cnt = 0;
            } else
                cnt++;
        }
        if (cnt > max) {
            idx = blocks.length() - cnt;
            max = cnt;
        }
        if (max < fileSize) {
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder(blocks);
        for (int i = idx; i < idx + fileSize; i++) {
            stringBuilder.setCharAt(i, '1');
        }

        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(idx);
        arrayList.add(fileSize);
        this.map.put(path, arrayList);
        blocks = stringBuilder.toString();
        return true;
    }

    @Override
    public boolean deAllocate(String path) {
        if (!map.containsKey(path))
            return false;
        ArrayList<Integer> pair = map.get(path);
        StringBuilder stringBuilder = new StringBuilder(blocks);
        for (int i = pair.get(0); i < pair.get(0) + pair.get(1); i++) {
            stringBuilder.setCharAt(i, '0');
        }
        blocks = stringBuilder.toString();
        map.remove(path);
        return true;
    }

}
