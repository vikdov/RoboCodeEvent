import java.util.*;

public class Main {
  public static void main(String[] args) {
    System.out.println("Environment test OK ✅");

    List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);
    int sum = 0;

    for (int n : nums) {
      sum += n;
    }

    System.out.println("Sum = " + sum);

    double r = Math.random();
    System.out.println("Random = " + r);

    if (r > 0.5) {
      System.out.println("Branch A");
    } else {
      System.out.println("Branch B");
    }
  }
}
