/*
 * Copyright [2025] [贾鹏]
 *
 * kitty-cms采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 贾鹏: jiapeng_aoa@163.com。
 * 5.不可二次分发开源参与同类竞品，如有想法可联系 贾鹏: jiapeng_aoa@163.com商议合作。
 */
package icu.jiapeng.kitty.common.core.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 通用树工具：列表构树、树打平。
 */
public final class TreeUtil {

    private TreeUtil() {
    }

    /**
     * 将列表节点构建为树。
     * <p>
     * 特性：
     * 1) 保持输入顺序；2) 父节点缺失时作为根节点输出；3) 会重置并覆盖 children。
     *
     * @param nodes        节点列表
     * @param rootParentId 根父 ID（例如 "0"、null、"ROOT"）
     * @param <T>          节点类型
     * @param <ID>         节点 ID 类型
     * @return 根节点列表
     */
    public static <ID, T extends TreeNodeModel<ID, T>> List<T> build(List<T> nodes, ID rootParentId) {
        if (nodes == null || nodes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<ID, T> nodeMap = new LinkedHashMap<>(nodes.size());
        for (T node : nodes) {
            if (node == null) {
                continue;
            }
            nodeMap.put(node.getId(), node);
        }
        if (nodeMap.isEmpty()) {
            return Collections.emptyList();
        }

        Map<ID, List<T>> childrenMap = new LinkedHashMap<>(nodeMap.size());
        for (ID id : nodeMap.keySet()) {
            childrenMap.put(id, new ArrayList<>());
        }

        List<T> roots = new ArrayList<>();
        for (T node : nodes) {
            if (node == null) {
                continue;
            }
            ID id = node.getId();
            ID parentId = node.getParentId();

            boolean isRoot = Objects.equals(parentId, rootParentId)
                    || !nodeMap.containsKey(parentId)
                    || Objects.equals(parentId, id);

            if (isRoot) {
                roots.add(node);
                continue;
            }
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(node);
        }

        for (Map.Entry<ID, T> entry : nodeMap.entrySet()) {
            List<T> children = childrenMap.getOrDefault(entry.getKey(), Collections.emptyList());
            entry.getValue().setChildren(children);
        }
        return roots;
    }

    /**
     * 将树按先序遍历打平为列表。
     *
     * @param treeNodes 根节点列表
     * @param <T>       节点类型
     * @param <ID>      节点 ID 类型
     * @return 打平后的列表
     */
    public static <ID, T extends TreeNodeModel<ID, T>> List<T> flatten(List<T> treeNodes) {
        if (treeNodes == null || treeNodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>();
        Deque<T> stack = new ArrayDeque<>();
        for (int i = treeNodes.size() - 1; i >= 0; i--) {
            T node = treeNodes.get(i);
            if (node != null) {
                stack.push(node);
            }
        }

        while (!stack.isEmpty()) {
            T node = stack.pop();
            result.add(node);
            List<T> children = node.getChildren();
            if (children == null || children.isEmpty()) {
                continue;
            }
            for (int i = children.size() - 1; i >= 0; i--) {
                T child = children.get(i);
                if (child != null) {
                    stack.push(child);
                }
            }
        }
        return result;
    }
}
