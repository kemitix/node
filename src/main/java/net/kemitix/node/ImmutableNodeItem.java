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

import java.util.List;
import java.util.Set;

/**
 * Represents an immutable tree of nodes.
 *
 * <p>Due to the use of generics the data within a node may not be immutable.
 * (We can't create a defensive copy.) So if a user were to use {@code
 * getData()} they could then modify the original data within the node. This
 * wouldn't affect the integrity of the node tree structure, however.</p>
 *
 * @param <T> the type of data stored in each node
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
final class ImmutableNodeItem<T> extends NodeItem<T> {

    private static final String IMMUTABLE_OBJECT = "Immutable object";

    private ImmutableNodeItem(
            final T data, final String name, final Node<T> parent, final Set<Node<T>> children
                             ) {
        super(data, name, null, children);
        forceParent(parent);
    }

    /**
     * Creates a new immutable root node.
     *
     * @param data     the data of the node
     * @param name     the name of the node
     * @param children the children of the node
     * @param <T>      the type of the data in the node
     *
     * @return the new node tree's root node
     */
    static <T> ImmutableNodeItem<T> newRoot(
            final T data, final String name, final Set<Node<T>> children
                                           ) {
        return new ImmutableNodeItem<>(data, name, null, children);
    }

    /**
     * Creates a new immutable subtree from this child.
     *
     * @param data     the data of the node
     * @param name     the name of the node
     * @param parent   the mutable parent of the node
     * @param children the children of the node
     * @param <T>      the type of the data in the node
     *
     * @return the new immutable node
     */
    static <T> ImmutableNodeItem<T> newChild(
            final T data, final String name, final Node<T> parent, final Set<Node<T>> children
                                            ) {
        return new ImmutableNodeItem<>(data, name, parent, children);
    }

    @Override
    public void setName(final String name) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void setData(final T data) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void setParent(final Node<T> parent) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void addChild(final Node<T> child) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public Node<T> createChild(final T child) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public Node<T> createChild(final T child, final String name) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void createDescendantLine(final List<T> descendants) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void insertInPath(final Node<T> node, final String... path) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void removeChild(final Node<T> node) {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }

    @Override
    public void removeParent() {
        throw new UnsupportedOperationException(IMMUTABLE_OBJECT);
    }
}
