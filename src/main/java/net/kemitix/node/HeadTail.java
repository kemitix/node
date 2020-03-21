/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Paul Campbell
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.kemitix.node;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides a simple implementation of head() and tail() for {@link List}s.
 */
public interface HeadTail {

    /**
     * Returns the first item in the list as an {@link Optional}.
     *
     * @param list the list
     * @param <X> the type of the lists contents
     * @return an Optional containing the first item in the list, or empty if
     * the list is empty.
     */
    static <X> Optional<X> head(final List<X> list) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(0));
    }

    /**
     * Returns the list, minus the first item.
     *
     * @param list the list
     * @param <X> the type of the lists contents
     * @return a view of the list starting with the second item, or an empty
     * list if the original list has less than two items.
     */
    static <X> List<X> tail(final List<X> list) {
        if (list.size() < 1) {
            return Collections.emptyList();
        }
        return list.subList(1, list.size());
    }
}
