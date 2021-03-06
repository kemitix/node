package net.kemitix.node;

import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Test for {@link NodeItem}.
 *
 * @author pcampbell
 */
public class NodeItemTest {

    private Node<String> node;

    @Test
    public void findDataWhenFullReturnsData() {
        //given
        val data = "data";
        node = Nodes.unnamedRoot(data);
        //when
        val result = node.findData();
        //then
        assertThat(result).contains(data);
    }

    @Test
    public void findDataWhenEmptyReturnsEmptyOptional() {
        //given
        node = Nodes.unnamedRoot(null);
        //when
        val result = node.findData();
        //then
        assertThat(result).isEmpty();
    }

    @Test
    public void findParentWhenRootReturnsEmptyOptional() {
        //given
        node = Nodes.unnamedRoot(null);
        //when
        val result = node.findParent();
        //then
        assertThat(result).isEmpty();
    }

    @Test
    public void findParentWhenChildReturnsRoot() {
        //given
        val root = Nodes.unnamedRoot("root");
        node = Nodes.unnamedChild("child", root);
        //when
        val result = node.findParent();
        //then
        assertThat(result).contains(root);
    }

    @Test
    public void canCreateAnEmptyAndUnnamedNode() {
        //when
        node = Nodes.unnamedRoot(null);
        //then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(node.isEmpty())
              .as("node is empty")
              .isTrue();
        softly.assertThat(node.isNamed())
              .as("node is unnamed")
              .isFalse();
        softly.assertAll();
    }

    @Test
    public void canSetName() {
        //given
        node = Nodes.unnamedRoot(null);
        //when
        node.setName("named");
        //then
        assertThat(node.getName()).isEqualTo("named");
    }

    /**
     * Test that default node parent is null.
     */
    @Test
    public void shouldHaveNullForDefaultParent() {
        //given
        node = Nodes.unnamedRoot("data");
        //then
        assertThat(node.findParent()).as("node created without a parent has no parent")
                                     .isEmpty();
    }

    /**
     * Test that provided node parent is returned.
     */
    @Test
    public void shouldReturnNodeParent() {
        //given
        val parent = Nodes.unnamedRoot("parent");
        //when
        node = Nodes.unnamedChild("subject", parent);
        //then
        assertThat(node.findParent()).as("node created with a parent can return the parent")
                                     .contains(parent);
    }

    @Test
    public void shouldAddAsChildWhenCreatedWithParent() {
        //given
        final Node<String> root = Nodes.namedRoot("root data", "root name");
        //when
        final Node<String> child = Nodes.namedChild("child data", "child name", root);
        //then
        final Set<Node<String>> children = root.getChildren();
        assertThat(children).containsExactly(child);
    }

    /**
     * Test that setting the parent on a node where the proposed parent is a
     * child of the node throws an exception.
     */
    @Test
    public void setParentShouldThrowNodeExceptionWhenParentIsAChild() {
        //given
        node = Nodes.unnamedRoot("subject");
        val child = Nodes.unnamedChild("child", node);
        //when
        assertThatExceptionOfType(NodeException.class)
                .isThrownBy(() ->
                        node.setParent(child))
                .withMessage("Parent is a descendant");
    }

    /**
     * Test that when parent is added to created node, the created node is now a
     * child of the parent.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAddNewNodeAsChildToParent() {
        //given
        val parent = Nodes.unnamedRoot("parent");
        //when
        node = Nodes.unnamedChild("subject", parent);
        //then
        assertThat(parent.getChildren()).as(
                "when a node is created with a parent, the parent has the new" + " node among it's children")
                                        .contains(node);
    }

    /**
     * Test that we return the same parent when set.
     */
    @Test
    public void shouldReturnSetParent() {
        //given
        node = Nodes.unnamedRoot("subject");
        val parent = Nodes.unnamedRoot("parent");
        //when
        node.setParent(parent);
        //then
        assertThat(node.findParent()).as("when a node is assigned a new parent that parent can be " + "returned")
                                     .contains(parent);
    }

    /**
     * Test that we throw an exception when passed null.
     */
    @Test
    public void shouldThrowNPEWhenSetParentNull() {
        //given
        node = Nodes.unnamedRoot("subject");
        //when
        assertThatNullPointerException()
                .isThrownBy(() ->
                        node.setParent(null))
                .withMessageContaining("parent");
    }

