package edu.yu.cs.com1320.project.stage5.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DocumentImplTest {

    DocumentImpl s;
    DocumentImpl b;
    String str = "words 1 2 3 4 words 2# 3 4 1 & 9";

    @BeforeEach
    void setUp() throws URISyntaxException {
        s = new DocumentImpl(new URI("uri"), str, null);
        b = new DocumentImpl(new URI("uri2"), str.getBytes());
    }

    @Test
    void testConstructors() throws URISyntaxException {
        URI u = new URI("uri");
        String e = "";
        String n = null;
        byte[] by = null;
        byte[] empty = {};
        assertThrows(IllegalArgumentException.class, () -> new DocumentImpl(null, str, null));
        assertThrows(IllegalArgumentException.class, () -> new DocumentImpl(null, str.getBytes()));
        assertThrows(IllegalArgumentException.class, () -> new DocumentImpl(u, n, null));
        assertThrows(IllegalArgumentException.class, () -> new DocumentImpl(u, by));
        assertThrows(IllegalArgumentException.class, () -> new DocumentImpl(u, e, null));
        assertThrows(IllegalArgumentException.class, () -> new DocumentImpl(u, empty));

    }

    @Test
    void getDocumentTxt() {
        assertEquals(s.getDocumentTxt(), str);
        assertNull(b.getDocumentTxt());
    }

    @Test
    void getDocumentBinaryData() {
        assertArrayEquals(b.getDocumentBinaryData(), str.getBytes());
        assertNull(s.getDocumentBinaryData());
    }

    @Test
    void getKey() throws URISyntaxException {
        assertEquals(s.getKey(), new URI("uri"));
        assertEquals(b.getKey(), new URI("uri2"));
    }

    @Test
    void testEquals() throws URISyntaxException {
        assertNotEquals(s, b);
        DocumentImpl d = new DocumentImpl(new URI("uri"), str, null);
        assertEquals(d, s);
    }

    @Test
    void testHashCode() throws URISyntaxException {
        DocumentImpl d = new DocumentImpl(new URI("uri"), str, null);
        DocumentImpl bytes = new DocumentImpl(new URI("uri2"), str.getBytes());
        assertEquals(s.hashCode(), d.hashCode());
        assertNotEquals(s.hashCode(), b.hashCode());
        assertEquals(bytes.hashCode(), b.hashCode());
    }

    @Test
    void wordCount() {
        assertEquals(2, s.wordCount("2"));
        assertEquals(0, b.wordCount("2"));
        assertEquals(0, s.wordCount("22"));
    }

    @Test
    void getWords() {
        Set<String> allWords = s.getWords();
        assertEquals(6, allWords.size());
        Set<String> results = new HashSet<>();
        results.add("1");
        results.add("2");
        results.add("3");
        results.add("4");
        results.add("9");
        results.add("words");
        assertEquals(results, allWords);
        Set<String> empty = new HashSet<>();
        assertEquals(empty, b.getWords());
    }
}