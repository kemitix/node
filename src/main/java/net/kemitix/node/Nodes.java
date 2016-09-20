/*
The MIT License (MIT)

Copyright (c) 2016 Paul Campbell

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package net.kemitix.node;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for {@link Node} items.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public final class Nodes {

    private Nodes() {
    }

    /**
     * Creates a new unnamed root node.
     *
     * @param data the data the node will contain
     * @param <T>  the type of the data
     *
     * @return the new node
     */
    public static <T> Node<T> unnamedRoot(final T data) {
        return new NodeItem<>(data, "", null, new HashSet<>());
    }

    /**
     * Creates a new named root node.
     *
     * @param data the data the node will contain
     * @param name the name of the node
     * @param <T>  the type of the data
     *
     * @return the new node
     */
    public static <T> Node<T> namedRoot(final T data, final String name) {
        return new NodeItem<>(data, name, null, new HashSet<>());
    }

    /**
     * Creates a new unnamed child node.
     *
     * @param data   the data the node will contain
     * @param parent the parent of the node
     * @param <T>    the type of the data
     *
     * @return the new node
     */
    public static <T> Node<T> unnamedChild(final T data, final Node<T> parent) {
        return new NodeItem<>(data, "", parent, new HashSet<>());
    }

    /**
     * Creates a new named child node.
     *
     * @param data   the data the node will contain
     * @param name   the name of the node
     * @param parent the parent of the node
     * @param <T>    the type of the data
     *
     * @return the new node
     */
    public static <T> Node<T> namedChild(
            final T data, final String name, final Node<T> parent
                                        ) {
        return new NodeItem<>(data, name, parent, new HashSet<>());
    }

    /**
     * Creates an immutable copy of an existing node tree.
     *
     * @param root the root node of the source tree
     * @param <T>  the type of the data
     *
     * @return the immutable copy of the tree
     */
    public static <T> Node<T> asImmutable(final Node<T> root) {
        if (root.getParent()
                .isPresent()) {
            throw new IllegalArgumentException("source must be the root node");
        }
        final Set<Node<T>> children = getImmutableChildren(root);
        return ImmutableNodeItem.newRoot(root.getData()
                                             .orElse(null), root.getName(), children);
    }

    private static <T> Set<Node<T>> getImmutableChildren(final Node<T> source) {
        return source.getChildren()
                     .stream()
                     .map(Nodes::asImmutableChild)
                     .collect(Collectors.toSet());
    }

    private static <T> Node<T> asImmutableChild(
            final Node<T> source
                                               ) {
        return ImmutableNodeItem.newChild(source.getData()
                                                .orElse(null), source.getName(), source.getParent()
                                                                                       .orElse(null),
                                          getImmutableChildren(source)
                                         );
    }

}
