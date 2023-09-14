package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class BTreeImplTest {
    private static final class Node {
        private int entryCount;
        private Entry[] entries = new Entry[4];

        private Node(int k) {
            this.entryCount = k;
        }
    }

    private static class Entry
    {
        private Comparable key;
        private Object val;
        private Node child;

        private Entry(Comparable key, Object val, Node child) {
            this.key = key;
            this.val = val;
            this.child = child;
        }
        private Object getValue()
        {
            return this.val;
        }
        private Comparable getKey()
        {
            return this.key;
        }
    }

    @Test
    void get() throws URISyntaxException {
        BTreeImpl<Integer, Document> st = new BTreeImpl<>();
        st.setPersistenceManager(new PersistenceManager<>() {
            @Override
            public void serialize(Integer integer, Document val) throws IOException {

            }

            @Override
            public Document deserialize(Integer integer) throws IOException {
                return null;
            }

            @Override
            public boolean delete(Integer integer) throws IOException {
                return false;
            }
        });
        HashMap<Integer, Document> m = new HashMap<>();
        st.put(1, new DocumentImpl(new URI("https://www.yu.edu/doc1"), "one", null));
        st.put(2, new DocumentImpl(new URI("https://www.yu.edu/doc2"), "two", null));
        st.put(3, new DocumentImpl(new URI("https://www.yu.edu/doc3"), "three", null));
        st.put(4, new DocumentImpl(new URI("https://www.yu.edu/doc4"), "four", null));
        st.put(5, new DocumentImpl(new URI("https://www.yu.edu/doc5"), "five", null));
        st.put(6, new DocumentImpl(new URI("https://www.yu.edu/doc6"), "six", null));
        st.put(7, new DocumentImpl(new URI("https://www.yu.edu/doc7"), "seven", null));
        st.put(8, new DocumentImpl(new URI("https://www.yu.edu/doc8"), "eight", null));
        st.put(9, new DocumentImpl(new URI("https://www.yu.edu/doc9"), "nine", null));
        st.put(10, new DocumentImpl(new URI("https://www.yu.edu/doc10"), "ten", null));
        st.put(11, new DocumentImpl(new URI("https://www.yu.edu/doc11"), "eleven", null));
        st.put(12, new DocumentImpl(new URI("https://www.yu.edu/doc12"), "twelve", null));
        st.put(13, new DocumentImpl(new URI("https://www.yu.edu/doc13"), "thirteen", null));
        m.put(1, new DocumentImpl(new URI("https://www.yu.edu/doc1"), "one", null));
        m.put(2, new DocumentImpl(new URI("https://www.yu.edu/doc2"), "two", null));
        m.put(3, new DocumentImpl(new URI("https://www.yu.edu/doc3"), "three", null));
        m.put(4, new DocumentImpl(new URI("https://www.yu.edu/doc4"), "four", null));
        m.put(5, new DocumentImpl(new URI("https://www.yu.edu/doc5"), "five", null));
        m.put(6, new DocumentImpl(new URI("https://www.yu.edu/doc6"), "six", null));
        m.put(7, new DocumentImpl(new URI("https://www.yu.edu/doc7"), "seven", null));
        m.put(8, new DocumentImpl(new URI("https://www.yu.edu/doc8"), "eight", null));
        m.put(9, new DocumentImpl(new URI("https://www.yu.edu/doc9"), "nine", null));
        m.put(10, new DocumentImpl(new URI("https://www.yu.edu/doc10"), "ten", null));
        m.put(11, new DocumentImpl(new URI("https://www.yu.edu/doc11"), "eleven", null));
        m.put(12, new DocumentImpl(new URI("https://www.yu.edu/doc12"), "twelve", null));
        m.put(13, new DocumentImpl(new URI("https://www.yu.edu/doc13"), "thirteen", null));

        for (int i = 1; i < 14; i++) {
            assertNotNull(st.get(i));
            assertEquals(m.get(i), st.get(i));
        }
        st.put(1, null);
        st.put(13, null);
        for (int i = 1; i < 14; i++) {
            if (i != 1 && i != 13) {
                assertTrue(st.get(i) != null && m.get(i).equals(st.get(i)));
            } else {
                assertNull(st.get(i));
            }
        }
    }

    @Test
    void moveToDiskAndBackAndDelete() throws Exception {
        BTreeImpl<URI, Document> btree = new BTreeImpl<>();
        btree.setPersistenceManager(new DocumentPersistenceManager(null));
        URI u = new URI("ll:tes't");
        Document d = new DocumentImpl(u,"testdoc", null);
        assertNull(btree.put(u, d));
        File f = new File(System.getProperty("user.dir"), "test.json");
        assertFalse(f.exists());
        btree.moveToDisk(u);
        assertTrue(f.exists());
        assertEquals(d, btree.get(u));
        assertFalse(f.exists());
        assertEquals(d, btree.put(u, null));
        assertNull(btree.get(u));
    }
}