package net.kemitix.node;

import lombok.val;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import static net.trajano.commons.testing.UtilityClassTestUtil
        .assertUtilityClassWellDefined;

/**
 * Tests for {@link Nodes}.
 *
 * @author pcampbell
 */
public class NodesTest {

    @Test
    public void shouldBeValidUtilityClass() throws Exception {
        assertUtilityClassWellDefined(Nodes.class);
    }

    @Test
    public void shouldCreateUnnamedRoot() throws Exception {
        val node = Nodes.unnamedRoot("data");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(node.getData()).contains("data");
        softly.assertThat(node.getName()).isEmpty();
        softly.assertAll();
    }

    @Test
    public void shouldCreateNamedRoot() throws Exception {
        val node = Nodes.namedRoot("data", "name");
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(node.getData()).contains("data");
        softly.assertThat(node.getName()).isEqualTo("name");
        softly.assertAll();
    }

    @Test
    public void shouldCreateUnnamedChild() throws Exception {
        val parent = Nodes.unnamedRoot("root");
        val node = Nodes.unnamedChild("data", parent);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(node.getData()).contains("data");
        softly.assertThat(node.getName()).isEmpty();
        softly.assertThat(node.findParent()).contains(parent);
        softly.assertAll();
    }

    @Test
    public void shouldCreateNamedChild() throws Exception {
        val parent = Nodes.unnamedRoot("root");
        val node = Nodes.namedChild("data", "child", parent);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(node.getData()).contains("data");
        softly.assertThat(node.getName()).isEqualTo("child");
        softly.assertThat(node.findParent()).contains(parent);
        softly.assertAll();
    }

}
