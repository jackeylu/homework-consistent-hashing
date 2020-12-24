package com.app.hash;

/**
 * @author lvlin
 * @date 2020-12-21 9:14 AM
 */
public interface ConsistentHashing<K, V> {
    /**
     * add a physical node to the consistent hashing
     * @param node node information
     */
    void  addNode(Node node);

    /**
     * remove a physical node from the consistent hashing
     * @param node node id
     */
     void removeNode(Node node);


    /**
     * print out the allocation and slots information for this consistent hashing system
     * @return generated information
     * @param detail show details or not
     */
    String info(final boolean detail);

    /**
     * the size of k-v pairs
     * @return k-v pairs' size
     */
    int size();

    /**
     * is empty or not
     * @return empty or not
     */
    boolean isEmpty();

    /**
     * retrieve value with key
     * @param key key
     * @return value if found, else null
     */
    V get(final K key);

    /**
     * put a k-v pair
     * @param key key
     * @param value value
     * @return the old value with given key
     */
    V put(final K key, final V value);

    /**
     * remove item with given key
     * @param key the key
     * @return value for removed item, else null
     */
    V remove(final K key);

}
