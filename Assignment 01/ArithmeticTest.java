// Author: Matthew Morgan
// Date:   20 August 2018
// Tabs:   2

public class ArithmeticTest {

  public static void main(String[] args) {
    // Binary numbers for assignment-page tests
    byte[] A = {0},
      B = {1},
      C = {0,1},
      D = {1,1},
      E = {1,0,0,1,0,1},
      F = {0,0,1,1,1,1,1,0,1},
      G = {1,1,1,1,1,1},
      H = {1,1,1,1,1,1,1,1,1,1,1,1},
      I = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
           1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
           1,1,1,1,1,1,1,1,1,1,1,1};
    
    // Assignment page tests
    printSum(A, A);
    printProduct(A, A);
    printSum(A, B);
    printProduct(A, B);
    printSum(B, B);
    printProduct(B, B);
    printSum(C, D);
    printProduct(C, D);
    printSum(E, F);
    printSum(F, E);
    printProduct(E, F);
    printSum(G, H);
    printSum(H, G);
    printProduct(G, H);
    printProduct(H, G);
    printSum(I, B);

    // Non-normalized binary numbers for custom testing
    byte[] AA = new byte[] {0,0,0},
      AB = new byte[] {1,0,1,0,0},
      AC = new byte[] {1,1,1,1,0,0,0};

    // Custom tests
    System.out.println();
    printProduct(A, AA);
    printProduct(AA, A);
    printSum(B, AA);
    printSum(AA, B);
    printSum(AB, AC);
    printProduct(AB, AC);
    printSum(new byte[] {0,1,1,0,0}, new byte[] {1,0,0,0,1});
    printProduct(new byte[] {0,1,1,0,0}, new byte[] {1,0,0,0,1});
    printInc(new byte[0], 8);
    printInc(AC, 8);
  }

  /* printProduct(A,B) prints the operands A and B as well as the result
   * of the operation A*B. */

  public static void printProduct(byte[] A, byte[] B) {
    Arithmetic.print(A);
    System.out.print(" x ");
    Arithmetic.print(B);
    System.out.print(" = '");
    Arithmetic.print(Arithmetic.product(A,B));
    System.out.println("'");
  }

  /* printSum(A,B) prints the operands A and B, as well as the result of A+B. */

  public static void printSum(byte[] A, byte[] B) {
    Arithmetic.print(A);
    System.out.print(" + ");
    Arithmetic.print(B);
    System.out.print(" = '");
    Arithmetic.print(Arithmetic.sum(A,B));
    System.out.println("'");
  }

  /* printInc(A,cnt) prints the binary number A and the cnt-th incrementations
   * of the binary number, delimited by spaces.
   *
   * For example, printInc([], 2) will print this line:
   * Increment of 0 for 2 times: 1 10 */

  public static void printInc(byte[] A, int cnt) {
    System.out.print("Increment of ");
    Arithmetic.print(A);
    System.out.print(" for "+cnt+" times: ");
    byte[] tmp = new byte[A.length];
    for(int i=0; i<A.length; i++) { tmp[i] = A[i]; }
    for(int i=0; i<cnt; i++)
    {
      tmp = Arithmetic.inc(tmp);
      Arithmetic.print(tmp);
      System.out.print(" ");
    }
    System.out.println();
  }

}
