package deque;

import deque.Deque61B;
import java.util.*;

public class ArrayDeque61B<T> implements Deque61B<T> {
    private T[] items;
    private int size;
    private int nextfirst;
    private int nextlast;

    public ArrayDeque61B(){
        items =(T[]) new Object[8];
        size = 0;
        nextfirst = 0;
        nextlast = 1;
    }

    private void resize(int capacity) {
        T[] temp = (T[])new Object[capacity];
        for(int i=0;i<size;i++){
            temp[i]=items[(i + nextfirst+1 + items.length)%items.length];
        }
        nextfirst=capacity -1;
        nextlast=size;
        items=temp;
    }

    @Override
    public void addFirst(T x) {
        if(size==items.length) {
            resize(items.length*2);
        }
        items[nextfirst]=x;
        nextfirst = (nextfirst-1+items.length)%items.length;
        size++;
    }

    @Override
    public void addLast(T x) {
        if(size==items.length){
            resize(2*items.length);
        }
        items[nextlast]=x;
        nextlast=(nextlast+1)%items.length;
        size++;
    }

    @Override
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(items[(nextfirst + 1 + i) % items.length]);
        }
        return list;
    }

    @Override
    public boolean isEmpty() {
        return size==0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T removeFirst() {
        if(size==0)
            return null;

        int firstIndex = (nextfirst + 1) % items.length;
        T tempT = items[firstIndex];
        items[firstIndex]=null;
        nextfirst=(nextfirst+1)%items.length;
        size--;
        if(size<items.length/4){
            resize(items.length/2);
        }
        return tempT;
    }

    @Override
    public T removeLast() {
        if(size==0)
            return null;
        int lastIndex = Math.floorMod(nextlast - 1, items.length);
        T tempT = items[lastIndex];
        items[lastIndex]=null;
        nextlast=(nextlast-1+items.length)%items.length;
        size--;
        if(size<items.length/4){
            resize(items.length/2);
        }
        return tempT;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        int Index = (nextfirst+1+index)%items.length;
        return items[Index];
    }

    @Override
    public T getRecursive(int index) {
        throw new UnsupportedOperationException("No need to implement getRecursive for proj 1b");
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }
    private class ArrayDequeIterator implements Iterator{

        private int pos;

        public ArrayDequeIterator(){
            pos =0;
        }

        @Override
        public boolean hasNext() {
            return pos<size;
        }

        @Override
        public T next() {
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            int Index = (nextfirst + 1 +pos) % items.length;
            T item = items[Index];
            pos++;
            return item;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Deque61B))
            return false;
        Deque61B<?> that = (Deque61B<?>) o;
        if (this.size() != that.size())
            return false;
        Iterator<?> it1=this.iterator();
        Iterator<?> it2 = that.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            Object a = it1.next();
            Object b = it2.next();
            if (a == null) {
                if (b != null) return false;
            } else {
                if (!a.equals(b)) return false;
            }
        }
        return true;
    }

    @Override
    public String toString(){
        return toList().toString();
    }
}
