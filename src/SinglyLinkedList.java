import java.io.Serializable;

public class SinglyLinkedList<T> implements Serializable{
    Node<T> head;
    int size;

    SinglyLinkedList() {
        this.head = null;
        this.size = 0;
    }

    public int size() {
        return this.size;
    }

    //inserts at the beginning of the linked list
    public void add(Node<T> newNode) {
        if (newNode != null) {
            //1- set next of new node to be the head
            newNode.next = this.head;
            //2 - adjust the head node to be the newnode
            this.head = newNode;
            //3 - increment linked list size
            this.size++;
        }
        else
            System.out.println("Can't add thew new node, it's null");

    }
    public void insertAfter(Node<T> prevNode, Node<T> newNode){
        if(prevNode==null){
            System.out.println("Previous node does not exist");
            return;
        }
        newNode.next = prevNode.next;
        prevNode.next = newNode;
        size++;
    }
    //appends a new node to the end of the linked list
    public void append(Node<T> newNode){
        // if list is empty then we're adding hte head node
        if(head==null){
            add(newNode);
            return;
        }
        newNode.next = null;
        // traversing from head (beginning) to tail (end... node with the next=null)
        Node<T> last = head;
        while(last.next != null){
            last = last.next;
        }
        //Now  we can add new Node after the last node
        last.next = newNode;
        size++;
    }
    //removes last node in the linked list
    public void remove(){
        if(head==null)
            return;
        if(head.next == null) //if only one head node
            head = null;
        else{ //more than one node in the list
            Node<T> previous = head;
            Node<T> last = head.next;
            while (last.next!=null){
                previous = previous.next;
                last = last.next;
            }
            previous.next = null;
        }
        size--;

    }

    //overloaded function: function that has the same exact variables but different arguments
    // EARLY BINDING
    //Made with Matthews help thank you!
    public void remove(int index){
        if(head==null)
            return;

        Node temp = head;

        if(index == 0){
            head = temp.next;
            return;
        }

        for(int i=0; temp != null && i < index - 1; i++){
            temp = temp.next;
        }

        if(temp == null || temp.next == null)
            return;

        Node next = temp.next.next;

        temp.next = next;



    }


    public void insert(Node<T> newNode, int index){
        if(head == null){
            this.head = newNode;
        }
        if(head.next == null){
            this.head.next = newNode;
        }
        if(index > size - 1){
            return;
        }

        Node<T> previous = this.head;
        Node<T> next = this.head.next;

        if(index == 0){
            newNode.next = previous;
            this.head = newNode;
        }
        else {
            for (int i = 0; i < index; i++) {
                previous = previous.next;
                next = next.next;
            }
            previous.next = newNode;
            newNode.next = next;
        }
        size++;
    }


    public void clear() {
        head = null;
        size = 0;
    }
    //prints the objects of a linked list

    public String printList(){
        StringBuilder sb = new StringBuilder();
        if(head == null){
            return "The list is empty!";
        }
        sb.append("[");
        Node<T> it = head; // it = iterator
        while(it!=null){
            sb.append(it.data + ",");
            it = it.next;
        }
        sb.append("]");

        return sb.toString();
    }



    public String toString(){
        String s = "[";

        if(head == null){
            return "The list is empty";
        }
        else {
            Node<T> it = head; // it = iterator
            while (it != null) {
                s += it.data + ", ";
                it = it.next;
            }

            s = s.substring(0, s.length() - 2);
            s += "]";
            return s;
        }
    }



}