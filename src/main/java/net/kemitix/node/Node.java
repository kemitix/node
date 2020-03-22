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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An interface for tree node items.
 *
 * @param <T> the type of data held in each node
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public interface Node<T> {

    /**
     * Fetch the name of the node. Where a node's name is determined via a name
     * supplier, the name may be regenerated each time this method is called.
     *
     * @return the name of the node
     */
    String getName();

    /**
     * Sets the explicit name for a node. Setting the name to null will clear
     * the name and revert to the parent's name supplier.
     *
     * @param name the new name
     */
    void setName(String name);

    /**
     * Fetch the data held within the node.
     *
     * @return an Optional containing the node's data, or empty if the node has none
     */
    Optional<T> findData();

    /**
     * Set the data held within the node.
     *
     * @param data the node's data
     */
    void setData(T data);

    /**
     * Returns true if the node is empty (has no data).
     *
     * @return true is data is null
     */
    boolean isEmpty();

    /**
     * Returns true is the node is a root node (has no parent).
     *
     * @return true is parent is null
     */
    boolean isRoot();

    /**
     * Fetch the parent node.
     *
     * @return an Optional contain the parent node, or empty if a root node
     */
    Optional<Node<T>> findParent();

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
     * Creates a new unnamed node and adds it as a child of the current node.
     *
     * @param child the child node's data
     *
     * @return the new child node
     */
    Node<T> createChild(T child);

    /**
     * Creates a new named node and adds it as a child of the current node.
     *
     * @param child the child node's data
     * @param name  the name
     *
     * @return the new child node
     */
    Node<T> createChild(T child, String name);

    /**
     * Populates the tree with the path of nodes, each being a child of the
     * previous node in the path.
     *
     * @param descendants the line of descendants from the current node
     */
    void createDescendantLine(List<T> descendants);

    /**
     * Fetches the node for the child if present.
     *
     * @param child the child's data to search for
     *
     * @return an {@link Optional} containing the child node if found
     */
    Optional<Node<T>> findChild(T child);

    /**
     * Checks if the node is an ancestor.
     *
     * @param node the potential ancestor
     *
     * @return true if the node is an ancestor
     */
    boolean isDescendantOf(Node<T> node);

    /**
     * Walks the node tree using the path to select each child.
     *
     * @param path the path to the desired child
     *
     * @return the child or null
     */
    Optional<Node<T>> findInPath(List<T> path);

    /**
     * Places the node in the tree under by the path. Intervening empty
     * nodes are created as needed.
     *
     * @param node the node to place
     * @param path the path to contain the new node
     */
    void insertInPath(Node<T> node, String... path);

    /**
     * Searches for a child with the name given.
     *
     * @param name the name of the child
     *
     * @return an Optional containing the child found or empty
     */
    Optional<Node<T>> findChildByName(String name);

    /**
     * Returns true if the Node has a name. Where a name supplier is used, the
     * generated name is used.
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

    /**
     * Returns a stream containing the node and all its children and their children.
     *
     * @return a stream of all the nodes in the tree below this node
     */
    Stream<Node<T>> stream();

    /**
     * Returns a stream of all the node's ancestor nodes.
     *
     * @return a stream of the node's parents recursively until the root node
     */
    Stream<Node<T>> parentStream();
}
