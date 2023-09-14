package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStoreImplTest {

    DocumentStoreImpl d;
    String str = "wlords";
    InputStream s = new ByteArrayInputStream(str.getBytes());
    ByteArrayInputStream s2 = new ByteArrayInputStream(str.getBytes());
    URI u = new URI("h:uri");
    URI u2 = new URI("h:uri2");
    URI missing = new URI("h:u");
    Document byteDoc;
    DocumentImpl txtDoc;
    File f = new File(System.getProperty("user.dir"), "u.json");
    File f1 = new File(System.getProperty("user.dir"), "uri.json");
    File f2 = new File(System.getProperty("user.dir"), "uri2.json");
    File f3 = new File(System.getProperty("user.dir"), "uri3.json");
    File f4 = new File(System.getProperty("user.dir"), "uri4.json");
    File f5 = new File(System.getProperty("user.dir"), "uri5.json");

    DocumentStoreImplTest() throws URISyntaxException {
    }

    @BeforeEach
    void setUp() throws IOException {
        d = new DocumentStoreImpl();
        byteDoc = new DocumentImpl(u, str.getBytes());
        txtDoc = new DocumentImpl(u2, str, null);
        d.put(s, u, DocumentStore.DocumentFormat.BINARY);
        d.put(s2, u2, DocumentStore.DocumentFormat.TXT);
    }

    @Test
    void put() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> d.put(s, null, DocumentStore.DocumentFormat.BINARY));
        assertThrows(IllegalArgumentException.class, () -> d.put(s, u, null));
        assertEquals(0, d.put(null, missing, DocumentStore.DocumentFormat.TXT));
        assertEquals(byteDoc.hashCode(), d.put(null, u, DocumentStore.DocumentFormat.BINARY));
        assertEquals(txtDoc.hashCode(), d.put(null, u2, DocumentStore.DocumentFormat.TXT));
    }

    @Test
    void get() {
        assertEquals(d.get(u), byteDoc);
        assertEquals(d.get(u2), txtDoc);
        assertNull(d.get(null));
        assertNull(d.get(missing));
    }

    @Test
    void delete() {
        assertTrue(d.delete(u));
        assertFalse(d.delete(missing));
        assertTrue(d.delete(u2));
        assertFalse(d.delete(null));
    }

    @Test
    void undo() throws IOException {
        d.undo();
        assertNull(d.get(u2));
        assertEquals(byteDoc, d.get(u));
        d = new DocumentStoreImpl();
        assertThrows(IllegalStateException.class, () -> d.undo());
        d.put(new ByteArrayInputStream(str.getBytes()), u, DocumentStore.DocumentFormat.BINARY);
        d.delete(u);
        assertNull(d.get(u));
        d.undo();
        assertEquals(byteDoc, d.get(u));
        d.put(new ByteArrayInputStream(str.getBytes()), u2, DocumentStore.DocumentFormat.TXT);
        assertEquals(txtDoc, d.get(u2));
        d.put(new ByteArrayInputStream(str.getBytes()), u2, DocumentStore.DocumentFormat.BINARY);
        assertEquals(new DocumentImpl(u2, str.getBytes()), d.get(u2));
        d.undo();
        assertEquals(txtDoc, d.get(u2));
    }

    @Test
    void undoUri() throws IOException {
        assertThrows(IllegalStateException.class, () -> d.undo(missing));
        d.undo(u);
        assertNotEquals(byteDoc, d.get(u));
        assertNull(d.get(u));
        assertEquals(txtDoc, d.get(u2));
        d = new DocumentStoreImpl();
        assertThrows(IllegalStateException.class, () -> d.undo(u));
        d.put(new ByteArrayInputStream(str.getBytes()), u, DocumentStore.DocumentFormat.BINARY);
        d.put(new ByteArrayInputStream(str.getBytes()), u2, DocumentStore.DocumentFormat.TXT);
        d.delete(u);
        d.delete(u2);
        d.undo(u);
        assertEquals(byteDoc, d.get(u));
        d.undo(u2);
        assertEquals(txtDoc, d.get(u2));
    }

    @Test
    void search() throws IOException {
        d.put(new ByteArrayInputStream("words word word wor wordiest wordy wordsearch".getBytes()), u, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("words wordy wordsearch".getBytes()), u2, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("words word word wor world wordiest and but me two ## 453 wordy wordsearch words word word wor wordiest wordy wordsearch".getBytes()), missing, DocumentStore.DocumentFormat.TXT);
        List<Document> results = new ArrayList<>();
        results.add(new DocumentImpl(missing, "words word word wor world wordiest and but me two ## 453 wordy wordsearch words word word wor wordiest wordy wordsearch", null));
        results.add(new DocumentImpl(u, "words word word wor wordiest wordy wordsearch", null));
        List<Document> search = d.search("word");
        assertEquals(results.size(), search.size());
        for (int i = 0; i < results.size(); i++) {
            assertEquals(results.get(i), search.get(i));
        }
    }

    @Test
    void searchByPrefix() throws IOException {
        d.put(new ByteArrayInputStream("words word word wor wordiest wordy wordsearch".getBytes()), u, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("words wordy wordsearch".getBytes()), u2, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("words word word wor world wordiest and but me two ## 453 wordy wordsearch words word word wor wordiest wordy wordsearch".getBytes()), missing, DocumentStore.DocumentFormat.TXT);
        List<Document> results = new ArrayList<>();
        results.add(new DocumentImpl(missing, "words word word wor world wordiest and but me two ## 453 wordy wordsearch words word word wor wordiest wordy wordsearch", null));
        results.add(new DocumentImpl(u2, "words word word wor wordiest wordy wordsearch", null));
        results.add(new DocumentImpl(u, "words wordy wordsearch", null));
        List<Document> search = d.searchByPrefix("word");
        assertEquals(results.size(), search.size());
        for (int i = 0; i < results.size(); i++) {
            assertEquals(results.get(i).getDocumentTxt(), search.get(i).getDocumentTxt());
        }
        results.clear();
        results.add(new DocumentImpl(missing, "words word word wor world wordiest and but me two ## 453 wordy wordsearch words word word wor wordiest wordy wordsearch", null));
        search = d.searchByPrefix("a");
        assertEquals(results.size(), search.size());
        for (int i = 0; i < results.size(); i++) {
            assertEquals(results.get(i), search.get(i));
        }
    }

    @Test
    void deleteAllAndUndoOnCommandSet() throws IOException {
        d.put(new ByteArrayInputStream("words word word wor wordiest wordy wordsearch".getBytes()), u, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("words wordy wordsearch".getBytes()), u2, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("words word word wor world wordiest and but me two ## 453 wordy wordsearch words word word wor wordiest wordy wordsearch".getBytes()), missing, DocumentStore.DocumentFormat.TXT);
        List<Document> list = d.search("words");
        long last = list.get(0).getLastUseTime();
        for (Document doc : list) {
            assertEquals(last, doc.getLastUseTime());
        }
        Set<URI> results = d.deleteAll("words");
        Set<URI> uris = new HashSet<>();
        uris.add(u);
        uris.add(missing);
        uris.add(u2);
        assertEquals(uris, results);
        for (Document doc : list) {
            assertTrue(results.contains(doc.getKey()));
        }
        assertEquals(new ArrayList<Document>(), d.search("words"));
        d.undo();
        assertEquals(list, d.search("words"));
    }

    @Test
    void deleteAllWithPrefixAndUndoUriOnCommandSet() throws IOException {
        d.put(new ByteArrayInputStream("words word word wor wordiest wordy wordsearch".getBytes()), u, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("words wordy wordsearch".getBytes()), u2, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("words word word wor world wordiest and but me two ## 453 wordy wordsearch words word word wor wordiest wordy wordsearch".getBytes()), missing, DocumentStore.DocumentFormat.TXT);
        List<Document> list = d.searchByPrefix("w");
        Set<URI> results = d.deleteAllWithPrefix("w");
        Set<URI> uris = new HashSet<>();
        uris.add(u);
        uris.add(missing);
        uris.add(u2);
        assertEquals(uris, results);
        for (Document doc : list) {
            assertTrue(results.contains(doc.getKey()));
        }
        assertEquals(new ArrayList<Document>(), d.search("w"));
        d.undo(u);
        list.removeIf((doc) -> !doc.getKey().equals(u));
        assertEquals(list, d.searchByPrefix("w"));
    }

    @Test
    void removeCommandSetFromStack() throws IOException {
        d.put(new ByteArrayInputStream("car".getBytes()), u, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("words wordy wordsearch".getBytes()), u2, DocumentStore.DocumentFormat.TXT);
        d.deleteAll("car");
        d.deleteAllWithPrefix("w");
        assertEquals(new ArrayList<>(), d.searchByPrefix("w"));
        assertEquals(new ArrayList<>(), d.searchByPrefix("c"));
        d.undo(u2);
        assertEquals(new DocumentImpl(u2, "words wordy wordsearch", null), d.get(u2));
        List<Document> result = new ArrayList<>();
        result.add(new DocumentImpl(u2, "words wordy wordsearch", null));
        assertEquals(result, d.searchByPrefix("word"));
        d.put(new ByteArrayInputStream("car".getBytes()), u, DocumentStore.DocumentFormat.TXT);
        assertEquals(new DocumentImpl(u, "car", null), d.get(u));
        result = new ArrayList<>();
        result.add(new DocumentImpl(u, "car", null));
        assertEquals(result, d.search("car"));
        d.undo(u);
        assertEquals(new ArrayList<>(), d.search("car"));
    }

    @Test
    void setMaxDocumentCount() throws Exception {
        d.put(new ByteArrayInputStream("words word word wor wordiest wordy wordsearch".getBytes()), u, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("words wordy wordsearch".getBytes()), u2, DocumentStore.DocumentFormat.TXT);
        d.setMaxDocumentCount(2);
        d.put(new ByteArrayInputStream("words word word wor world wordiest and but me two ## 453 wordy wordsearch words word word wor wordiest wordy wordsearch".getBytes()), missing, DocumentStore.DocumentFormat.TXT);
        assertTrue(f1.exists());
        f1.delete();
        d = new DocumentStoreImpl();
        d.put(new ByteArrayInputStream("words word word wor wordiest wordy wordsearch".getBytes()), u, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("words wordy wordsearch".getBytes()), u2, DocumentStore.DocumentFormat.TXT);
        d.setMaxDocumentCount(1);
        assertTrue(f1.exists());
    }

    @Test
    void undoDeletePrefix() throws Exception {
        d = new DocumentStoreImpl();
        URI u3 = new URI("h:uri3");
        d.put(new ByteArrayInputStream("aaron earned an iron urn".getBytes()), u, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("aaron earned an iron urn".getBytes()), u2, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("aaron earned an iron urn".getBytes()), u3, DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("test".getBytes()), new URI("h:uri4"), DocumentStore.DocumentFormat.TXT);
        d.put(new ByteArrayInputStream("test".getBytes()), new URI("h:uri5"), DocumentStore.DocumentFormat.TXT);
        Document doc1 = d.get(u);
        Document doc2 = d.get(u3);
        d.deleteAllWithPrefix("aa");
        d.setMaxDocumentCount(2);
        d.undo();
        assertNotNull(doc1);
        assertNotNull(doc2);
        //assertEquals(doc1.getLastUseTime(), doc2.getLastUseTime());
    }

    @AfterEach
    void tearDown() {
        f.delete();
        f1.delete();
        f2.delete();
        f3.delete();
        f4.delete();
        f5.delete();
    }
}