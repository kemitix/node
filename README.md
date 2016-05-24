# node

[![Join the chat at https://gitter.im/kemitix/node](https://badges.gitter.im/kemitix/node.svg)](https://gitter.im/kemitix/node?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
A parent/children data structure

# Usage

Add as a dependency in your `pom.xml`:

    <dependency>
        <groupId>net.kemitix</groupId>
        <artifactId>node</artifactId>
        <version>${node.version}</version>
    </dependency>

The library consits of an interface `Node` and an implementation `NodeItem`.

## Create a root node

    Node<String> root = new NodeItem<>("[root]");

## Get the contents of the node

    String rootData = root.getData(); // returns "[root]"

## Add a child node

    Node<String> child = root.createChild("child");

Which is shorthand for:

    Node<String> child = new NodeItem<>("child");
    root.addChild(child);

The tree now looks like:

    "[root]"
    \-> "child"

## Get the child node

    Node<String> childNode = root.getChild("child");

## Create a chain of nodes

    root.createDescendantLine(Arrays.asList("alpha", "beta", "gamma"));

    "[root]"
    |-> "child"
    \-> "alpha"
        \-> "beta"
            \-> "gamma"

## Walk the tree to find a node

    Optional<Node<String>> foundNode = root.walkTree(Arrays.asList(
        "alpha", "beta", "gamma"));
    if (foundNode.isPresent()) {
        String betaData = foundNode.get().getParent().getData();
            // returns "beta"
    }

## Get all children of a node

    Set<Node<String>> children = root.getChildren();
    children.size(); // returns 2 ("child" and "alpha")
