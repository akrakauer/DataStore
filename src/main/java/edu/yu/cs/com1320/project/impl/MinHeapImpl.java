package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {
    public MinHeapImpl() {
        this.elements = (E[]) new Comparable[2];
    }

    @Override
    public void reHeapify(E element) {
        if (element == null) {
            throw new NoSuchElementException();
        }
        int i = this.getArrayIndex(element);
        this.downHeap(i);
        this.upHeap(i);
    }

    @Override
    protected int getArrayIndex(E element) {
        for (int i = 1; i <= this.count; i++) {
            if (element.equals(this.elements[i])) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    protected void doubleArraySize() {
        E[] temp = this.elements;
        this.elements = (E[]) new Comparable[temp.length * 2];
        System.arraycopy(temp, 0, this.elements, 0, temp.length);
    }
}
