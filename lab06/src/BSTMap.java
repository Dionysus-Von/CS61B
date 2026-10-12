import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

public class BSTMap<K extends Comparable<K>,V> implements Map61B<K,V> {
    private Node rootNode;
    private int size;
    private class Node{
        private K k;
        private V v;
        private Node left;
        private Node right;

        public Node(K key,V value){
            k=key;
            v=value;
            left=null;
            right=null;
        }
    }
    public BSTMap(){
        rootNode = null;
        size = 0;
    }

    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (rootNode == null) {
            rootNode = new Node(key, value);
            size += 1;
            return;
        }
        Node p = rootNode;
        while (p.k.compareTo(key) != 0) {
            if(p.k.compareTo(key)>0){
                if(p.left==null){
                    p.left= new Node(key,value);
                    size++;
                    return;
                }
                p=p.left;
            }else{
                if(p.right==null) {
                    p.right= new Node(key,value);
                    size++;
                    return;
                    }
                p=p.right;
            }
        }
        p.v=value;
    }



    @Override
    public V get(K key) {
        Node p = findNode(key);
        if(p == null)
            return null;

        return p.v;
    }

    @Override
    public boolean containsKey(K key) {
        Node p = findNode(key);
        return p != null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        rootNode = null;
        size = 0;
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        Node p = findParent(key);
        if (p != null) {
            if (p.left.k==key){
                V value = p.left.v;
                p.left=null;
                size--;
                return value;
            }
            if (p.right.k==key){
               V value = p.right.v;
                p.right=null;
                size--;
                return value;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(Consumer<? super K> action) {
        Map61B.super.forEach(action);
    }

    @Override
    public Spliterator<K> spliterator() {
        return Map61B.super.spliterator();
    }

    private Node findNode(K key){
        Node p = rootNode;
        while(p!=null){
            if(p.k.compareTo(key) >0){
                if(p.left==null)
                    return null;
                p=p.left;
            }else if(p.k.compareTo(key)<0){
                    if(p.right==null)
                        return null;
                    p=p.right;
                }
            else{
                return p;
            }
        }
        return null;
    }
    private Node findParent(K key) {
        Node parent = null;
        Node p = rootNode;

        while (p != null) {
            int cmp = key.compareTo(p.k);
            if (cmp == 0) {
                return parent;
            }
            parent = p;
            if (cmp < 0) {
                p = p.left;
            } else {
                p = p.right;
            }
        }
        return null;
    }
}
