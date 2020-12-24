package com.app.hash;

import java.io.*;

/**
 * @author lvlin
 * @date 2020-12-22 8:43 AM
 */
public final class Main {
    public static void main(String[] args) throws IOException {
        final int[] factors = new int[]{1, 3, 5, 7, 10, 12, 15};
//        final int[] numNodes = new int[]{2, 5, 10, 15, 20};
        final int[] numNodes = new int[]{10};
        final int[] numRecords = new int[]{1_000_000};

        PrintStream console = System.out;

        File file = new File("statics.txt");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            PrintStream ps = new PrintStream(outputStream);
            System.setOut(ps);

            for (final int factor : factors) {
                for (final int numNode : numNodes) {
                    for (final int numRecord : numRecords) {
                        testConsistentHashing(factor, numNode, numRecord);
                    }
                }
            }
        }
        System.setOut(console);
        System.out.println("Done");
    }

    private static void testConsistentHashing(final int factor, final int numNodes, final int numRecords) {
        // create 10 physical node
        ConsistentHashing<String, String> consistentHashing = new CycleConsistentHashing<>(factor);
        Node[] nodes = new Node[numNodes];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new Node(String.valueOf(i));
        }
        for (final Node node : nodes) {
            consistentHashing.addNode(node);
        }

        // create 100 000 000 K-V
        for (int i = 0; i < numRecords; i++) {
            String key = String.format("Key%05d", i);
            String value = String.format("Value%05d", i);
            consistentHashing.put(key, value);
        }

        System.out.println(consistentHashing.info(false));
    }
}
