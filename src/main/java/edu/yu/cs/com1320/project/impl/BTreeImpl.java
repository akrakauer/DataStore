package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {
    private static final int MAX = 4;
    private Node root = new Node(0); //root of the B-tree
    private int height; //height of the B-tree
    private PersistenceManager<Key,Value> pm = null;

    private static final class Node {
        private int entryCount;
        private Entry[] entries = new Entry[BTreeImpl.MAX];
        private Node next;
        private Node previous;

        private Node(int k) {
            this.entryCount = k;
        }

        private void setNext(Node next) {
            this.next = next;
        }

        private Node getNext() {
            return this.next;
        }

        private void setPrevious(Node previous) {
            this.previous = previous;
        }
    }

    private static class Entry {
        private Comparable key;
        private Object val;
        private Node child;

        private Entry(Comparable key, Object val, Node child) {
            this.key = key;
            this.val = val;
            this.child = child;
        }
    }

    @Override
    public Value get(Key key) {
        if (key == null) {
            throw new IllegalArgumentException("argument to get() is null");
        }
        Entry entry = this.get(this.root, key, this.height);
        Value val;
        if (entry == null || entry.val == null) {
            try {
                if (this.pm == null) {
                    throw new IllegalStateException();
                }
                val = this.pm.deserialize(key);
                if (val != null) {
                    entry.val = val;
                    this.pm.delete(key);
                }
            } catch (IOException e) {
                return null;
            }
        } else {
            val = (Value)entry.val;
        }
        return val;
    }

    private Entry get(Node currentNode, Key key, int height) {
        Entry[] entries = currentNode.entries;
        if (height == 0) { //current node is external (i.e. height == 0)
            for (int j = 0; j < currentNode.entryCount; j++) {
                if (key.equals(entries[j].key)) { //found desired key. Return its value
                    return entries[j];
                }
            }
        } else { //current node is internal (height > 0)
            for (int j = 0; j < currentNode.entryCount; j++) { //if (we are at the last key in this node OR the key we are looking for is less than the next key, i.e. the desired key must be in the subtree below the current entry), then recurse into the current entry’s child
                if (j + 1 == currentNode.entryCount || key.compareTo((Key)entries[j + 1].key) < 0) {
                    return this.get(entries[j].child, key, height - 1);
                }
            }
        }
        return null; //didn't find the key
    }

    /**
     * Inserts the key-value pair into the symbol table, overwriting the old
     * value with the new value if the key is already in the symbol table. If
     * the value is {@code null}, this effectively deletes the key from the
     * symbol table.
     *
     * @param key the key
     * @param val the value
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    @Override
    public Value put(Key key, Value val) {
        if (key == null) {
            throw new IllegalArgumentException("argument key to put() is null");
        }
        Entry alreadyThere = this.get(this.root, key, this.height);
        if (alreadyThere != null) {
            Value previous;
            try {
                if (this.pm == null) {
                    throw new IllegalStateException();
                }
                previous = this.pm.deserialize(key);
                this.pm.delete(key);
            } catch (IOException e) {
                return null;
            }
            if (previous == null) {
                previous = (Value) alreadyThere.val;
            }
            alreadyThere.val = val;
            return previous;
        }
        if (val == null) {
            return null;
        }
        Node newNode = this.put(this.root, key, val, this.height);
        if (newNode == null) {
            return null;
        }
        //split the root:
        //Create a new node to be the root.
        //Set the old root to be new root's first entry.
        //Set the node returned from the call to put to be new root's second entry
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        this.height++;
        return null;
    }

    /**
     *
     * @param currentNode
     * @param key
     * @param val
     * @param height
     * @return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node
     */
    private Node put(Node currentNode, Key key, Value val, int height) {
        int j;
        Entry newEntry = new Entry(key, val, null);
        if (height == 0) { //external node
            //find index in currentNode’s entry[] to insert new entry, we look for key < entry.key since we want to leave j pointing to the slot to insert the new entry, hence we want to find the first entry in the current node that key is LESS THAN
            for (j = 0; j < currentNode.entryCount; j++) {
                if (key.compareTo((Key)currentNode.entries[j].key) < 0) {
                    break;
                }
            }
        } else { //internal node
            //find index in node entry array to insert the new entry
            for (j = 0; j < currentNode.entryCount; j++) {
                //if (we are at the last key in this node OR the key we are looking for is less than the next key, i.e. the desired key must be added to the subtree below the current entry), then do a recursive call to put on the current entry’s child
                if ((j + 1 == currentNode.entryCount) || key.compareTo((Key)currentNode.entries[j + 1].key) < 0) {
                    //increment j (j++) after the call so that a new entry created by a split will be inserted in the next slot
                    Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null) {
                        return null;
                    }
                    //if the call to put returned a node, it means I need to add a new entry to the current node
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        for (int i = currentNode.entryCount; i > j; i--) { //shift entries over one place to make room for new entry
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        //add new entry
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < MAX) { //no structural changes needed in the tree, so just return null
            return null;
        } else { //will have to create new entry in the parent due to the split, so return the new node, which is the node for which the new entry will be created
            return this.split(currentNode, height);
        }
    }

    private Node split(Node currentNode, int height) {
        Node newNode = new Node(MAX / 2);
        //by changing currentNode.entryCount, we will treat any value at index higher than the new currentNode.entryCount as if it doesn't exist
        currentNode.entryCount = MAX / 2;
        //copy top half of h into t
        for (int j = 0; j < MAX / 2; j++) {
            newNode.entries[j] = currentNode.entries[MAX / 2 + j];
        }
        //external node
        if (height == 0) {
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }

    @Override
    public void moveToDisk(Key k) throws Exception {
        if (this.pm == null) {
            throw new IllegalStateException();
        }
        if (k == null) {
            throw new IllegalArgumentException();
        }
        Value val = this.get(k);
        if (val == null) {
            throw new NoSuchElementException();
        }
        Entry e = this.get(this.root, k, this.height);
        e.val = null;
        this.pm.serialize(k, val);
    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key, Value> pm) {
        this.pm = pm;
    }
}