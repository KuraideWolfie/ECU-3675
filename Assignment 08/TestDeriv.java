public class TestDeriv
{
  public static void main(String args[])
  {
    Expression x = new VarExpression(),
      e0 = new ConstantExpression(0.0), e1 = new ConstantExpression(1.0);

    // Simplification tests
    System.out.println("Simplification Tests:\n-------------------------");
    Expression e3 = new ConstantExpression(3.0), e5 = new ConstantExpression(5.0);
    printSimple(new SumExpression(e3, e5));
    printSimple(new SubExpression(e3, e3));
    printSimple(new ProExpression(e5, e5));
    printSimple(new ExpExpression(e5, e3));
    printSimple(build("!b * b !a + a 5 a 10 b 3"));

    System.out.println("Expected output: x, x, x, 0, 0, x, x, x, 1");
    printSimple(new SumExpression(x, e0));
    printSimple(new SumExpression(e0, x));
    printSimple(new SubExpression(x, e0));
    printSimple(new ProExpression(x, e0));
    printSimple(new ProExpression(e0, x));
    printSimple(new ProExpression(x, e1));
    printSimple(new ProExpression(e1, x));
    printSimple(new ExpExpression(x, e1));
    printSimple(new ExpExpression(e1, x));

    // Derivative tests
    System.out.println("\nDerivative tests:\n-------------------------");
    printDeriv(build("!e0 + e0 2 e0 !e1 * e1 3 e1 x"));
    printDeriv(build("^ x 2"));
    printDeriv(build("!e ^ e x e !b * b 2 b 5"));
    printDeriv(build("!b ^ b !e ^ e x e 2 b !c + c 3 c 4"));
    printDeriv(build("!a - a !c + c !d - d !e + e * 10 20 e 40 d + x 3 c * 30 2 a !b ^ b x b 2"));
    printDeriv(build("^ x 3"));
    printDeriv(build("!a ^ a !b * b 3 b x a 3"));
    printDeriv(build("!a * a x a * x x"));
    printDeriv(build("!a ^ a x a + 3 x"));
  }

  /** printDeriv(d) prints the simplified derivative of an expression.
    * 
    * @param d The expression to print the derivative for */

  public static void printDeriv(Expression d) {
    System.out.println("deriv("+d+") = "+d.deriv());
  }

  /** printSimple(d) prints the simplified form of an expression.
    *
    * @param d The expression to print the simplified form of */

  public static void printSimple(Expression d) {
    System.out.println("simplify("+d+") = "+d.simplify());
  }

  /** build(xp) builds an expression using an appropriately-defined expression
    * string, where flags are specified using '!' as a prefix to separate the
    * operator, left operand, and right operand. For example,<br /><br />
    *
    * !e0 + e0 2 e0 !e1 * e1 3 e1 x ---> 2 + (3 * x) <br /><br />
    * * 2 3 ---> 2 * 3
    *
    * @param xp The expression string to build an expression for
    * @return An expession representing the expression string */

  public static Expression build(String xp) {
    // Catch if xp is just 'x' or a constant
    if (xp.equals("x")) { return new VarExpression(); }
    try { return new ConstantExpression(Double.parseDouble(xp)); }
    catch(Exception e) {}

    // Split the expression into its components using the user flag
    String[] comp;
    int off = (xp.charAt(0) == '!' ? 1 : 0);

    if (off == 1) {
      comp = xp.substring(1).split(xp.substring(1, xp.indexOf(" ")+1));
    }
    else {
      comp = xp.split(" ");
    }

    for(int i=0; i<comp.length; i++) { comp[i] = comp[i].trim(); }

    Expression a = build(comp[1+off]), b = build(comp[2+off]);

    // Build the right expression based on operator
    switch(comp[0+off]) {
      case "+":
        return new SumExpression(a, b);
      case "-":
        return new SubExpression(a, b);
      case "*":
        return new ProExpression(a, b);
      case "^":
        return new ExpExpression(a, b);
      default:
        return null;
    }
  }
}