package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {
    private int top = 0;
    private Object[] stack = new Object[6];

    @Override
    public void push(T element) {
        if (this.top == this.stack.length - 1) {
            Object[] temp = this.stack;
            this.stack = new Object[temp.length * 2];
            System.arraycopy(temp, 0, this.stack, 0, temp.length);
        }
        this.stack[this.top] = element;
        this.top++;
    }

    @Override
    public T pop() {
        if (this.top == 0) {
            return null;
        }
        T remove = (T)this.stack[this.top - 1];
        this.stack[--this.top] = null;
        return remove;
    }

    @Override
    public T peek() {
        return this.top == 0 ? null : (T)this.stack[top - 1];
    }

    @Override
    public int size() {
        return this.top;
    }
}