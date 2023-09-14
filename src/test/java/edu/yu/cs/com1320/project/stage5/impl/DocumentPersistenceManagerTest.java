package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class DocumentPersistenceManagerTest {
    private DocumentPersistenceManager dpm;

    @BeforeEach
    void setUp() {
        dpm = new DocumentPersistenceManager(null);
    }


    @Test
    void serializeDeserializeAndDelete() throws URISyntaxException, IOException {
        URI uri = new URI("mailto:java-net@java.sun.com");
        Document d = new DocumentImpl(uri, "this is a test test to see if a example of this works", null);
        dpm.serialize(uri, d);
        File f = new File(System.getProperty("user.dir"), uri.toString().replace(uri.getScheme(), "").replaceAll("[!@#$%^&* <>:+=\"'`]", "") + ".json");
        assertTrue(f.exists());
        assertEquals(d, dpm.deserialize(uri));
        assertTrue(dpm.delete(uri));
        assertFalse(f.exists());
    }

    @Test
    void deleteNonexistentURI() throws URISyntaxException, IOException {
        assertFalse(dpm.delete(new URI("testuri:foo")));
    }
}