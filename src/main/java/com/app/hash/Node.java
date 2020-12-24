package com.app.hash;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lvlin
 * @date 2020-12-22 8:44 AM
 */
public final class Node {
    /**
     * the node id
     */
    private final String id;
    /**
     * the physical node points to
     */
    private final Node physicalNode;

    private AtomicInteger count;


    /**
     * create a physical node
     *
     * @param id the node id for this physical node
     */
    public Node(final String id) {
        Objects.requireNonNull(id);
        this.id = "Physical_" + id;
        physicalNode = null;
        count = new AtomicInteger(0);
    }

    /**
     * create a virtual node
     *
     * @param id           the node id for this virtual node
     * @param physicalNode the physical node points to
     */
    public Node(final String id, final Node physicalNode) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(physicalNode);
        this.id = id;
        this.physicalNode = physicalNode;
        this.count = null;
    }

    public String getId() {
        return id;
    }

    public Node getPhysicalNode() {
        return isPhysicalNode() ? this : physicalNode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Node node = (Node) o;
        return getId().equals(node.getId());
    }

    @Override
    public int hashCode() {
        HashFunction hashFunction = Hashing.murmur3_32();
        return hashFunction.hashString(getId(), Charset.defaultCharset()).asInt();
//        return Objects.hash(getId());
    }

    public String info() {
        return "Node id: " + getId() + "," +
                "Node type: " + nodeType() + "," +
                "Size: " + count();
    }

    public String nodeType() {
        return isPhysicalNode() ? "Physical" : "Virtual";
    }

    public boolean isPhysicalNode() {
        return null == physicalNode;
    }


    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", node type =" + nodeType() +
                ", count=" + count() +
                '}';
    }

    public int count() {
        return isPhysicalNode() ? count.get() : getPhysicalNode().count();
    }

    public <V, K> V get(final K key) {
        return null;
    }

    public <V, K> V put(final K key, final V value) {
        if (!isPhysicalNode()) {
            return getPhysicalNode().put(key, value);
        }

        count.incrementAndGet();
        return null;
    }

    public <V, K> V remove(final K key) {
        if (!isPhysicalNode()) {
            return getPhysicalNode().remove(key);
        }
        count.getAndDecrement();
        return null;
    }
}