    /**
     * Test that we throw an exceptions when attempting to node as its own
     * parent.
     */
    @Test
    public void setParentShouldThrowNodeExceptionWhenParentIsSelf() {
        //given
        node = Nodes.unnamedRoot("subject");
        //when
        assertThatExceptionOfType(NodeException.class)
                .isThrownBy(() ->
                        node.setParent(node))
                .withMessage("Parent is a descendant");
    }

    /**
     * Test that when a node with an existing parent is assigned a new parent,
     * that the old parent no longer sees it as one of its children.
     */
    @Test
    public void shouldUpdateOldParentWhenNodeSetToNewParent() {
        //given
        node = Nodes.unnamedRoot("subject");
        val child = node.createChild("child");
        val newParent = Nodes.unnamedRoot("newParent");
        //when
        child.setParent(newParent);
        //then
        assertThat(child.findParent()).as("when a node is assigned a new parent, the old parent is " + "replaced")
                                      .contains(newParent);
        assertThat(node.findChild("child")
                       .isPresent()).as(
                "when a node is assigned a new parent, the old parent no " + "longer has the node among it's children")
                                    .isFalse();
    }

    /**
     * Test that when a node is added as a child to another node, that it's
     * previous parent no longer has it as a child.
     */
    @Test
    public void shouldRemoveNodeFromOldParentWhenAddedAsChildToNewParent() {
        //given
        node = Nodes.unnamedRoot("subject");
        val child = node.createChild("child");
        val newParent = Nodes.unnamedRoot("newParent");
        //when
        newParent.addChild(child);
        //then
        assertThat(child.findParent()).as("when a node with an existing parent is added as a child " +
                                          "to another node, then the old parent is replaced")
                                      .contains(newParent);
        assertThat(node.findChild("child")
                       .isPresent()).as("when a node with an existing parent is added as a child to " +
                                        "another node, then the old parent no longer has " +
                                        "the node among it's children")
                                    .isFalse();
    }

    /**
     * Test that adding null as a child throws an exception.
     */
    @Test
    public void shouldThrowNPEWhenAddingNullAsChild() {
        //given
        node = Nodes.unnamedRoot("subject");
        //when
        assertThatNullPointerException()
                .isThrownBy(() ->
                        node.addChild(null))
                .withMessageContaining("child");
    }

    /**
     * Test that adding a child is returned.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnAddedChild() {
        //given
        node = Nodes.unnamedRoot("subject");
        val child = Nodes.unnamedRoot("child");
        //when
        node.addChild(child);
        //then
        assertThat(node.getChildren()).as("when a node is added as a child, the node is among the " + "children")
                                      .contains(child);
    }

    /**
     * Test that adding a node as it's own child throws an exception.
     */
    @Test
    public void addChildShouldThrowNodeExceptionWhenAddingANodeAsOwnChild() {
        //given
        node = Nodes.unnamedRoot("subject");
        //then
        assertThatExceptionOfType(NodeException.class)
                .isThrownBy(() ->
                        node.addChild(node))
                .withMessage("Child is an ancestor");
    }

    /**
     * Test that adding a node to itself as a child causes an exception.
     */
    @Test
    public void addChildShouldThrowNodeExceptionWhenAddingSelfAsChild() {
        //given
        node = Nodes.unnamedRoot("subject");
        //when
        assertThatExceptionOfType(NodeException.class)
                .isThrownBy(() ->
                        node.addChild(node))
                .withMessage("Child is an ancestor");
    }

    /**
     * Test that adding the parent of a node to the node as a child causes an
     * exception.
     */
    @Test
    public void addChildShouldThrowNodeExceptionWhenChildIsParent() {
        //given
        val parent = Nodes.unnamedRoot("parent");
        node = Nodes.unnamedChild("subject", parent);
        //when
        assertThatExceptionOfType(NodeException.class)
                .isThrownBy(() ->
                        node.addChild(parent))
                .withMessage("Child is an ancestor");
    }

    /**
     * Test that adding the grandparent to a node as a child causes an
     * exception.
     */
    @Test
    public void addChildShouldThrowNodeExceptionWhenAddingGrandParentAsChild() {
        //given
        val grandParent = Nodes.unnamedRoot("grandparent");
        val parent = Nodes.unnamedChild("parent", grandParent);
        node = Nodes.unnamedChild("subject", parent);
        //when
        assertThatExceptionOfType(NodeException.class)
                .isThrownBy(() ->
                        node.addChild(grandParent))
                .withMessage("Child is an ancestor");
    }

