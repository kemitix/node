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

import lombok.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * An abstract node item, providing default implementations for most read-only
 * operations.
 *
 * @param <T> the type of data stored in each node
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
abstract class AbstractNodeItem<T> implements Node<T> {

    private final Set<Node<T>> children;

    private T data;

    private String name;

    private Node<T> parent;

    /**
     * Constructor.
     *
     * @param data     the data of the node
     * @param name     the name of the node
     * @param parent   the parent of the node, or null for a root node
     * @param children the children of the node - must not be null
     */
    protected AbstractNodeItem(
            final T data, final String name, final Node<T> parent, @NonNull final Set<Node<T>> children
                              ) {
        this.data = data;
        this.name = name;
        this.parent = parent;
        this.children = children;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }

    @Override
    public boolean isEmpty() {
        return data == null;
    }

    @Override
    public Optional<Node<T>> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Set<Node<T>> getChildren() {
        return new HashSet<>(children);
    }

    /**
     * Fetches the node for the child if present.
     *
     * @param child the child's data to search for
     *
     * @return an {@link Optional} containing the child node if found
     */
    @Override
    public Optional<Node<T>> findChild(@NonNull final T child) {
        return children.stream()
                       .filter(node -> {
                           final Optional<T> d = node.getData();
                           return d.isPresent() && d.get()
                                                    .equals(child);
                       })
                       .findAny();
    }

    @Override
    public Node<T> getChild(final T child) {
        return findChild(child).orElseThrow(() -> new NodeException("Child not found"));
    }

    /**
     * Checks if the node is an ancestor.
     *
     * @param node the potential ancestor
     *
     * @return true if the node is an ancestor
     */
    @Override
    public boolean isDescendantOf(final Node<T> node) {
        return parent != null && (node.equals(parent) || parent.isDescendantOf(node));
    }

    /**
     * Walks the node tree using the path to select each child.
     *
     * @param path the path to the desired child
     *
     * @return the child or null
     */
    @Override
    public Optional<Node<T>> findInPath(@NonNull final List<T> path) {
        if (path.isEmpty()) {
            return Optional.empty();
        }
        Node<T> current = this;
        for (int i = 0, pathSize = path.size(); i < pathSize && current != null; i++) {
            current = current.findChild(path.get(i))
                             .orElse(null);
        }
        return Optional.ofNullable(current);
    }

    @Override
    public Optional<Node<T>> findChildByName(@NonNull final String named) {
        return children.stream()
                       .filter(n -> n.getName()
                                     .equals(named))
                       .findAny();
    }

    @Override
    public Node<T> getChildByName(final String named) {
        return findChildByName(named).orElseThrow(() -> new NodeException("Named child not found"));
    }

    @Override
    public String drawTree(final int depth) {
        final StringBuilder sb = new StringBuilder();
        final String unnamed = "(unnamed)";
        if (isNamed()) {
            sb.append(formatByDepth(name, depth));
        } else if (!children.isEmpty()) {
            sb.append(formatByDepth(unnamed, depth));
        }
        getChildren().forEach(c -> sb.append(c.drawTree(depth + 1)));
        return sb.toString();
    }

    private String formatByDepth(final String value, final int depth) {
        return String.format("[%1$" + (depth + value.length()) + "s]\n", value);
    }

    @Override
    public boolean isNamed() {
        return name != null && name.length() > 0;
    }
}
