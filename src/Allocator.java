public interface Allocator {
    public void setBlocks(String blocks);

    public String getBlocks();

    boolean allocate(String path, int fileSize);

    boolean deAllocate(String path);

}