    /**
     * Test that adding a child to a node, sets the child's parent node.
     */
    @Test
    public void shouldSetParentOnChildWhenAddedAsChild() {
        //given
        node = Nodes.unnamedRoot("subject");
        val child = Nodes.unnamedRoot("child");
        //when
        node.addChild(child);
        //then
        assertThat(child.findParent()).as("when a node is added as a child, the child has the node as " + "its parent")
                .contains(node);
    }

    @Nested
    @DisplayName("findInPath")
    public class FindInPathTests {

        /**
         * Test that we can walk a tree to the target node.
         */
        @Test
        public void shouldWalkTreeToNode() {
            //given
            val grandparent = "grandparent";
            val grandParentNode = Nodes.unnamedRoot(grandparent);
            val parent = "parent";
            val parentNode = Nodes.unnamedChild(parent, grandParentNode);
            val subject = "subject";
            node = Nodes.unnamedChild(subject, parentNode);
            //when
            val result = grandParentNode.findInPath(
                    Arrays.asList(parent, subject));
            //then
            assertThat(result.isPresent())
                    .as("when we walk the tree to a node it is found")
                    .isTrue();
            result.ifPresent(
                    stringNode ->
                            assertThat(stringNode)
                                    .as("when we walk the tree to a node we find the correct node")
                                    .isSameAs(node));
        }

        /**
         * Test that we get an empty {@link Optional} when walking a path that
         * doesn't exist.
         */
        @Test
        public void shouldNotFindNonExistentChildNode() {
            //given
            val parent = "parent";
            val parentNode = Nodes.unnamedRoot(parent);
            val subject = "subject";
            node = Nodes.unnamedChild(subject, parentNode);
            //when
            val result = parentNode.findInPath(Arrays.asList(subject, "no child"));
            //then
            assertThat(result.isPresent()).as("when we walk the tree to a node that doesn't exists, nothing" + " is found")
                    .isFalse();
        }

        /**
         * Test that when we pass null we get an exception.
         */
        @Test
        public void shouldThrowNPEWhenWalkTreeNull() {
            //given
            node = Nodes.unnamedRoot("subject");
            //when
            assertThatNullPointerException()
                    .isThrownBy(() ->
                            node.findInPath(null))
                    .withMessageContaining("path");
        }

        /**
         * Test that when we pass an empty path we get and empty {@link Optional} as
         * a result.
         */
        @Test
        public void shouldReturnEmptyForEmptyWalkTreePath() {
            //given
            node = Nodes.unnamedRoot("subject");
            //when
            val result = node.findInPath(Collections.emptyList());
            //then
            assertThat(result).isEmpty();
        }
    }

    /**
     * Test that we can create a chain of descendant nodes.
     */
    @Test
    public void shouldCreateDescendantNodes() {
        //given
        node = Nodes.unnamedRoot("subject");
        val alphaData = "alpha";
        val betaData = "beta";
        val gammaData = "gamma";
        //when
        node.createDescendantLine(Arrays.asList(alphaData, betaData, gammaData));
        //then
        val alphaOptional = node.findChild(alphaData);
        assertThat(alphaOptional.isPresent()).as("when creating a descendant line, the first element is found")
                                             .isTrue();
        alphaOptional.ifPresent(alpha -> {
            assertThat(alpha.findParent()).as(
                    "when creating a descendant line, the first element has the current node as its parent")
                                          .contains(node);
            val betaOptional = alpha.findChild(betaData);
            assertThat(betaOptional.isPresent()).as("when creating a descendant line, the second element is found")
                                                .isTrue();
            betaOptional.ifPresent(beta -> {
                assertThat(beta.findParent()).as(
                        "when creating a descendant line, the second element has the first as its parent")
                                             .contains(alpha);
                val gammaOptional = beta.findChild(gammaData);
                assertThat(gammaOptional.isPresent()).as("when creating a descendant line, the third element is found")
                                                     .isTrue();
                gammaOptional.ifPresent(gamma -> {
                    assertThat(gamma.findParent()).as(
                            "when creating a descendant line, the third element has the second as its parent")
                                                  .contains(beta);
                });
            });
        });
    }

    /**
     * Test that if we pass null to create a chain of descendant nodes we get an
     * exception.
     */
    @Test
    public void createDescendantLineShouldThrowNPEWhenDescendantsAreNull() {
        //given
        node = Nodes.unnamedRoot("subject");
        //when
        assertThatNullPointerException()
                .isThrownBy(() ->
                        node.createDescendantLine(null))
                .withMessageContaining("descendants");
    }

