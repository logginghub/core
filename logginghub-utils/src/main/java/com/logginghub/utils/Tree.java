package com.logginghub.utils;

import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.Tree.TreeVisitor;
import com.logginghub.utils.maven.VersionedMavenKey;

public class Tree<T> {

    public interface TreeVisitor<T> {
        void visit(T t);
    }

    public static class Node<T> {

        private T item;
        private List<Node<T>> children = new ArrayList<Node<T>>();
        private boolean isRoot = false;

        public Node(T t) {
            item = t;
        }

        private Node() {isRoot = true;}

        public Node<T> add(T t) {
            Node<T> child = new Node<T>(t);
            children.add(child);
            return child;
        }

        public T get() {
            return item;
        }

        public List<Node<T>> getChildren() {
            return children;
        }

        public void toString(StringBuilder builder, String indent) {
            for (Node<T> child : children) {
                builder.append(indent).append("+- ").append(child.get().toString()).append("\n");
                child.toString(builder, indent + "  ");
            }
        }

        @Override public String toString() {
            StringBuilder builder = new StringBuilder();
            toString(builder, "");
            return builder.toString();
        }

        public void visitDepthFirst(TreeVisitor<T> treeVisitor) {
            for (Node<T> node : children) {
                node.visitDepthFirst(treeVisitor);
            }
            if (!isRoot()) {
                treeVisitor.visit(get());
            }
        }

        public void visitBreadthFirst(TreeVisitor<T> treeVisitor) {
            if (!isRoot()) {
                treeVisitor.visit(get());
            }
            for (Node<T> node : children) {
                node.visitBreadthFirst(treeVisitor);
            }
        }
        
        public boolean isRoot() {
            return isRoot;
        }

    }

    // public interface NodeFactory<T> {
    // T createNode(String name);
    // }

    // public Tree(NodeFactory nodeFactory) {
    // root = nodeFactory.createNode("root");
    // }

    private Node<T> root = new Node<T>();

    public Node<T> getRoot() {
        return root;
    }

    @Override public String toString() {
        StringBuilder builder = new StringBuilder();
        root.toString(builder, "");
        return builder.toString();
    }

    public void visitDepthFirst(TreeVisitor<T> treeVisitor) {
        root.visitDepthFirst(treeVisitor);
    }

    public void visitBreadthFirst(TreeVisitor<T> treeVisitor) {
        root.visitBreadthFirst(treeVisitor);
    }

}
