package net.kemitix.node;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * An interface for tree node items.
 *
 * @param <T> the type of data held in each node
 *
 * @author pcampbell
 */
public interface Node<T> {

    /**
     * Fetch the name of the node.
     *
     * @return the name of the node
     */
    String getName();

    /**
     * Sets the explicit name for a node.
     *
     * @param name the new name
     */
    void setName(String name);

    /**
     * Fetch the data held within the node.
     *
     * @return the node's data
     */
    T getData();

    /**
     * Returns true if the node is empty (has no data).
     *
     * @return true is data is null
     */
    boolean isEmpty();

    /**
     * Fetch the parent node.
     * <p>
     * If the node is a root node, i.e. has no parent, then this will return
     * null.
     *
     * @return the parent node
     */
    Node<T> getParent();

    /**
     * Make the current node a direct child of the parent.
     *
     * @param parent the new parent node
     */
    void setParent(Node<T> parent);

    /**
     * Fetches the child nodes.
     *
     * @return the set of child nodes
     */
    Set<Node<T>> getChildren();

    /**
     * Adds the child to the node.
     *
     * @param child the node to add
     */
    void addChild(Node<T> child);

    /**
     * Creates a new node and adds it as a child of the current node.
     *
     * @param child the child node's data
     *
     * @return the new child node
     */
    Node<T> createChild(T child);

    /**
     * Populates the tree with the path of nodes, each being a child of the
     * previous node in the path.
     *
     * @param descendants the line of descendants from the current node
     */
    void createDescendantLine(List<T> descendants);

    /**
     * Looks for a child node and returns it, creating a new child node if one
     * isn't found.
     *
     * @param child the child's data to search or create with
     *
     * @return the found or created child node
     */
    Node<T> findOrCreateChild(T child);

    /**
     * Fetches the node for the child if present.
     *
     * @param child the child's data to search for
     *
     * @return an {@link Optional} containing the child node if found
     */
    Optional<Node<T>> getChild(T child);

    /**
     * Checks if the node is an ancestor.
     *
     * @param node the potential ancestor
     *
     * @return true if the node is an ancestor
     */
    boolean isChildOf(Node<T> node);

    /**
     * Walks the node tree using the path to select each child.
     *
     * @param path the path to the desired child
     *
     * @return the child or null
     */
    Optional<Node<T>> walkTree(List<T> path);

    /**
     * Places the node in the tree under by the path. Intervening empty
     * nodes are created as needed.
     *
     * @param node the node to place
     * @param path the path to contain the new node
     */
    void placeNodeIn(Node<T> node, String... path);

    /**
     * Searches for a child with the name given.
     *
     * @param name the name of the child
     *
     * @return an Optional containing the child found or empty
     */
    Optional<Node<T>> findChildNamed(String name);

    /**
     * Returns the child with the given name. If one can't be found a
     * NodeException is thrown.
     *
     * @param name the name of the child
     *
     * @return the node
     */
    Node<T> getChildNamed(String name);

    /**
     * Draw a representation of the tree.
     *
     * @param depth current depth for recursion
     *
     * @return a representation of the tree
     */
    String drawTree(int depth);

    /**
     * Returns true if the Node has a name.
     *
     * @return true if the node has a name
     */
    boolean isNamed();

    /**
     * Remove the node from the children.
     *
     * @param node the node to be removed
     */
    void removeChild(Node<T> node);

    /**
     * Removes the parent from the node. Makes the node into a new root node.
     */
    void removeParent();
}