    /**
     * Test that if we pass an empty list nothing is changed.
     */
    @Test
    public void shouldChangeNothingWhenCreateDescendantEmpty() {
        //given
        node = Nodes.unnamedRoot("subject");
        //when
        node.createDescendantLine(Collections.emptyList());
        //then
        assertThat(node.getChildren()).as("when creating a descendant line from an empty list, nothing " + "is created")
                                      .isEmpty();
    }

    /**
     * Test that we can get the node for a child.
     */
    @Test
    public void shouldGetChild() {
        //given
        node = Nodes.unnamedRoot("subject");
        val childData = "child";
        val child = Nodes.unnamedRoot(childData);
        node.addChild(child);
        //when
        val found = node.findChild(childData);
        //then
        assertThat(found.isPresent()).as("when retrieving a child by its data, it is found")
                                     .isTrue();
        found.ifPresent(
                stringNode -> assertThat(stringNode).as("when retrieving a child by its data, it is the expected node")
                                                    .isSameAs(child));
    }

    /**
     * Test that we throw an exception when passed null.
     */
    @Test
    public void getChildShouldThrowNPEWhenThereIsNoChild() {
        //given
        node = Nodes.unnamedRoot("data");
        //when
        assertThatNullPointerException()
                .isThrownBy(() ->
                        node.findChild(null))
                .withMessageContaining("child");
    }

    /**
     * Test that we create a child as a child of the current node and with the
     * current node as its parent.
     */
    @Test
    public void shouldCreateChild() {
        //given
        node = Nodes.unnamedRoot("subject");
        val childData = "child";
        //when
        val child = node.createChild(childData);
        //then
        assertThat(child.findParent()).as("when creating a child node, the child has the current node as its parent")
                                      .contains(node);
        val foundChild = node.findChild(childData);
        assertThat(foundChild.isPresent()).as("when creating a child node, the child can be found by its data")
                                          .isTrue();
        foundChild.ifPresent(stringNode -> assertThat(stringNode).as(
                "when creating a child node, the correct child can be found by its data")
                                                                 .isSameAs(child));
    }

    /**
     * Test that we throw an exception when passed null.
     */
    @Test
    public void createChildShouldThrowNPEWhenChildIsNull() {
        //given
        node = Nodes.unnamedRoot("subject");
        //when
        assertThatNullPointerException()
                .isThrownBy(() ->
                        node.createChild(null))
                .withMessageContaining("child");
    }

    @Test
    public void canCreateRootNodeWithoutData() {
        node = Nodes.namedRoot(null, "empty");
        assertThat(node.findData()).isEmpty();
    }

    @Test
    public void nodeNamesAreUniqueWithinAParent() {
        //given
        node = Nodes.namedRoot("root data", "root");
        val alpha = Nodes.namedRoot("alpha data", "alpha");
        node.addChild(alpha);
        val beta = Nodes.namedRoot("beta data", "alpha");
        //when
        assertThatExceptionOfType(NodeException.class)
                .isThrownBy(() ->
                        node.addChild(beta))
                .withMessage("Node with that name already exists here");
    }

    @Test
    public void canPlaceNodeInTreeByPathNames() {
        //given
        node = Nodes.namedRoot("root data", "root"); // create a root
        val four = Nodes.namedRoot("data", "four");
        //when
        node.insertInPath(four, "one", "two", "three");
        //then
        assertThat(four.findParent())
                .as("add node to a tree")
                .isNotEmpty();

        val three = four.findParent();
        assertThat(three).isNotEmpty();
        three.map(threeNode ->
                assertThat(threeNode.getName())
                        .isEqualTo("three"));

        val two = three.flatMap(Node::findParent);
        assertThat(two).isNotEmpty();
        two.map(twoNode ->
                assertThat(twoNode.getName())
                        .isEqualTo("two"));

        val one = two.flatMap(Node::findParent);
        assertThat(one).isNotEmpty();
        one.ifPresent(oneNode ->
                SoftAssertions.assertSoftly(softly -> {
                    assertThat(oneNode.getName()).isEqualTo("one");
                    assertThat(oneNode.findParent()).contains(node);
                }));
        Optional<Node<String>> fourNode = node.findChildByName("one")
                .flatMap(oneChild -> oneChild.findChildByName("two"))
                .flatMap(twoChild -> twoChild.findChildByName("three"))
                .flatMap(threeChild -> threeChild.findChildByName("four"));
        assertThat(fourNode).isNotEmpty();
        assertThat(fourNode).contains(four);
    }

