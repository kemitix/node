package net.kemitix.node;

import lombok.val;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeTreeDrawTest {

    @Test
    public void drawTreeIsCorrect() {
        //given
        final Node<String> node = Nodes.namedRoot(null, "root");
        val bob = Nodes.namedChild("bob data", "bob", node);
        val alice = Nodes.namedChild("alice data", "alice", node);
        Nodes.namedChild("dave data", "dave", alice);
        Nodes.unnamedChild("bob's child's data", bob); // has no name and no children so no included
        val kim = Nodes.unnamedChild("kim data", node); // nameless mother
        Nodes.namedChild("lucy data", "lucy", kim);
        //when
        val tree = Nodes.drawTree(node, 0);
        //then
        String[] lines = tree.split("\n");
        assertThat(lines).contains("[root]", "[ alice]", "[  dave]", "[ (unnamed)]", "[  lucy]", "[ bob]");
        assertThat(lines).containsSubsequence("[root]", "[ alice]", "[  dave]");
        assertThat(lines).containsSubsequence("[root]", "[ (unnamed)]", "[  lucy]");
        assertThat(lines).containsSubsequence("[root]", "[ bob]");
    }

}
