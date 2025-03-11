package com.example.merkle;


public class Node {

  private Node left;
  private Node right;
  private String hash;

  public Node(Node left, Node right, String hash) {
      this.left = left;
      this.right = right;
      this.hash = hash;
  }

  public Node getLeft() {
      return left;
  }

  public void setLeft(Node left) {
      this.left = left;
  }

  public Node getRight() {
      return right;
  }

  public void setRight(Node right) {
      this.right = right;
  }

  public String getHash() {
      return hash;
  }

  public void setHash(String hash) {
      this.hash = hash;
  }
}