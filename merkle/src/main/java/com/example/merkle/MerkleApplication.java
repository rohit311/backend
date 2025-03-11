package com.example.merkle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MerkleApplication {

  public static Node generateTree(ArrayList<String> dataBlocks) {
    ArrayList<Node> childNodes = new ArrayList<>();

    for (String message : dataBlocks) {
        childNodes.add(new Node(null, null, HashAlgorithm.generateHash(message)));
    }

    return buildTree(childNodes);
  }

  private static Node buildTree(ArrayList<Node> children) {
    ArrayList<Node> parents = new ArrayList<>();

    while (children.size() != 1) {
        int index = 0, length = children.size();
        while (index < length) {
            Node leftChild = children.get(index);
            Node rightChild = null;

            if ((index + 1) < length) {
                rightChild = children.get(index + 1);
            } else {
                rightChild = new Node(null, null, leftChild.getHash());
            }

            String parentHash = HashAlgorithm.generateHash(leftChild.getHash() + rightChild.getHash());
            parents.add(new Node(leftChild, rightChild, parentHash));
            index += 2;
        }
        children = parents;
        parents = new ArrayList<>();
    }
    return children.get(0);
  }

  private static void printLevelOrderTraversal(Node root) {
        if (root == null) {
            return;
        }

        if ((root.getLeft() == null && root.getRight() == null)) {
            System.out.println(root.getHash());
        }
        Queue<Node> queue = new LinkedList<>();
        queue.add(root);
        queue.add(null);

        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (node != null) {
                System.out.println(node.getHash());
            } else {
                System.out.println();
                if (!queue.isEmpty()) {
                    queue.add(null);
                }
            }

            if (node != null && node.getLeft() != null) {
                queue.add(node.getLeft());
            }

            if (node != null && node.getRight() != null) {
                queue.add(node.getRight());
            }

        }

  }

  private static void testBuild() {
    ArrayList<String> dataBlocks = new ArrayList<>();
    dataBlocks.add("Captain America");
    dataBlocks.add("Iron Man");
    dataBlocks.add("God of thunder");
    dataBlocks.add("Doctor strange");
    Node root = generateTree(dataBlocks);
    printLevelOrderTraversal(root);
  }

	public static void main(String[] args) {
		SpringApplication.run(MerkleApplication.class, args);
    testBuild();
	}

}
