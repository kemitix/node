package net.kemitix.node;

import lombok.NonNull;
import lombok.val;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents a tree of nodes.
 *
 * @author Paul Campbell
 *
 * @param <T> the type of data stored in each node
 */
class NodeItem<T> implements Node<T> {

    private T data;

    private final Set<Node<T>> children = new HashSet<>();

    private Function<Node<T>, String> nameSupplier;

    private Node<T> parent;

    private String name;

    /**
     * Create named root node.
     *
     * @param data the data or null
     * @param name the name
     */
    NodeItem(final T data, final String name) {
        this(data);
        this.name = name;
    }

    /**
     * Create unnamed root node.
     *
     * @param data the data or null
     */
    NodeItem(final T data) {
        this.data = data;
        this.nameSupplier = (n) -> null;
    }

    /**
     * Creates a node with a parent.
     *
     * @param data   the data or null
     * @param parent the parent node
     */
    NodeItem(final T data, final Node<T> parent) {
        this.data = data;
        setParent(parent);
    }

    /**
     * Creates a named node with a parent.
     *
     * @param data   the data or null
     * @param name   the name
     * @param parent the parent node
     */
    NodeItem(final T data, final String name, final Node<T> parent) {
        this.data = data;
        this.name = name;
        setParent(parent);
    }

    private String generateName() {
        return getNameSupplier().apply(this);
    }

    private Function<Node<T>, String> getNameSupplier() {
        if (nameSupplier != null) {
            return nameSupplier;
        }
        // no test for parent as root nodes will always have a default name
        // supplier
        return ((NodeItem<T>) parent).getNameSupplier();
    }

    @Override
    public String getName() {
        if (name == null) {
            return generateName();
        }
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Optional<T> getData() {
        return Optional.ofNullable(data);
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
    public Optional<Node<T>> getParent() {
        return Optional.ofNullable(parent);
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
        verifyChildWithSameNameDoesNotAlreadyExist(child);
        children.add(child);
        // update the child's parent if they don't have one or it is not this
        val childParent = child.getParent();
        if (!childParent.isPresent() || !childParent.get().equals(this)) {
            child.setParent(this);
        }
    }

    private void verifyChildWithSameNameDoesNotAlreadyExist(
            final @NonNull Node<T> child) {
        if (child.isNamed()) {
            findChildByName(child.getName())
                    .filter(existingChild -> existingChild != child)
                    .ifPresent(existingChild -> {
                        throw new NodeException(
                                "Node with that name already exists here");
                    });
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
        return new NodeItem<>(child, this);
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
            findOrCreateChild(descendants.get(0)).createDescendantLine(
                    descendants.subList(1, descendants.size()));
        }
    }

    /**
     * Looks for a child node and returns it, creating a new child node if one
     * isn't found.
     *
     * @param child the child's data to search or create with
     *
     * @return the found or created child node
     *
     * @deprecated use node.findChild(child).orElseGet(() -> node.createChild
     * (child));
     */
    @Override
    @Deprecated
    public Node<T> findOrCreateChild(@NonNull final T child) {
        return findChild(child).orElseGet(() -> createChild(child));
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
        return children.stream().filter(node -> {
            final Optional<T> d = node.getData();
            return d.isPresent() && d.get().equals(child);
        }).findAny();
    }

    @Override
    public Node<T> getChild(final T child) {
        Optional<Node<T>> optional = findChild(child);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw new NodeException("Child not found");
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
        return parent != null && (node.equals(parent) || parent.isDescendantOf(
                node));
    }

    /**
     * Make the current node a direct child of the parent.
     *
     * @param parent the new parent node
     */
    @Override
    public final void setParent(@NonNull final Node<T> parent) {
        if (this.equals(parent) || parent.isDescendantOf(this)) {
            throw new NodeException("Parent is a descendant");
        }
        if (this.parent != null) {
            this.parent.getChildren().remove(this);
        }
        this.parent = parent;
        parent.addChild(this);
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
        for (T item : path) {
            final Optional<Node<T>> child = current.findChild(item);
            if (child.isPresent()) {
                current = child.get();
            } else {
                current = null;
                break;
            }
        }
        return Optional.ofNullable(current);
    }

    @Override
    public void insertInPath(final Node<T> nodeItem, final String... path) {
        if (path.length == 0) {
            insertChild(nodeItem);
        } else {
            val item = path[0];
            findChildByName(item)
                    .orElseGet(() -> new NodeItem<>(null, item, this))
                    .insertInPath(nodeItem,
                            Arrays.copyOfRange(path, 1, path.length));
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
                nodeItem.getData().ifPresent(existing::setData);
            } else {
                throw new NodeException("A non-empty node named '"
                        + nodeItem.getName() + "' already exists here");
            }
        } else {
            // nothing with the same name exists
            addChild(nodeItem);
        }
    }

    @Override
    public Optional<Node<T>> findChildByName(@NonNull final String named) {
        return children.stream()
                .filter((Node<T> t) -> t.getName().equals(named))
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
            Function<Node<T>, String> supplier = getNameSupplier();
            parent = null;
            oldParent.removeChild(this);
            if (this.nameSupplier == null) {
                // this is now a root node, so must provide a default name
                // supplier
                this.nameSupplier = supplier;
            }
        }
    }

}