    @Test
    public void canPlaceInTreeUnderExistingNode() {
        //given
        node = Nodes.namedRoot(null, "root");
        val child = Nodes.namedRoot("child data", "child");
        val grandchild = Nodes.namedRoot("grandchild data", "grandchild");
        //when
        node.insertInPath(child); // as root/child
        node.insertInPath(grandchild, "child"); // as root/child/grandchild
        //then
        assertThat(node.findChildByName("child"))
                .as("child")
                .contains(child);
        Optional<Node<String>> grandNode = node.findChildByName("child")
                .flatMap(childNode -> childNode.findChildByName("grandchild"));
        assertThat(grandNode)
                .as("grandchild")
                .contains(grandchild);
    }

    @Test
    public void canPlaceInTreeAboveExistingNode() {
        //given
        node = Nodes.namedRoot(null, "root");
        val child = Nodes.namedRoot("child data", "child");
        val grandchild = Nodes.namedRoot("grandchild data", "grandchild");
        //when
        node.insertInPath(grandchild, "child");
        node.insertInPath(child);
        //then
        assertThat(node.findChildByName("child").flatMap(Node::findData))
                .as("data in tree")
                .contains("child data");
        assertThat(
                node.findChildByName("child").flatMap(childNode ->
                        childNode.findChildByName("grandchild")))
                .as("grandchild")
                .contains(grandchild);
    }

    @Test
    public void removingParentFromNodeWithNoParentIsNoop() {
        //given
        node = Nodes.unnamedRoot(null);
        //when
        node.removeParent();
    }

