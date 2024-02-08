/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2024, Rambo Industries
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package br.com.ramboindustries.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
/**
 * This class is not thread safe, any race condition checker must be at client code
 */
public class ByteList
{

    private byte[] elementData;
    private int index;

    /**
     * Every array grow, increments this variable by 10.
     * So, we just increase big on the initial new allocations.
     * Does not seem right to increase 50% every time ... Like JDK implementation does ...
     * So, when shiftOperations are less than 200, we shift by 1(elementData.length >> (shiftOperations / 100))
     * When, shift operations are greater than 200 and less than 300, we shift by 2(elementData.length >> (shiftOperations / 100))
     */
    private int shiftOperations = 100;

    /**
     * Default size is 256 Bytes
     */
    private static final int DEFAULT_SIZE = 256;

    public ByteList(final int size)
    {
        elementData = new byte[size];
        index = 0;
    }

    private int growToSize(final int minimumSize)
    {
        final int shiftBy = shiftOperations / 100;
        final int grownSize = elementData.length + ( elementData.length >> shiftBy);

        // In case, the desired size is also bigger than the new allocation
        return Math.max(minimumSize, grownSize);
    }

    public ByteList()
    {
        this(DEFAULT_SIZE);
    }

    private void ensureCapacity(final int minimumCapacity)
    {
        final int freeSlots = elementData.length - index;

        if (freeSlots < minimumCapacity)
        {
            grow(minimumCapacity);
        }
    }

    private void grow(final int minimumCapacity)
    {
        final var newArray = Arrays.copyOf(elementData, growToSize(minimumCapacity));
        log.debug("Growing the size of the ByteList. Current size: {}, Required: {}, New Array Size:{}", elementData.length, minimumCapacity, newArray.length);
        shiftOperations += 10;
        this.elementData = newArray;
    }


    public void add(final byte b)
    {
        ensureCapacity(1);
        elementData[index++] = b;
    }

    public void merge(final byte[] other)
    {
        ensureCapacity(other.length);
        System.arraycopy(other, 0, elementData, index, other.length);
        index += other.length;
    }

    public byte[] get()
    {
        final var value = new byte[index];
        System.arraycopy(elementData, 0, value, 0, index);
        return value;
    }

}
