package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DocumentImpl implements Document {
    private byte[] binaryData = null;
    private final URI uri;
    private String text = null;
    private Map<String,Integer> words = new HashMap<>();
    private long lastUsedTime = 0;

    public DocumentImpl(URI uri, String text, Map<String,Integer> wordCountMap) {
        if (uri == null || text == null || text.equals("")) {
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.text = text;
        if (wordCountMap != null) {
            this.words = wordCountMap;
        } else {
            String[] wordsArray = text.split(" ");
            for (String word : wordsArray) {
                word = word.replaceAll("[^\\p{L}\\p{Nd}]+", "");
                if (!word.equals("")) {
                    Integer count = this.words.get(word);
                    this.words.put(word, count == null ? 1 : count + 1);
                }
            }
        }
    }

    public DocumentImpl(URI uri, byte[] binaryData) {
        if (uri == null || binaryData == null || binaryData.length == 0) {
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.binaryData = binaryData;
    }

    @Override
    public String getDocumentTxt() {
        return this.text;
    }

    @Override
    public byte[] getDocumentBinaryData() {
        return this.binaryData;
    }

    @Override
    public URI getKey() {
        return this.uri;
    }

    @Override
    public int wordCount(String word) {
        return this.words.get(word) == null ? 0 : this.words.get(word);
    }

    @Override
    public Set<String> getWords() {
        return this.words.keySet();
    }

    @Override
    public long getLastUseTime() {
        return this.lastUsedTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.lastUsedTime = timeInNanoseconds;
    }

    @Override
    public Map<String, Integer> getWordMap() {
        return new HashMap<>(this.words);
    }

    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        this.words = wordMap;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DocumentImpl d)) {
            return false;
        }
        return d.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }

    @Override
    public int compareTo(Document d) {
        return Long.compare(this.lastUsedTime, d.getLastUseTime());
    }
}