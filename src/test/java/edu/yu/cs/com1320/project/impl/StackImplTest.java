package edu.yu.cs.com1320.project.impl;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

class StackImplTest {

    StackImpl<Integer> stack;

    @BeforeEach
    void setUp() {
        stack = new StackImpl<>();
    }

    @Test
    void push() {
        stack.push(2);
        stack.push(3);
        assertEquals(2, stack.size());
        assertEquals(stack.peek(), 3);
    }

    @Test
    void pop() {
        assertNull(stack.pop());
        stack.push(4);
        assertEquals(4, stack.pop());
        assertNull(stack.peek());
        stack.push(4);
        stack.push(67);
        assertEquals(67, stack.pop());
        assertEquals(1, stack.size());
        assertEquals(4, stack.peek());
    }

    @Test
    void peek() {
        assertNull(stack.peek());
        stack.push(1);
        stack.push(987);
        assertEquals(987, stack.peek());
        assertEquals(2, stack.size());
    }

    @Test
    void size() {
        assertEquals(stack.size(), 0);
        stack.push(2);
        assertEquals(stack.size(), 1);
        stack.push(3);
        stack.push(6);
        stack.push(8);
        stack.push(1);
        stack.push(987);
        stack.push(54);
        stack.push(12);
        stack.push(67);
        stack.push(0);
        assertEquals(stack.size(), 10);
        assertEquals(0, stack.pop());
        assertEquals(9, stack.size());
    }
}