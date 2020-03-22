package net.kemitix.node;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NodeException}.
 *
 * @author pcampbell
 */
public class NodeExceptionTest {

    /**
     * Test that message provided to constructor is returned.
     */
    @Test
    public void shouldReturnConstructorMessage() {
        //given
        val message = "this is the message";
        //when
        val nodeException = new NodeException(message);
        //then
        assertThat(nodeException.getMessage())
                .isEqualTo(message);
    }

}
