package deque;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class LinkedListDeque61B<T> implements Deque61B<T> {
    private Node sentinel;
    private int size = 0;

    public LinkedListDeque61B(){
        sentinel = new Node(null,null,null);
        sentinel.next=sentinel;
        sentinel.prev=sentinel;
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node p;

        LinkedListDequeIterator(){
            p = sentinel.next;

        }
        @Override
        public boolean hasNext() {
            return p != sentinel;
        }

        @Override
        public T next() {
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            T item = p.item;
            p=p.next;
            return item;
        }
    }

    private class Node{
        public T item;
        public Node next;
        public Node prev;

        public Node(T item,Node next,Node prev){
            this.item = item;
            this.next = next;
            this.prev = prev;
        }
    }

    @Override
    public void addFirst(T x) {
        Node first = new Node(x,sentinel.next,sentinel);
        sentinel.next.prev = first;
        sentinel.next = first;
        size++;
    }

    @Override
    public void addLast(T x) {
        Node last = new Node(x,sentinel,sentinel.prev);
        sentinel.prev.next = last;
        sentinel.prev=last;
        size++;
    }

    @Override
    public List<T> toList() {
        List<T> resL = new ArrayList<>();
        Node p = sentinel.next;
        while(p != sentinel){
            resL.add(p.item);
            p = p.next;
        }
        return resL;
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
        if (size == 0) {
            return null;
        }
        T temp = sentinel.next.item;
        sentinel.next=sentinel.next.next;
        sentinel.next.prev=sentinel;
        size--;
        return temp;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        T temp = sentinel.prev.item;
        sentinel.prev=sentinel.prev.prev;
        sentinel.prev.next=sentinel;
        size--;
        return temp;
    }

    @Override
    public T get(int index) {
        Node tempNode = sentinel.next;
        if(index>=0&&index<size){
            for(int i=0;i<index;i++){
                tempNode=tempNode.next;
            }
            return tempNode.item;
        }
        else
            return null;
    }

    @Override
    public T getRecursive(int index) {
        if(index>=0&&index<size) {
            return getNode(sentinel.next, index);
        }
        else
            return null;
    }
    private T getNode(Node node, int index) {
        if (index == 0) {
            return node.item;
        }
        return getNode(node.next, index - 1);
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
