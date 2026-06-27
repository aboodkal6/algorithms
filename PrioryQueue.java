import java.util.ArrayList;
import java.util.List;

public class PrioryQueue {
    private List<WaitListEntry> heap;

    public PrioryQueue(){
        this.heap = new ArrayList<>();
    }

    public void enqueue(WaitListEntry entry){
        heap.add(entry);
        heapifyUP(heap.size()-1);
    }

    public WaitListEntry dequeue(){
        if(heap.isEmpty())
            return null;
        WaitListEntry top = heap.get(0);
        heap.set(0, heap.get(heap.size()-1));
        heap.remove(heap.size()-1);
        if(!heap.isEmpty())
            heapifyDown(0);
        return top;
    }

    public WaitListEntry peek(){
        if(heap.isEmpty())
            return null;
        return heap.get(0);
    }

    public void remove(String borrowerUserName){
        int indexToRemove = -1;
        for(int i = 0; i < heap.size(); i++){
            if(heap.get(i).getBorrower().getBorrowerUserName().equals(borrowerUserName)){
                indexToRemove = i;
                break;
            }
        }
        if(indexToRemove == -1) return;
        heap.set(indexToRemove, heap.get(heap.size()-1));
        heap.remove(heap.size()-1);
        if(indexToRemove < heap.size())
            heapifyDown(indexToRemove);
    }

    public boolean isEmpty(){
        return heap.isEmpty();
    }

    public int size(){
        return heap.size();
    }

    private void heapifyUP(int index){
        if(index == 0) return;
        int parentIndex = (index - 1) / 2;
        if(heap.get(index).comperTo(heap.get(parentIndex)) < 0){
            swap(index, parentIndex);
            heapifyUP(parentIndex);
        }
    }

    private void heapifyDown(int index){
        int leftChildIndex = index * 2 + 1;
        int rightChildIndex = index * 2 + 2;
        int smallest = index;

        if(leftChildIndex < heap.size() && heap.get(leftChildIndex).comperTo(heap.get(smallest)) < 0)
            smallest = leftChildIndex;

        if(rightChildIndex < heap.size() && heap.get(rightChildIndex).comperTo(heap.get(smallest)) < 0)
            smallest = rightChildIndex;

        if(smallest != index){
            swap(index, smallest);
            heapifyDown(smallest);
        }
    }

    public void swap(int i, int j){
        WaitListEntry waitListEntry = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, waitListEntry);
    }
}
