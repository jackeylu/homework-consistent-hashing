package com.app.hash;

import com.google.common.hash.Hashing;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lvlin
 * @date 2020-12-22 8:41 AM
 */
public final class CycleConsistentHashing<K, V> implements ConsistentHashing<K, V> {
    private final ConflictedMap<Integer, Node> position2Node;
    private final ConflictedMap<Node, Integer> node2Position;
    /**
     * positions on the cycle
     */
    private int[] positions;
    /**
     * a mapping from the physical node to its virtual nodes.
     */
    private final Map<Node,List<Node>> physicalNodes2VirtualNodes;
    /**
     * Replicate factor for virtual nodes.
     */
    private final int factor;
    /**
     * distance between two adjacent physical node and virtual node.
     */
    private final int distance;
    private final int seed = 43;

    /**
     * creat a cycling consistent hashing system with replicate factor
     *
     * @param factor the replicate factor for virtual nodes
     */
    public CycleConsistentHashing(final int factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("factor should > 0");
        }
        this.factor = factor;
        this.distance = Integer.MAX_VALUE / factor;
        position2Node = new ConflictedMap<>();
        node2Position = new ConflictedMap<>();
        physicalNodes2VirtualNodes = new HashMap<>();
    }

    @Override
    public void addNode(final Node node) {
        int hashCode = node.hashCode();
        long temp = hashCode;
        position2Node.put(hashCode, node);
        node2Position.put(node, hashCode);

        for (int i = 0; i < factor; i++) {
            temp += distance;

            Node vNode = buildVirtualNode(node, i);
            int vPosition = position(temp);
            position2Node.put(vPosition, vNode);
            node2Position.put(vNode, vPosition);

            List<Node> vNodes = physicalNodes2VirtualNodes.getOrDefault(node, new LinkedList<>());
            vNodes.add(vNode);
            physicalNodes2VirtualNodes.put(node, vNodes);
        }

        rebuildPositionArray();
    }

    private void rebuildPositionArray() {
        Object[] objects = position2Node.map.keySet().stream().sorted().toArray();
        positions = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            positions[i] = (int) objects[i];
        }
    }

    @Override
    public void removeNode(final Node node) {
        List<Node> nodes = physicalNodes2VirtualNodes.get(node);
        if (nodes == null){
            return;
        }

        // remove the virtual nodes firstly
        for (final Node vNode : nodes) {
            List<Integer> positions = node2Position.get(vNode);
            for (final Integer position : positions) {
                position2Node.remove(position, vNode);
            }
            node2Position.remove(vNode);
        }
        // remove the physical node
        position2Node.remove(node.hashCode(), node);
        node2Position.remove(node);

        physicalNodes2VirtualNodes.remove(node);

        rebuildPositionArray();
    }

    private Node buildVirtualNode(final Node physicalNode, final int virtualNodeId) {
        return new Node(String.format("V%d_%s",
                virtualNodeId, physicalNode.getId()), physicalNode);
    }

    private static int position(long value) {
        return (int) (value % Integer.MAX_VALUE);
    }


    @Override
    public String info(final boolean detail) {
        StringBuilder sb = new StringBuilder();
        sb.append("Physical Nodes: ").append(physicalNodes2VirtualNodes.size()).append("\n");
        sb.append("Replicated factor: ").append(factor).append("\n");
        sb.append("Virtual Nodes: ").append(node2Position.size() - physicalNodes2VirtualNodes.size()).append("\n");
        sb.append("positions: ").append(position2Node.size()).append("\n");
        sb.append("duplicated positions: ").append(node2Position.size() - positions.length).append("\n");
        sb.append("Size of K-V pairs: ").append(size()).append("\n");
        sb.append("Physical Nodes Information: \n");
        physicalNodes2VirtualNodes.keySet().forEach(node -> sb.append(node.info()).append("\n"));
        if (detail) {
            sb.append("Physical --> Virtual").append("\n");
            physicalNodes2VirtualNodes.forEach((node, nodes) -> {
                for (final Node vNode : nodes) {
                    sb.append(node.getId()).append("/").append(node.hashCode())
                            .append(":")
                            .append(vNode.getId()).append("/").append(vNode.hashCode())
                            .append("\n");
                }
            });
        }
        return sb.toString();
    }

    @Override
    public int size() {
        return physicalNodes2VirtualNodes.keySet().stream().map(Node::count).reduce(Integer::sum).get();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }


    @Override
    public V get(final K key) {
        Node node = findNode(key);
        return node.get(key);
    }

    @Override
    public V put(final K key, final V value) {
        Node node = findNode(key);
        return node.put(key, value);
    }

    @Override
    public V remove(final K key) {
        Node node = findNode(key);
        return node.remove(key);
    }

    private Node findNode(final K key) {
        int i = 0;
        int hashCode = Hashing.murmur3_32(seed).hashString(key.toString(), Charset.defaultCharset()).asInt();
        // key.hashCode();
        while (i < positions.length) {
            if (positions[i] >= hashCode){
                return findPhysicalNode(position2Node.get(positions[i]));
            }
            i++;
        }
        return findPhysicalNode(position2Node.get(positions[0]));
    }

    private Node findPhysicalNode(List<Node> nodes){
        for (final Node node : nodes) {
            return node.getPhysicalNode();
        }
        return null;
    }


    /**
     * A hashmap can handle conflicts
     * @param <K> the key type
     * @param <V> the value type
     */
    public static class ConflictedMap<K, V> {
        private final Map<K, List<V>> map = new HashMap<>();

        public void put(final K key, final V value) {
            List<V> vList = map.getOrDefault(key, new LinkedList<>());
//            if (!vList.isEmpty()){
//                System.out.println("found conflict for key " + key);
//                System.out.println("exists: ");
//                for (final V v : vList) {
//                    System.out.println("v = " + v);
//                }
//                System.out.println("new adding: " + value);
//            }
            vList.add(value);
            map.put(key, vList);
        }

        public List<V> get(final K key) {
            return map.getOrDefault(key, null);
        }

        public void remove(final K key, final V value) {
            List<V> vList = map.get(key);
            if (vList != null){
                List<V> collect = vList.stream().filter(v -> !v.equals(value)).collect(Collectors.toList());
                if (!collect.isEmpty()){
                    map.put(key, collect);
                } else {
                    map.remove(key);
                }
            }
        }

        public void remove(final K key) {
            map.remove(key);
        }

        public int size() {
            return map.size();
        }
    }
}