    @Test
    public void removingParentFromNodeWithParentRemovesParent() {
        //given
        node = Nodes.unnamedRoot(null);
        val child = Nodes.unnamedChild("child data", node);
        //when
        child.removeParent();
        //then
        assertThat(child.findParent()).isEmpty();
        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void placeNodeInTreeWhereNonEmptyNodeWithSameNameExists() {
        //given
        node = Nodes.unnamedRoot(null);
        val child = Nodes.namedChild("child data", "child", node);
        Nodes.namedChild("data", "grandchild", child);
        // root -> child -> grandchild
        // only grandchild has data
        //when
        // attempt to add another node called 'grandchild' to 'child'
        assertThatExceptionOfType(NodeException.class)
                .isThrownBy(() ->
                        node.insertInPath(
                                Nodes.namedRoot(
                                        "cuckoo",
                                        "grandchild"),
                                "child"))
                .withMessage("A non-empty node named 'grandchild' already exists here");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void placeNodeInTreeWhenAddedNodeIsUnnamed() {
        //given
        node = Nodes.unnamedRoot(null);
        final Node<String> newNode = Nodes.unnamedRoot(null);
        //when
        node.insertInPath(newNode);
        //then
        assertThat(node.getChildren()).containsOnly(newNode);
    }

    @Test
    public void placeNodeInTreeWhenEmptyChildWithTargetNameExists() {
        //given
        node = Nodes.unnamedRoot(null);
        final Node<String> child = Nodes.namedRoot(null, "child");
        final Node<String> target = Nodes.namedRoot(null, "target");
        node.addChild(child);
        child.addChild(target);
        Node<String> addMe = Nodes.namedRoot("I'm new", "target");
        assertThat(addMe.findParent()).isEmpty();
        assertThat(child.findChildByName("target").flatMap(Node::findData))
                .as("target starts empty")
                .isEmpty();
        //when
        // addMe should replace target as the sole descendant of child
        node.insertInPath(addMe, "child");
        //then
        assertThat(child.findChildByName("target").flatMap(Node::findData))
                .as("target now contains data")
                .contains("I'm new");
    }

    @Test
    public void findChildNamedShouldThrowNPEWhenNameIsNull() {
        //given
        node = Nodes.unnamedRoot(null);
        //when
        assertThatNullPointerException()
                .isThrownBy(() ->
                        node.findChildByName(null))
                .withMessageContaining("name");
    }

    @Nested
    @DisplayName("isNamed")
    public class IsNamedTests {

        @Test
        public void isNamedNull() {
            //given
            node = Nodes.namedRoot(null, null);
            //then
            assertThat(node.isNamed()).isFalse();
        }

        @Test
        public void isNamedEmpty() {
            //given
            node = Nodes.namedRoot(null, "");
            //then
            assertThat(node.isNamed()).isFalse();
        }

        @Test
        public void isNamedNamed() {
            //given
            node = Nodes.namedRoot(null, "named");
            //then
            assertThat(node.isNamed()).isTrue();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void removeChildRemovesTheChild() {
        //given
        node = Nodes.unnamedRoot(null);
        Node<String> child = node.createChild("child");
        assertThat(node.getChildren()).containsExactly(child);
        //then
        node.removeChild(child);
        //then
        assertThat(node.getChildren()).isEmpty();
        assertThat(child.findParent()).isEmpty();
    }

    @Test
    public void canChangeNodeData() {
        //given
        node = Nodes.unnamedRoot("initial");
        //when
        node.setData("updated");
        //then
        assertThat(node.findData()).contains("updated");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void canCreateNamedChild() {
        //given
        node = Nodes.unnamedRoot(null);
        //when
        Node<String> child = node.createChild("child data", "child name");
        //then
        assertThat(child.getName()).isEqualTo("child name");
        assertThat(child.findParent()).contains(node);
        assertThat(node.getChildren()).containsExactly(child);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void constructorWithNameSupplierAndParentBeChildOfParent() {
        //given
        node = Nodes.unnamedRoot(null);
        //when
        val child = Nodes.unnamedChild("child data", node);
        //then
        assertThat(child.findParent()).contains(node);
        assertThat(node.getChildren()).containsExactly(child);
    }

    @Test
    public void canSafelyHandleFindChildWhenAChildHasNoData() {
        //given
        node = Nodes.unnamedRoot(null);
        Nodes.unnamedChild(null, node);
        //when
        node.findChild("data");
    }

    @Test
    public void canStreamAll() throws Exception {
        //given
        node = Nodes.namedRoot("root", "root");
        val n1 = Nodes.namedChild("one", "one", node);
        val n2 = Nodes.namedChild("two", "two", node);
        val n3 = Nodes.namedChild("three", "three", n1);
        val n4 = Nodes.namedChild("four", "four", n2);
        val n5 = Nodes.namedChild("five", "five", n1);
        val n6 = Nodes.namedChild("six", "six", n2);
        val n7 = Nodes.namedChild("seven", "seven", n5);
        val n8 = Nodes.namedChild("eight", "eight", n6);
        //when
        val result = node.stream()
                         .collect(Collectors.toList());
        //then
        assertThat(result).as("full tree")
                          .contains(node, n1, n2, n3, n4, n5, n6, n7, n8);
        // and
        assertThat(n1.stream()
                     .collect(Collectors.toList())).as("sub-tree")
                                                   .containsExactlyInAnyOrder(n1, n3, n5, n7);
    }

    @Test
    public void isRootWhenRootIsTrue() {
        assertThat(Nodes.unnamedRoot(null)
                        .isRoot()).isTrue();
    }

    @Test
    public void isRootWhenNotRootIsFalse() {
        //given
        val root = Nodes.unnamedRoot(null);
        //then
        assertThat(Nodes.unnamedChild(null, root)
                        .isRoot()).isFalse();
    }

    @Test
    public void parentStream() {
        //given
        val root = Nodes.namedRoot("root data", "root");
        val child1 = Nodes.namedChild("child 1 data", "child 1", root);
        val child2 = Nodes.namedChild("child 2 data", "child 2", root);
        val child3 = Nodes.namedChild("child 3 data", "child 3", child2);
        //when
        val resultRoot = root.parentStream()
                             .collect(Collectors.toSet());
        val resultChild1 = child1.parentStream()
                                 .collect(Collectors.toSet());
        val resultChild3 = child3.parentStream()
                                 .collect(Collectors.toSet());
        //then
        assertThat(resultRoot).isEmpty();
        assertThat(resultChild1).containsExactlyInAnyOrder(root);
        assertThat(resultChild3).containsExactlyInAnyOrder(child2, root);
    }

    @Test
    public void whenNodeItemChildrenAreNullThenAsNoChildren() {
        //when
        NodeItem<String> nodeItem = new NodeItem<>(
                "data",
                "name",
                null,
                null);
        //then
        assertThat(nodeItem.getChildren())
                .isEmpty();
    }
}
