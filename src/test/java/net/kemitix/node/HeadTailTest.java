package net.kemitix.node;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static net.kemitix.node.HeadTail.head;
import static net.kemitix.node.HeadTail.tail;
import static org.assertj.core.api.Assertions.assertThat;

public class HeadTailTest {

    private String headValue = "1-" + UUID.randomUUID().toString();
    private String secondValue = "2-" +  UUID.randomUUID().toString();
    private String thirdValue = "3-" + UUID.randomUUID().toString();
    private List<String> emptyList = Collections.emptyList();
    private List<String> singletonList = Collections.singletonList(headValue);
    private List<String> aList = Arrays.asList(
            headValue, secondValue, thirdValue
    );
    private List<String> tailValue = Arrays.asList(
            secondValue, thirdValue
    );

    @Test
    public void headOfAnEmptyListIsEmpty() {
        assertThat(head(emptyList)).isEmpty();
    }
    @Test
    public void headOfASingletonListIsTheItem() {
        assertThat(head(singletonList)).contains(headValue);
    }
    @Test
    public void headOfAListIsTheFirstItem() {
        assertThat(head(aList)).contains(headValue);
    }

    @Test
    public void tailOfAnEmptyListIsEmpty() {
        assertThat(tail(emptyList)).isEmpty();
    }
    @Test
    public void tailOfASingletonListIsEmpty() {
        assertThat(tail(singletonList)).isEmpty();
    }
    @Test
    public void tailOfAListIsMinusTheHead() {
        assertThat(tail(aList)).isEqualTo(tailValue);
    }
}