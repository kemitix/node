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

import lombok.NonNull;
import lombok.val;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.kemitix.node.HeadTail.head;
import static net.kemitix.node.HeadTail.tail;

/**
 * Represents a tree of nodes.
 *
 * @param <T> the type of data stored in each node
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
@SuppressWarnings("methodcount")
class NodeItem<T> implements Node<T> {

    private final Set<Node<T>> children = new HashSet<>();

    private T data;

    private Node<T> parent;

    private String name;

    /**
     * Constructor.
     *
     * @param data     the data of the node
     * @param name     the name of the node
     * @param parent   the parent of the node, or null for a root node
     * @param children the children of the node - must not be null
     */
    NodeItem(final T data, final String name, final Node<T> parent, @NonNull final Set<Node<T>> children) {
        this.data = data;
        this.name = name;
        if (parent != null) {
            doSetParent(parent);
        }
        this.children.addAll(children);
    }

    /**
     * Sets the parent of a node without updating the parent in the process as {@link #setParent(Node)} does.
     *
     * @param newParent The new parent node
     */
    void forceParent(final Node<T> newParent) {
        this.parent = newParent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Optional<T> findData() {
        return Optional.ofNullable(data);
    }

    @Override
    public T getData() {
        if (isEmpty()) {
            throw new EmptyNodeException(getName());
        }
        return data;
    }

    @Override
    public void setData(final T data) {
        this.data = data;
    }

    @Override
    public boolean isEmpty() {
        return data == null;
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public Optional<Node<T>> findParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Node<T> getParent() {
        if (parent == null) {
            throw new OrphanedNodeException(getName());
        }
        return parent;
    }

    /**
     * Make the current node a direct child of the parent.
     *
     * @param parent the new parent node
     */
    @Override
    public void setParent(@NonNull final Node<T> parent) {
        doSetParent(parent);
    }

    @SuppressWarnings("npathcomplexity")
    private void doSetParent(@NonNull final Node<T> newParent) {
        if (this.equals(newParent) || newParent.isDescendantOf(this)) {
            throw new NodeException("Parent is a descendant");
        }
        if (this.parent != null) {
            this.parent.getChildren()
                       .remove(this);
        }
        this.parent = newParent;
        newParent.addChild(this);
    }

    @Override
    public Set<Node<T>> getChildren() {
        return children;
    }

    /**
     * Adds the child to the node.
     *
     * @param child the node to add
     */
    @Override
    public void addChild(@NonNull final Node<T> child) {
        verifyChildIsNotAnAncestor(child);
        //verifyChildWithSameNameDoesNotAlreadyExist
        if (child.isNamed()) {
            findChildByName(child.getName()).filter(existingChild -> existingChild != child)
                                            .ifPresent(existingChild -> {
                                                throw new NodeException("Node with that name already exists here");
                                            });
        }
        children.add(child);
        // update the child's parent if they don't have one or it is not this
        if (!child.findParent()
                  .filter(this::equals)
                  .isPresent()) {
            child.setParent(this);
        }
    }

    private void verifyChildIsNotAnAncestor(final @NonNull Node<T> child) {
        if (this.equals(child) || isDescendantOf(child)) {
            throw new NodeException("Child is an ancestor");
        }
    }

    /**
     * Creates a new node and adds it as a child of the current node.
     *
     * @param child the child node's data
     *
     * @return the new child node
     */
    @Override
    public Node<T> createChild(@NonNull final T child) {
        return new NodeItem<>(child, "", this, new HashSet<>());
    }

    @Override
    @SuppressWarnings("hiddenfield")
    public Node<T> createChild(final T child, final String name) {
        Node<T> node = createChild(child);
        node.setName(name);
        return node;
    }

    /**
     * Populates the tree with the path of nodes, each being a child of the
     * previous node in the path.
     *
     * @param descendants the line of descendants from the current node
     */
    @Override
    public void createDescendantLine(@NonNull final List<T> descendants) {
        if (!descendants.isEmpty()) {
            val child = descendants.get(0);
            val remainingLine = descendants.subList(1, descendants.size());
            findChild(child).orElseGet(() -> createChild(child))
                            .createDescendantLine(remainingLine);
        }
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
                .filter(node ->
                        child.equals(
                                node.findData()
                                        .orElse(null)))
                .findFirst();
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
        return head(path)
                .flatMap(this::findChild)
                .flatMap(findInChildsPath(tail(path)));
    }

    private Function<Node<T>, Optional<Node<T>>> findInChildsPath(final List<T> path) {
        return child -> {
            if (path.isEmpty()) {
                return Optional.of(child);
            }
            return child.findInPath(path);
        };
    }

    @Override
    public void insertInPath(final Node<T> nodeItem, final String... path) {
        if (path.length == 0) {
            insertChild(nodeItem);
        } else {
            val nextInPath = path[0];
            findChildByName(nextInPath).orElseGet(() -> new NodeItem<>(null, nextInPath, this, new HashSet<>()))
                                       .insertInPath(nodeItem, Arrays.copyOfRange(path, 1, path.length));
        }
    }

    private void insertChild(final Node<T> nodeItem) {
        if (nodeItem.isNamed()) {
            insertNamedChild(nodeItem);
        } else {
            // nothing to conflict with
            addChild(nodeItem);
        }
    }

    private void insertNamedChild(final Node<T> nodeItem) {
        val childByName = findChildByName(nodeItem.getName());
        if (childByName.isPresent()) {
            // we have an existing node with the same name
            val existing = childByName.get();
            if (existing.isEmpty()) {
                // place any data in the new node into the existing empty node
                nodeItem.findData()
                        .ifPresent(existing::setData);
            } else {
                throw new NodeException("A non-empty node named '" + nodeItem.getName() + "' already exists here");
            }
        } else {
            // nothing with the same name exists
            addChild(nodeItem);
        }
    }

    @Override
    public Optional<Node<T>> findChildByName(@NonNull final String named) {
        return children.stream()
                       .filter((Node<T> t) -> t.getName()
                                               .equals(named))
                       .findAny();
    }

    @Override
    public Node<T> getChildByName(final String named) {
        final Optional<Node<T>> optional = findChildByName(named);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new NodeException("Named child not found");
    }

    @Override
    @SuppressWarnings("movevariableinsideif")
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
        String currentName = getName();
        return currentName != null && currentName.length() > 0;
    }

    @Override
    public void removeChild(final Node<T> node) {
        if (children.remove(node)) {
            node.removeParent();
        }
    }

    @Override
    public void removeParent() {
        if (parent != null) {
            Node<T> oldParent = parent;
            parent = null;
            oldParent.removeChild(this);
        }
    }

    @Override
    public Stream<Node<T>> stream() {
        return Stream.concat(Stream.of(this), getChildren().stream()
                                                           .flatMap(Node::stream));
    }

    @Override
    public Stream<Node<T>> parentStream() {
        return findParent().map(node -> Stream.concat(Stream.of(node), node.parentStream()))
                           .orElseGet(Stream::empty);
    }
}
