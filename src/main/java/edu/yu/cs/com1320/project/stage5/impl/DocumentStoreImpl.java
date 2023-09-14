package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage5.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class DocumentStoreImpl implements DocumentStore {
    private final StackImpl<Undoable> commandStack = new StackImpl<>();
    private final TrieImpl<URI> trie = new TrieImpl<>();
    private final MinHeapImpl<Node> heap = new MinHeapImpl<>();
    private final BTreeImpl<URI,Document> btree = new BTreeImpl<>();
    private int maxDocs = Integer.MAX_VALUE;
    private int maxBytes = Integer.MAX_VALUE;
    private int currentDocs = 0;
    private int currentBytes = 0;
    private final DocumentPersistenceManager pm;

    public DocumentStoreImpl(File baseDir) {
        this.pm = new DocumentPersistenceManager(baseDir);
        this.btree.setPersistenceManager(this.pm);
    }

    public DocumentStoreImpl() {
        this.pm = new DocumentPersistenceManager(null);
        this.btree.setPersistenceManager(this.pm);
    }

    @Override
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null || format == null) {
            throw new IllegalArgumentException();
        }
        if (input == null) {
            int hashcode = 0;
            Document doc = this.btree.get(uri);
            if (doc != null) {
                hashcode = doc.hashCode();
                this.delete(uri);
            }
            return hashcode;
        }
        byte[] byteArray = input.readAllBytes();
        DocumentImpl d;
        if (format == DocumentFormat.BINARY) {
            d = new DocumentImpl(uri, byteArray);
        } else {
            String s = new String(byteArray);
            d = new DocumentImpl(uri, s, null);
        }
        Document previous = this.btree.get(uri);
        if (previous != null) {
            this.deleteLogic(previous);
        }
        this.putLogic(uri, d);
        this.commandStack.push(new GenericCommand<>(uri, (u) -> {
            this.deleteLogic(d);
            this.putLogic(u, previous);
            return true;
        }));
        return previous == null ? 0 : previous.hashCode();
    }

    private void putLogic(URI u, Document d) {
        if (d != null) {
            this.btree.put(u, d);
            for (String word : d.getWords()) {
                this.trie.put(word, d.getKey());
            }
            d.setLastUseTime(System.nanoTime());
            Node n = new Node(d.getKey(), this.btree);
            try {
                this.heap.reHeapify(n);
            } catch (NoSuchElementException e) {
                this.heap.insert(n);
            }
            int bytes = d.getDocumentBinaryData() == null ? d.getDocumentTxt().getBytes().length : d.getDocumentBinaryData().length;
            if (bytes > this.maxBytes) {
                this.removeFromHeap(d);
            } else {
                while (this.currentBytes + bytes > this.maxBytes || this.currentDocs >= this.maxDocs) {
                    this.moveLeastRecentDocToDisk();
                }
            }
            this.currentBytes += bytes;
            this.currentDocs++;
        }
    }

    @Override
    public Document get(URI uri) {
        if (uri == null) {
            return null;
        }
        boolean disk;
        try {
            disk = this.pm.deserialize(uri) != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Document d = this.btree.get(uri);
        if (d != null) {
            d.setLastUseTime(System.nanoTime());
            Node n = new Node(d.getKey(), this.btree);
            if (!disk) {
                this.heap.reHeapify(n);
            } else {
                this.heap.insert(n);
                int bytes = d.getDocumentBinaryData() == null ? d.getDocumentTxt().getBytes().length : d.getDocumentBinaryData().length;
                while (this.currentBytes + bytes > this.maxBytes || this.currentDocs >= this.maxDocs) {
                    this.moveLeastRecentDocToDisk();
                }
            }
        }
        return d;
    }

    @Override
    public boolean delete(URI uri) {
        if (uri != null && this.btree.get(uri) != null) {
            Document prev = this.btree.get(uri);
            this.deleteLogic(prev);
            this.commandStack.push(new GenericCommand<>(uri, (u) -> {
                this.putLogic(u, prev);
                return true;
            }));
            return true;
        }
        return false;
    }

    private void deleteLogic(Document d) {
        for (String word : d.getWords()) {
            this.trie.delete(word, d.getKey());
        }
        boolean disk;
        try {
            disk = this.pm.deserialize(d.getKey()) != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!disk) {
            this.removeFromHeap(d);
            this.btree.put(d.getKey(), null);
            this.currentDocs--;
            this.currentBytes -= d.getDocumentBinaryData() != null ? d.getDocumentBinaryData().length : d.getDocumentTxt().getBytes().length;
        } else {
            try {
                this.pm.delete(d.getKey());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void undo() {
        if (this.commandStack.size() > 0) {
            if (this.commandStack.peek() instanceof GenericCommand<?>) {
                this.commandStack.pop().undo();
            } else {
                Set<? extends GenericCommand<?>> set = ((CommandSet<?>)this.commandStack.pop()).undoAll();
                long time = System.nanoTime();
                for (GenericCommand<?> gc : set) {
                    Document doc = this.get((URI)gc.getTarget());
                    if (doc != null) {
                        doc.setLastUseTime(time);
                        this.heap.reHeapify(new Node(doc.getKey(), this.btree));
                    }
                }
            }
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void undo(URI uri) {
        StackImpl<Undoable> helper = new StackImpl<>();
        boolean noActions = true;
        while (this.commandStack.size() > 0) {
            Undoable temp = this.commandStack.pop();
            if (temp instanceof CommandSet) {
                if (((CommandSet<URI>)temp).undo(uri)) {
                    if (((CommandSet<URI>)temp).size() != 0) {
                        helper.push(temp);
                    }
                    noActions = false;
                    break;
                }
            } else {
                if (((GenericCommand<URI>)temp).getTarget().equals(uri)) {
                    temp.undo();
                    noActions = false;
                    break;
                }
            }
            helper.push(temp);
        }
        while (helper.size() > 0) {
            this.commandStack.push(helper.pop());
        }
        if (noActions) {
            throw new IllegalStateException();
        }
    }

    @Override
    public List<Document> search(String keyword) {
        List<URI> uris = this.trie.getAllSorted(keyword, (u1, u2) -> {
            int d1Words = this.get(u1).wordCount(keyword);
            int d2Words = this.get(u2).wordCount(keyword);
            return Integer.compare(d2Words, d1Words);
        });
        List<Document> results = new ArrayList<>();
        long time = System.nanoTime();
        for (URI u : uris) {
            Document d = this.btree.get(u);
            results.add(d);
            d.setLastUseTime(time);
            this.heap.reHeapify(new Node(u, this.btree));
        }
        return results;
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        List<URI> uris = this.trie.getAllWithPrefixSorted(keywordPrefix, (u1, u2) -> {
            char[] keywordLetters = keywordPrefix.toCharArray();
            int d1Words = this.countPrefixWords(this.get(u1), keywordLetters);
            int d2Words = this.countPrefixWords(this.get(u2), keywordLetters);
            return Integer.compare(d2Words, d1Words);
        });
        List<Document> results = new ArrayList<>();
        long time = System.nanoTime();
        for (URI u : uris) {
            Document d = this.btree.get(u);
            results.add(d);
            d.setLastUseTime(time);
            this.heap.reHeapify(new Node(u, this.btree));
        }
        return results;
    }

    private int countPrefixWords(Document d, char[] keywordLetters) {
        int dWords = 0;
        for (String word : d.getWords()) {
            char[] letters = word.toCharArray();
            boolean equals = false;
            for (int i = 0; i < keywordLetters.length && i < letters.length; i++) {
                if (keywordLetters[i] != letters[i]) {
                    break;
                }
                if (i == keywordLetters.length - 1) {
                    equals = true;
                    break;
                }
            }
            if (equals) {
                dWords += d.wordCount(word);
            }
        }
        return dWords;
    }

    @Override
    public Set<URI> deleteAll(String keyword) {
        return this.stringDelete(this.trie.deleteAll(keyword));
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        return this.stringDelete(this.trie.deleteAllWithPrefix(keywordPrefix));
    }

    private Set<URI> stringDelete(Set<URI> result) {
        Set<URI> uriResult = new HashSet<>();
        CommandSet<URI> commands = new CommandSet<>();
        for (URI uri : result) {
            uriResult.add(uri);
            Document d = this.btree.get(uri);
            this.deleteLogic(d);
            commands.addCommand(new GenericCommand<>(uri, (u) -> {
                this.putLogic(u, d);
                return true;
            }));
        }
        this.commandStack.push(commands);
        return uriResult;
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException();
        }
        this.maxDocs = limit;
        while (this.currentDocs > limit) {
            this.moveLeastRecentDocToDisk();
        }
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException();
        }
        this.maxBytes = limit;
        while (this.currentBytes > limit) {
            this.moveLeastRecentDocToDisk();
        }
    }

    private void moveLeastRecentDocToDisk() {
        Node n = this.heap.remove();
        this.heap.insert(n);
        Document d = this.btree.get(n.uri);
        try {
            this.btree.moveToDisk(n.uri);
        } catch (Exception e) {
            throw new NoSuchElementException();
        }
        this.currentDocs--;
        this.currentBytes -= d.getDocumentBinaryData() != null ? d.getDocumentBinaryData().length : d.getDocumentTxt().getBytes().length;
        this.heap.remove();
    }

    private void removeFromHeap(Document d) {
        Document doc = this.btree.get(d.getKey());
        if (doc != null) {
            doc.setLastUseTime(Long.MIN_VALUE);
            this.heap.reHeapify(new Node(doc.getKey(), this.btree));
            this.heap.remove();
            try {
                this.btree.moveToDisk(d.getKey());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class Node implements Comparable<Node> {
        private final URI uri;
        private final BTreeImpl<URI, Document> btree;

        private Node(URI u, BTreeImpl<URI, Document> btree) {
            this.uri = u;
            this.btree = btree;
        }

        @Override
        public int compareTo(Node n){
            return Long.compare(this.btree.get(this.uri).getLastUseTime(), this.btree.get(n.uri).getLastUseTime());
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Node n)) {
                return false;
            }
            return this.uri.equals(n.uri);
        }
    }
}