package edu.yu.cs.com1320.project.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TrieImplTest {
    TrieImpl<Integer> t = null;

    @BeforeEach
    void setUp() {
        t = new TrieImpl<>();
    }

    @Test
    void put() {
        t.put("Two", 2);
        t.put("two", 2);
        t.put("Two", 22);
        List<Integer> test = new ArrayList<>();
        test.add(2);
        test.add(22);
        assertEquals(test, t.getAllSorted("Two", Comparator.comparingInt(o -> o)));
        assertThrows(IllegalArgumentException.class, () -> t.put("don't", 9));
    }

    @Test
    void getAllSorted() {
        t.put("Two", 2);
        t.put("two", 2);
        t.put("Two", 22);
        t.put("Twonty", 6);
        List<Integer> test = new ArrayList<>();
        test.add(2);
        test.add(22);
        assertEquals(test, t.getAllSorted("Two", Comparator.comparingInt(o -> o)));
    }

    @Test
    void getAllWithPrefixSorted() {
        t.put("sheep", 1);
        t.put("shells", 2);
        t.put("shells", 22);
        t.put("shell", 3);
        t.put("she", 7);
        t.put("shape", 4);
        t.put("shapes", 4);
        t.put("street", 5);
        t.put("steel", 56);
        t.put("s", 98);
        List<Integer> results = new ArrayList<>();
        results.add(1);
        results.add(2);
        results.add(3);
        results.add(4);
        results.add(5);
        results.add(7);
        results.add(22);
        results.add(56);
        results.add(98);
        assertEquals(results, t.getAllWithPrefixSorted("s", Comparator.comparingInt(o -> o)));
    }

    @Test
    void deleteAllWithPrefix() {
        //manually check with debugger to see if nodes are all gone
        t.put("thsheep", 1);
        t.put("thshells", 2);
        t.put("thshells", 22);
        t.put("thshell", 3);
        t.put("thshe", 7);
        t.put("thshape", 4);
        t.put("thshapes", 4);
        t.put("thstreet", 5);
        t.put("thsteel", 56);
        t.put("ths", 98);
        t.put("tree", 69);
        Set<Integer> results = new HashSet<>();
        results.add(1);
        results.add(2);
        results.add(3);
        results.add(4);
        results.add(5);
        results.add(7);
        results.add(22);
        results.add(56);
        results.add(98);
        assertEquals(results, t.deleteAllWithPrefix("ths"));
        List<Integer> getPrefixResults = t.getAllWithPrefixSorted("ths", Comparator.comparingInt(o -> o));
        for (Integer i : results) {
            assertFalse(getPrefixResults.contains(i));
        }
        assertTrue(getPrefixResults.isEmpty());
    }

    @Test
    void deleteAll() {
        //manually check if deleted using debugger
        t.put("sheep", 1);
        t.put("shells", 2);
        t.put("shells", 22);
        t.put("shell", 3);
        Set<Integer> results = new HashSet<>(t.getAllSorted("shells", Comparator.comparingInt(o -> o)));
        Set<Integer> deleted = t.deleteAll("shells");
        assertEquals(results, deleted);
        assertEquals(1, t.getAllSorted("sheep", Comparator.comparingInt(o -> o)).get(0));
        assertEquals(3, t.getAllSorted("shell", Comparator.comparingInt(o -> o)).get(0));
    }

    @Test
    void delete() {
        //manually check if deleted using debugger
        t.put("sheep", 1);
        t.put("shells", 2);
        t.put("shell", 3);
        assertEquals(2, t.delete("shells", 2));
        List<Integer> empty = new ArrayList<>();
        assertEquals(empty, t.getAllSorted("shells", Comparator.comparingInt(o -> o)));
        assertEquals(1, t.getAllSorted("sheep", Comparator.comparingInt(o -> o)).get(0));
        assertEquals(3, t.getAllSorted("shell", Comparator.comparingInt(o -> o)).get(0));
        assertNull((t.delete("shell", 2)));
    }
}