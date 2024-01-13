package engine.utils;

public record Tuple<A, B>(A first, B second) {

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    // You can override equals and hashCode methods if needed

    public static void main(String[] args) {
        // Example usage
        Tuple<String, Integer> myTuple = new Tuple<>("Hello", 42);
        System.out.println(myTuple.toString());

        String value1 = myTuple.first();
        int value2 = myTuple.second();

        System.out.println("First: " + value1);
        System.out.println("Second: " + value2);
    }
}