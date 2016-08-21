package net.kemitix.node;

import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Test for {@link NodeItem}.
 *
 * @author pcampbell
 */
public class NodeItemTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Node<String> node;

    @Test
    public void getDataReturnsData() {
        //given
        val data = "this node data";
        //when
        node = Nodes.unnamedRoot(data);
        //then
        assertThat(node.getData()).as("can get the data from a node").
                contains(data);
    }

    @Test
    public void canCreateAnEmptyAndUnnamedNode() {
        //when
        node = Nodes.unnamedRoot(null);
        //then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(node.isEmpty()).as("node is empty").isTrue();
        softly.assertThat(node.isNamed()).as("node is unnamed").isFalse();
        softly.assertAll();
    }

    @Test
    public void canCreateNodeWithParentAndCustomNameSupplier() {
        //given
        node = new NodeItem<>(null, n -> "root name supplier");
        //when
        val child = new NodeItem<String>(null, n -> "overridden", node);
        //then
        assertThat(child.getName()).isEqualTo("overridden");
        assertThat(child.getParent()).contains(node);
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
        assertThat(node.getParent()).as(
                "node created without a parent has no parent").isEmpty();
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
        assertThat(node.getParent()).as(
                "node created with a parent can return the parent")
                                    .contains(parent);
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
        exception.expect(NodeException.class);
        exception.expectMessage("Parent is a descendant");
        //when
        node.setParent(child);
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
                "when a node is created with a parent, the parent has the new"
                        + " node among it's children").contains(node);
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
        assertThat(node.getParent()).as(
                "when a node is assigned a new parent that parent can be "
                        + "returned").contains(parent);
    }

    /**
     * Test that we throw an exception when passed null.
     */
    @Test
    public void shouldThrowNPEWhenSetParentNull() {
        //given
        node = Nodes.unnamedRoot("subject");
        exception.expect(NullPointerException.class);
        exception.expectMessage("parent");
        //when
        node.setParent(null);
    }

    /**
     * Test that we throw an exceptions when attempting to node as its own
     * parent.
     */
    @Test
    public void setParentShouldThrowNodeExceptionWhenParentIsSelf() {
        //given
        node = Nodes.unnamedRoot("subject");
        exception.expect(NodeException.class);
        exception.expectMessage("Parent is a descendant");
        //when
        node.setParent(node);
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
        assertThat(child.getParent()).as(
                "when a node is assigned a new parent, the old parent is "
                        + "replaced").contains(newParent);
        assertThat(node.findChild("child").isPresent()).as(
                "when a node is assigned a new parent, the old parent no "
                        + "longer has the node among it's children").isFalse();
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
        assertThat(child.getParent()).as(
                "when a node with an existing parent is added as a child "
                        + "to another node, then the old parent is replaced")
                                     .contains(newParent);
        assertThat(node.findChild("child").isPresent()).as(
                "when a node with an existing parent is added as a child to "
                        + "another node, then the old parent no longer has "
                        + "the node among it's children").isFalse();
    }

    /**
     * Test that adding null as a child throws an exception.
     */
    @Test
    public void shouldThrowNPEWhenAddingNullAsChild() {
        //given
        node = Nodes.unnamedRoot("subject");
        exception.expect(NullPointerException.class);
        exception.expectMessage("child");
        //when
        node.addChild(null);
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
        assertThat(node.getChildren()).as(
                "when a node is added as a child, the node is among the "
                        + "children").contains(child);
    }

    /**
     * Test that adding a node as it's own child throws an exception.
     */
    @Test
    public void addChildShouldThrowNodeExceptionWhenAddingANodeAsOwnChild() {
        //given
        node = Nodes.unnamedRoot("subject");
        exception.expect(NodeException.class);
        exception.expectMessage("Child is an ancestor");
        //then
        node.addChild(node);
    }

    /**
     * Test that adding a node to itself as a child causes an exception.
     */
    @Test
    public void addChildShouldThrowNodeExceptionWhenAddingSelfAsChild() {
        //given
        node = Nodes.unnamedRoot("subject");
        exception.expect(NodeException.class);
        exception.expectMessage("Child is an ancestor");
        //when
        node.addChild(node);
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
        exception.expect(NodeException.class);
        exception.expectMessage("Child is an ancestor");
        //when
        node.addChild(parent);
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
        exception.expect(NodeException.class);
        exception.expectMessage("Child is an ancestor");
        //when
        node.addChild(grandParent);
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
        assertThat(child.getParent()).as(
                "when a node is added as a child, the child has the node as "
                        + "its parent").contains(node);
    }

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
        val result = grandParentNode.findInPath(Arrays.asList(parent, subject));
        //then
        assertThat(result.isPresent()).as(
                "when we walk the tree to a node it is found").isTrue();
        if (result.isPresent()) {
            assertThat(result.get()).as(
                    "when we walk the tree to a node the correct node is found")
                                    .isSameAs(node);
        }
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
        assertThat(result.isPresent()).as(
                "when we walk the tree to a node that doesn't exists, nothing"
                        + " is found").isFalse();
    }

    /**
     * Test that when we pass null we get an exception.
     */
    @Test
    public void shouldThrowNEWhenWalkTreeNull() {
        //given
        node = Nodes.unnamedRoot("subject");
        exception.expect(NullPointerException.class);
        exception.expectMessage("path");
        //when
        node.findInPath(null);
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
        node.createDescendantLine(
                Arrays.asList(alphaData, betaData, gammaData));
        //then
        val alphaOptional = node.findChild(alphaData);
        assertThat(alphaOptional.isPresent()).as(
                "when creating a descendant line, the first element is found")
                                             .isTrue();
        if (alphaOptional.isPresent()) {
            val alpha = alphaOptional.get();
            assertThat(alpha.getParent()).as(
                    "when creating a descendant line, the first element has "
                            + "the current node as its parent").contains(node);
            val betaOptional = alpha.findChild(betaData);
            assertThat(betaOptional.isPresent()).as(
                    "when creating a descendant line, the second element is "
                            + "found").isTrue();
            if (betaOptional.isPresent()) {
                val beta = betaOptional.get();
                assertThat(beta.getParent()).as(
                        "when creating a descendant line, the second element "
                                + "has the first as its parent")
                                            .contains(alpha);
                val gammaOptional = beta.findChild(gammaData);
                assertThat(gammaOptional.isPresent()).as(
                        "when creating a descendant line, the third element "
                                + "is found").isTrue();
                if (gammaOptional.isPresent()) {
                    val gamma = gammaOptional.get();
                    assertThat(gamma.getParent()).as(
                            "when creating a descendant line, the third "
                                    + "element has the second as its parent")
                                                 .contains(beta);
                }
            }
        }
    }

    /**
     * Test that if we pass null to create a chain of descendant nodes we get an
     * exception.
     */
    @Test
    public void createDescendantLineShouldThrowNPEWhenDescendantsAreNull() {
        //given
        node = Nodes.unnamedRoot("subject");
        exception.expect(NullPointerException.class);
        exception.expectMessage("descendants");
        //when
        node.createDescendantLine(null);
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
        assertThat(node.getChildren()).as(
                "when creating a descendant line from an empty list, nothing "
                        + "is created").isEmpty();
    }

    /**
     * Test that we can find a child of a node.
     */
    @Test
    public void shouldFindExistingChildNode() {
        //given
        node = Nodes.unnamedRoot("subject");
        val childData = "child";
        val child = Nodes.unnamedChild(childData, node);
        //when
        val found = node.findOrCreateChild(childData);
        //then
        assertThat(found).as(
                "when searching for a child by data, the matching child is "
                        + "found").isSameAs(child);
    }

    /**
     * Test that we create a missing child of a node.
     */
    @Test
    public void shouldFindCreateNewChildNode() {
        //given
        node = Nodes.unnamedRoot("subject");
        val childData = "child";
        //when
        val found = node.findOrCreateChild(childData);
        //then
        assertThat(found.getData()).as(
                "when searching for a non-existent child by data, a new node "
                        + "is created").contains(childData);
    }

    /**
     * Test that if we pass null we get an exception.
     */
    @Test
    public void findOrCreateChildShouldThrowNPEFWhenChildIsNull() {
        //given
        node = Nodes.unnamedRoot("subject");
        exception.expect(NullPointerException.class);
        exception.expectMessage("child");
        //when
        node.findOrCreateChild(null);
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
        assertThat(found.isPresent()).as(
                "when retrieving a child by its data, it is found").isTrue();
        if (found.isPresent()) {
            assertThat(found.get()).as(
                    "when retrieving a child by its data, it is the expected "
                            + "node").isSameAs(child);
        }
    }

    /**
     * Test that we throw an exception when passed null.
     */
    @Test
    public void getChildShouldThrowNPEWhenThereIsNoChild() {
        //given
        node = Nodes.unnamedRoot("data");
        exception.expect(NullPointerException.class);
        exception.expectMessage("child");
        //when
        node.findChild(null);
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
        assertThat(child.getParent()).as(
                "when creating a child node, the child has the current node "
                        + "as its parent").contains(node);
        val foundChild = node.findChild(childData);
        assertThat(foundChild.isPresent()).as(
                "when creating a child node, the child can be found by its "
                        + "data").isTrue();
        if (foundChild.isPresent()) {
            assertThat(foundChild.get()).as(
                    "when creating a child node, the correct child can be "
                            + "found by its data").isSameAs(child);
        }
    }

    /**
     * Test that we throw an exception when passed null.
     */
    @Test
    public void createChildShouldThrowNPEWhenChildIsNull() {
        //given
        node = Nodes.unnamedRoot("subject");
        exception.expect(NullPointerException.class);
        exception.expectMessage("child");
        //when
        node.createChild(null);
    }

    @Test
    public void getNameShouldBeCorrect() {
        //given
        node = new NodeItem<>("subject", n -> n.getData().get());
        //then
        assertThat(node.getName()).isEqualTo("subject");
    }

    @Test
    public void getNameShouldUseParentNameSupplier() {
        //given
        val root = new NodeItem<String>("root", n -> n.getData().get());
        node = Nodes.unnamedChild("child", root);
        //then
        assertThat(node.getName()).isEqualTo("child");
    }

    @Test
    public void getNameShouldReturnNameForNonStringData() {
        val root = new NodeItem<LocalDate>(LocalDate.parse("2016-05-23"), n -> {
            if (n.isEmpty()) {
                return null;
            }
            return n.getData().get().format(DateTimeFormatter.BASIC_ISO_DATE);

        });
        //then
        assertThat(root.getName()).isEqualTo("20160523");
    }

    @Test
    public void getNameShouldUseClosestNameSupplier() {
        node = new NodeItem<>("root", n -> n.getData().get());
        val child = new NodeItem<String>("child", Object::toString);
        node.addChild(child);
        val grandChild = Nodes.unnamedChild("grandchild", child);
        //then
        assertThat(node.getName()).isEqualTo("root");
        assertThat(child.getName()).isNotEqualTo("child");
        assertThat(grandChild.getName()).isNotEqualTo("grandchild");
    }

    @Test
    public void getNameShouldWorkWithoutNameSupplier() {
        node = Nodes.namedRoot(null, "root");
        val namedchild = Nodes.namedChild("named", "Alice", node);
        //then
        assertThat(node.getName()).isEqualTo("root");
        assertThat(namedchild.getName()).isEqualTo("Alice");
    }

    @Test
    public void canCreateRootNodeWithoutData() {
        node = Nodes.namedRoot(null, "empty");
        assertThat(node.getData()).isEmpty();
    }

    @Test
    public void canCreateRootNodeWithoutDataButWithNameSupplier() {
        node = Nodes.unnamedRoot(null);
        assertThat(node.getData()).isEmpty();
    }

    @Test
    public void getChildNamedFindsChild() {
        //given
        node = Nodes.namedRoot("root data", "root");
        val alpha = Nodes.namedRoot("alpha data", "alpha");
        val beta = Nodes.namedRoot("beta data", "beta");
        node.addChild(alpha);
        node.addChild(beta);
        //when
        val result = node.getChildByName("alpha");
        //then
        assertThat(result).isSameAs(alpha);
    }

    @Test
    public void getChildNamedFindsNothing() {
        //given
        node = Nodes.namedRoot("root data", "root");
        val alpha = Nodes.namedRoot("alpha data", "alpha");
        val beta = Nodes.namedRoot("beta data", "beta");
        node.addChild(alpha);
        node.addChild(beta);
        exception.expect(NodeException.class);
        exception.expectMessage("Named child not found");
        //when
        node.getChildByName("gamma");
    }

    @Test
    public void nodeNamesAreUniqueWithinAParent() {
        //given
        node = Nodes.namedRoot("root data", "root");
        val alpha = Nodes.namedRoot("alpha data", "alpha");
        node.addChild(alpha);
        val beta = Nodes.namedRoot("beta data", "alpha");
        exception.expect(NodeException.class);
        exception.expectMessage("Node with that name already exists here");
        //when
        node.addChild(beta);
    }

    @Test
    public void canPlaceNodeInTreeByPathNames() {
        //given
        node = Nodes.namedRoot("root data", "root"); // create a root
        val four = Nodes.namedRoot("data", "four");
        //when
        node.insertInPath(four, "one", "two", "three");
        //then
        val three = four.getParent().get();
        assertThat(four.getParent()).as("add node to a tree").isNotNull();
        assertThat(three.getName()).isEqualTo("three");
        val two = three.getParent().get();
        assertThat(two.getName()).isEqualTo("two");
        val one = two.getParent().get();
        assertThat(one.getName()).isEqualTo("one");
        assertThat(one.getParent().get()).isSameAs(node);
        assertThat(node.getChildByName("one")
                       .getChildByName("two")
                       .getChildByName("three")
                       .getChildByName("four")).isSameAs(four);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void canPlaceInTreeUnderExistingNode() {
        //given
        node = Nodes.namedRoot(null, "root");
        val child = Nodes.namedRoot("child data", "child");
        val grandchild = Nodes.namedRoot("grandchild data", "grandchild");
        //when
        node.insertInPath(child); // as root/child
        node.insertInPath(grandchild, "child"); // as root/child/grandchild
        //then
        assertThat(node.getChildByName("child")).as("child").isSameAs(child);
        assertThat(
                node.getChildByName("child").getChildByName("grandchild")).as(
                "grandchild").isSameAs(grandchild);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void canPlaceInTreeAboveExistingNode() {
        //given
        node = Nodes.namedRoot(null, "root");
        val child = Nodes.namedRoot("child data", "child");
        val grandchild = Nodes.namedRoot("grandchild data", "grandchild");
        //when
        node.insertInPath(grandchild, "child");
        node.insertInPath(child);
        //then
        assertThat(node.getChildByName("child").getData()).as("data in tree")
                                                          .contains(
                                                                  "child data");
        assertThat(
                node.getChildByName("child").getChildByName("grandchild")).as(
                "grandchild").isSameAs(grandchild);
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
        assertThat(child.getParent()).isEmpty();
        assertThat(node.getChildren()).isEmpty();
    }

    @Test
    public void placeNodeInTreeWhereNonEmptyNodeWithSameNameExists() {
        //given
        exception.expect(NodeException.class);
        exception.expectMessage(
                "A non-empty node named 'grandchild' already exists here");
        node = Nodes.unnamedRoot(null);
        val child = Nodes.namedChild("child data", "child", node);
        Nodes.namedChild("data", "grandchild", child);
        // root -> child -> grandchild
        // only grandchild has data
        //when
        // attempt to add another node called 'grandchild' to 'child'
        node.insertInPath(Nodes.namedRoot("cuckoo", "grandchild"), "child");
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
    @SuppressWarnings("unchecked")
    public void placeNodeInTreeWhenEmptyChildWithTargetNameExists() {
        //given
        node = Nodes.unnamedRoot(null);
        final Node<String> child = Nodes.namedRoot(null, "child");
        final Node<String> target = Nodes.namedRoot(null, "target");
        node.addChild(child);
        child.addChild(target);
        val addMe = Nodes.namedRoot("I'm new", "target");
        assertThat(addMe.getParent()).isEmpty();
        assertThat(child.getChildByName("target").isEmpty()).as(
                "target starts empty").isTrue();
        //when
        // addMe should replace target as the sole descendant of child
        node.insertInPath(addMe, "child");
        //then
        assertThat(child.getChildByName("target").getData()).as(
                "target now contains data").contains("I'm new");
    }

    @Test
    public void findChildNamedShouldThrowNPEWhenNameIsNull() {
        //given
        exception.expect(NullPointerException.class);
        exception.expectMessage("name");
        node = Nodes.unnamedRoot(null);
        //when
        node.findChildByName(null);
    }

    @Test
    public void isNamedNull() {
        //given
        node = Nodes.unnamedRoot(null);
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

    @Test
    public void removeParentNodeProvidesSameNameSupplier() {
        // once a node has it's parent removed it should provide a default name
        // provider
        //given
        node = new NodeItem<>("data", n -> n.getData().get());
        val child = Nodes.unnamedChild("other", node);
        assertThat(node.getName()).as("initial root name").isEqualTo("data");
        assertThat(child.getName()).as("initial child name").isEqualTo("other");
        //when
        child.removeParent();
        //then
        assertThat(node.getName()).as("final root name").isEqualTo("data");
        assertThat(child.getName()).as("final child name").isEqualTo("other");
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
        assertThat(child.getParent()).isEmpty();
    }

    @Test
    public void drawTreeIsCorrect() {
        //given
        node = Nodes.namedRoot(null, "root");
        val bob = Nodes.namedChild("bob data", "bob", node);
        val alice = Nodes.namedChild("alice data", "alice", node);
        Nodes.namedChild("dave data", "dave", alice);
        Nodes.unnamedChild("bob's child's data",
                bob); // has no name and no children so no included
        val kim = Nodes.unnamedChild("kim data", node); // nameless mother
        Nodes.namedChild("lucy data", "lucy", kim);
        //when
        val tree = node.drawTree(0);
        //then
        String[] lines = tree.split("\n");
        assertThat(lines).contains("[root]", "[ alice]", "[  dave]",
                "[ (unnamed)]", "[  lucy]", "[ bob]");
        assertThat(lines).containsSubsequence("[root]", "[ alice]", "[  dave]");
        assertThat(lines).containsSubsequence("[root]", "[ (unnamed)]",
                "[  lucy]");
        assertThat(lines).containsSubsequence("[root]", "[ bob]");
    }

    @Test
    public void canChangeNodeData() {
        //given
        node = Nodes.unnamedRoot("initial");
        //when
        node.setData("updated");
        //then
        assertThat(node.getData()).contains("updated");
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
        assertThat(child.getParent()).contains(node);
        assertThat(node.getChildren()).containsExactly(child);
    }

    @Test
    public void canGetChildWhenFound() {
        //given
        node = Nodes.unnamedRoot("data");
        val child = Nodes.namedChild("child data", "child name", node);
        //when
        val found = node.getChild("child data");
        //then
        assertThat(found).isSameAs(child);
    }

    @Test
    public void canGetChildWhenNotFound() {
        //given
        exception.expect(NodeException.class);
        exception.expectMessage("Child not found");
        node = Nodes.unnamedRoot("data");
        //when
        node.getChild("child data");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void constructorWithNameSupplierAndParentBeChildOfParent() {
        //given
        node = Nodes.unnamedRoot(null);
        //when
        val child = Nodes.unnamedChild("child data", node);
        //then
        assertThat(child.getParent()).contains(node);
        assertThat(node.getChildren()).containsExactly(child);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void removeParentCopiesRootNameSupplier() {
        //given
        node = new NodeItem<>("root data", n -> "root supplier");
        val child = Nodes.unnamedChild("child data", node);
        assertThat(child.getName()).isEqualTo("root supplier");
        //when
        child.removeParent();
        //then
        assertThat(child.getName()).isEqualTo("root supplier");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void removeParentDoesNotReplaceLocalNameSupplier() {
        //given
        node = new NodeItem<>("root data", n -> "root supplier");
        val child = new NodeItem<>("child data", n -> "local supplier", node);
        assertThat(child.getName()).isEqualTo("local supplier");
        //when
        child.removeParent();
        //then
        assertThat(child.getName()).isEqualTo("local supplier");
    }

    @Test
    public void setNameToNullRevertsToParentNameSupplier() {
        //given
        node = new NodeItem<>(null, n -> "root supplier");
        val child = Nodes.namedChild("child data", "child name", node);
        assertThat(child.getName()).isEqualTo("child name");
        //when
        child.setName(null);
        //then
        assertThat(child.getName()).isEqualTo("root supplier");
    }

    @Test
    public void getNameWithNameSupplierIsRecalculatedEachCall() {
        val counter = new AtomicInteger(0);
        node = new NodeItem<>(null,
                n -> Integer.toString(counter.incrementAndGet()));
        //then
        assertThat(node.getName()).isNotEqualTo(node.getName());
    }

    @Test
    public void isNamedWithNameSupplierIsRecalculatedEachCall() {
        val counter = new AtomicInteger(0);
        node = new NodeItem<>(null, n -> {
            // alternate between even numbers and nulls: null, 2, null, 4, null
            final int i = counter.incrementAndGet();
            if (i % 2 == 0) {
                return Integer.toString(i);
            }
            return null;
        });
        //then
        assertThat(node.isNamed()).isFalse();
        assertThat(node.isNamed()).isTrue();
    }

    @Test
    public void canUseNameSupplierToBuildFullPath() {
        //given
        final Function<Node<String>, String> pathNameSupplier = node -> {
            Optional<Node<String>> parent = node.getParent();
            if (parent.isPresent()) {
                return parent.get().getName() + "/" + node.getData().get();
            }
            return "";
        };
        node = new NodeItem<>(null, pathNameSupplier);
        val child = Nodes.unnamedChild("child", node);
        val grandchild = Nodes.unnamedChild("grandchild", child);
        //then
        assertThat(grandchild.getName()).isEqualTo("/child/grandchild");
    }

    @Test
    public void canSafelyHandleFindChildWhenAChildHasNoData() {
        //given
        node = Nodes.unnamedRoot(null);
        Nodes.unnamedChild(null, node);
        //when
        node.findChild("data");
    }
}
