package icu.jiapeng.kitty.common.core.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeUtilTest {

    @Test
    void buildAndFlattenByNodeModel() {
        List<TestNode> nodes = List.of(
                new TestNode("a", "0"),
                new TestNode("b", "a"),
                new TestNode("c", "b"),
                new TestNode("d", "missing")
        );

        List<TestNode> tree = TreeUtil.build(nodes, "0");
        assertEquals(List.of("a", "d"), ids(tree));
        assertEquals(List.of("b"), ids(tree.get(0).getChildren()));
        assertEquals(List.of("c"), ids(tree.get(0).getChildren().get(0).getChildren()));

        List<TestNode> flat = TreeUtil.flatten(tree);
        assertEquals(List.of("a", "b", "c", "d"), ids(flat));
    }

    @Test
    void emptyInputShouldReturnEmptyList() {
        List<TestNode> built = TreeUtil.build(List.of(), "0");
        List<TestNode> flat = TreeUtil.flatten(List.of());
        assertTrue(built.isEmpty());
        assertTrue(flat.isEmpty());
    }

    private static List<String> ids(List<TestNode> nodes) {
        return nodes.stream().map(TestNode::getId).collect(Collectors.toList());
    }

    private static final class TestNode implements TreeNodeModel<String, TestNode> {
        private final String id;
        private final String parentId;
        private List<TestNode> children = new ArrayList<>();

        private TestNode(String id, String parentId) {
            this.id = id;
            this.parentId = parentId;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getParentId() {
            return parentId;
        }

        @Override
        public List<TestNode> getChildren() {
            return children;
        }

        @Override
        public void setChildren(List<TestNode> children) {
            this.children = children;
        }
    }
}
