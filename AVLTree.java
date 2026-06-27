import java.util.ArrayList;

public class AVLTree {
    private class AVLNode {
        Book book;
        AVLNode left;
        AVLNode right;
        int height;

        public AVLNode(Book book) {
            this.book = book;
            this.height = 0;
        }
    }

    private AVLNode root;

    public void insert(Book book) {
        root = insertRec(root, book);
    }

    public void delete(long isbn) {
        root = deleteRec(root, isbn);
    }

    public Book search(long isbn) {
        return searchRec(root, isbn);
    }

    public ArrayList<Book> inorder() {
        ArrayList<Book> result = new ArrayList<>();
        inorderRec(root, result);
        return result;
    }

    private AVLNode insertRec(AVLNode node, Book book) {
        if (node == null)
            return new AVLNode(book);

        if (book.getIsbn() < node.book.getIsbn())
            node.left = insertRec(node.left, book);
        else if (book.getIsbn() > node.book.getIsbn())
            node.right = insertRec(node.right, book);
        else
            return node;

        updateHeight(node);
        return rebalance(node);
    }

    private AVLNode deleteRec(AVLNode node, long isbn) {
        if (node == null)
            return null;

        if (isbn < node.book.getIsbn())
            node.left = deleteRec(node.left, isbn);
        else if (isbn > node.book.getIsbn())
            node.right = deleteRec(node.right, isbn);
        else {
            if (node.left == null)
                return node.right;
            else if (node.right == null)
                return node.left;
            else {
                AVLNode successor = minValueNode(node.right);
                node.book = successor.book;
                node.right = deleteRec(node.right, successor.book.getIsbn());
            }
        }

        updateHeight(node);
        return rebalance(node);
    }

    private Book searchRec(AVLNode node, long isbn) {
        if (node == null)
            return null;

        if (isbn == node.book.getIsbn())
            return node.book;
        else if (isbn < node.book.getIsbn())
            return searchRec(node.left, isbn);
        else
            return searchRec(node.right, isbn);
    }

    private void inorderRec(AVLNode node, ArrayList<Book> result) {
        if (node == null)
            return;
        inorderRec(node.left, result);
        result.add(node.book);
        inorderRec(node.right, result);
    }

    private AVLNode rebalance(AVLNode node) {
        if (isLeftHeavy(node)) {
            if (balanceFactor(node.left) < 0)
                node.left = rotateLeft(node.left);
            return rotateRight(node);
        }
        else if (isRightHeavy(node)) {
            if (balanceFactor(node.right) > 0)
                node.right = rotateRight(node.right);
            return rotateLeft(node);
        }
        return node;
    }

    private AVLNode rotateLeft(AVLNode node) {
        AVLNode newRoot = node.right;
        node.right = newRoot.left;
        newRoot.left = node;
        updateHeight(node);
        updateHeight(newRoot);
        return newRoot;
    }

    private AVLNode rotateRight(AVLNode node) {
        AVLNode newRoot = node.left;
        node.left = newRoot.right;
        newRoot.right = node;
        updateHeight(node);
        updateHeight(newRoot);
        return newRoot;
    }

    private void updateHeight(AVLNode node) {
        int leftHeight  = (node.left  == null) ? -1 : node.left.height;
        int rightHeight = (node.right == null) ? -1 : node.right.height;
        node.height = 1 + Math.max(leftHeight, rightHeight);
    }

    private int balanceFactor(AVLNode node) {
        if (node == null) return 0;
        int leftHeight  = (node.left  == null) ? -1 : node.left.height;
        int rightHeight = (node.right == null) ? -1 : node.right.height;
        return leftHeight - rightHeight;
    }

    private boolean isLeftHeavy(AVLNode node) {
        return balanceFactor(node) > 1;
    }

    private boolean isRightHeavy(AVLNode node) {
        return balanceFactor(node) < -1;
    }

    private AVLNode minValueNode(AVLNode node) {
        if (node.left == null) return node;
        return minValueNode(node.left);
    }
}