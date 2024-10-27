import java.io.Serializable;

public class Node<T> implements Serializable{
    T data; //T is a placeholder for any object
    Node next;

    Node(T data){
        this.data = data;
        this.next = null;
    }
    //OVERLOADED CONSTRUCTOR FOR NODE CLASS
    Node (T data, Node nextNode){
        this.data = data;
        this.next = nextNode;
    }
}
