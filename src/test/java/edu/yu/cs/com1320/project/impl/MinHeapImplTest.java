package edu.yu.cs.com1320.project.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MinHeapImplTest {

    static class Entry implements Comparable<Entry> {
        String key;
        int value;

        Entry(String k, int v) {
            this.key = k;
            this.value = v;
        }

        @Override
        public int compareTo(Entry i) {
            return Integer.compare(this.value, i.value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Entry)) {
                return false;
            }
            return this.key.equals(((Entry)o).key);
        }
    }

    MinHeapImpl<Entry> h = new MinHeapImpl<>();
    Entry i, i2, i3;

    @BeforeEach
    void setUp() {
        i = new Entry("test", 3);
        i2 = new Entry("fghjk", 5);
        i3 = new Entry("tdryu", 6);
        h.insert(i2);
        h.insert(i3);
        h.insert(i);
        assertEquals(new Entry("test", 3), h.remove());
        h.insert(i);
    }

    @Test
    void reHeapify() {
        i3.value = 1;
        h.reHeapify(i3);
        assertEquals(new Entry("tdryu", 1), h.remove());
        h.insert(i3);
        i3.value = 6;
        h.reHeapify(i3);
        assertEquals(new Entry("test", 3), h.remove());
        assertThrows(NoSuchElementException.class, () -> h.reHeapify(null));
    }
}