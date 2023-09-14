package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {
    private Node<Value> root = null;

    private static class Node<Value> {
        private final Set<Value> val = new HashSet<>();
        private final HashMap<Character,Node<Value>> links = new HashMap<>();
    }

    @Override
    public void put(String key, Value val) {
        if (val == null) {
            return;
        }
        this.root = put(this.root, key, val, 0);
    }

    private Node<Value> put(Node<Value> x, String key, Value val, int d) {
        if (x == null) {
            x = new Node<>();
        }
        if (d == key.length()) {
            x.val.add(val);
            return x;
        }
        char c = key.charAt(d);
        if (c < 48 || (c > 57 && c < 65) || (c > 90 && c < 97) || c > 122) {
            throw new IllegalArgumentException();
        }
        x.links.put(c, this.put(x.links.get(c), key, val, d + 1));
        return x;
    }

    @Override
    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
        if (key == null || comparator == null) {
            throw new IllegalArgumentException();
        }
        Node<Value> x = this.get(this.root, key, 0);
        List<Value> result = new ArrayList<>();
        this.sort(x, comparator, result);
        return result;
    }

    private Node<Value> get(Node<Value> x, String key, int d) {
        if (x == null) {
            return null;
        }
        if (d == key.length()) {
            return x;
        }
        char c = key.charAt(d);
        return this.get(x.links.get(c), key, d + 1);
    }

    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        if (prefix == null || comparator == null) {
            throw new IllegalArgumentException();
        }
        Node<Value> prefixNode = this.get(this.root, prefix, 0);
        List<Value> sorted = new ArrayList<>();
        if (prefixNode != null) {
            this.getAllPrefix(prefixNode, comparator, sorted);
            this.sort(prefixNode, comparator, sorted); //check this
        }
        return sorted;
    }

    private void getAllPrefix (Node<Value> node, Comparator<Value> comparator, List<Value> results) {
        for (Character c : node.links.keySet()) {
            this.getAllPrefix(node.links.get(c), comparator, results);
        }
        if (!node.val.isEmpty()) {
            this.sort(node, comparator, results);
        }
    }

    private void sort(Node<Value> node, Comparator<Value> comparator, List<Value> sortedValues) {
        if (node != null) {
            for (Value value : node.val) {
                if (!sortedValues.contains(value)) {
                    sortedValues.add(value);
                }
            }
            sortedValues.sort(comparator);
        }
    }

    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException();
        }
        Node<Value> prefixNode = this.get(this.root, prefix, 0);
        Set<Value> sorted = new HashSet<>();
        if (prefixNode != null) {
            this.deleteAllPrefix(prefixNode, sorted);
            this.deleteAll(this.root, prefix, sorted, 0);
        }
        return sorted;
    }

    private void deleteAllPrefix (Node<Value> node, Set<Value> results) {
        for (Character c : node.links.keySet()) {
            this.deleteAllPrefix(node.links.get(c), results);
        }
        if (!node.val.isEmpty()) {
            results.addAll(node.val);
        }
        node.links.clear();
    }

    @Override
    public Set<Value> deleteAll(String key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        Set<Value> delete = new HashSet<>();
        this.deleteAll(this.root, key, delete, 0);
        return delete;
    }

    private void deleteAll(Node<Value> x, String key, Set<Value> results, int d) {
        if (x == null) {
            return;
        }
        if (d == key.length()) {
            results.addAll(x.val);
            x.val.clear();
            return;
        }
        Character c = key.charAt(d);
        this.deleteAll(x.links.get(c), key, results,d + 1);
        if (x.links.get(c).val.isEmpty() && x.links.get(c).links.isEmpty()) {
            x.links.remove(c);
        }
    }

    @Override
    public Value delete(String key, Value val) {
        if (val == null) {
            throw new IllegalArgumentException();
        }
        Node<Value> node = this.get(this.root, key, 0);
        if (node != null && node.val.contains(val)) {
            this.delete(this.root, key, val, 0);
            return val;
        }
        return null;
    }

    private void delete(Node<Value> x, String key, Value val, int d) {
        if (x == null) {
            return;
        }
        if (d == key.length()) {

            x.val.remove(val);
            return ;
        }
        Character c = key.charAt(d);
        this.delete(x.links.get(c), key, val,d + 1);
        if (x.links.get(c).val.isEmpty() && x.links.get(c).links.isEmpty()) {
            x.links.remove(c);
        }
    }
}