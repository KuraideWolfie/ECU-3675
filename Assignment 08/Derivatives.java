import java.io.*;

//==========================================================
//                     Class Expression
//==========================================================


abstract class Expression
{
  //----------------------------------------------------------------------------
  // simplify() returns a possibly simplified version of this expression.
  //----------------------------------------------------------------------------

  public abstract Expression simplify();

  //----------------------------------------------------------------------------
  // toString() returns a string that described this expression.  It can be used
  // for printing this expression.
  //----------------------------------------------------------------------------

  public abstract String toString();

  //----------------------------------------------------------------------------
  // rawderiv() returns the derivative of this expression, without simplification.
  //----------------------------------------------------------------------------

  public abstract Expression rawderiv();

  //----------------------------------------------------------------------------
  // deriv() returns the derivative of this expression, with simplification.
  //----------------------------------------------------------------------------

  public Expression deriv()
  {
      return rawderiv().simplify();
  }
}

//==============================================================================
//                     Class ConstantExpression
//==============================================================================
// A constant expression is the expression equivalent of a real number.
//==============================================================================

class ConstantExpression extends Expression
{
  private double value;

  public ConstantExpression(double v)
  {
    value = v;
  }

  public Expression rawderiv()
  {
    return new ConstantExpression(0.0);
  }

  public Expression simplify()
  {
    return this;
  }

  public String toString()
  {
    return "" + value;
  }

  public double getvalue()
  {
    return value;
  }
}

//==============================================================================
//                     Class VarExpression
//==============================================================================
// A VarExpression is the independent variable x.
//==============================================================================

class VarExpression extends Expression
{
  public VarExpression()
  {
  }

  public Expression rawderiv()
  {
    return new ConstantExpression(1.0);
  }

  public Expression simplify()
  {
    return this;
  }

  public String toString()
  {
    return "x";
  }

}

//==============================================================================
//                    Class SumExpression
//==============================================================================
// A SumExpression is an expression that is the sum of two other expressions.
//==============================================================================

class SumExpression extends Expression
{
  private Expression addend1, addend2;

  public SumExpression(Expression e1, Expression e2)
  {
    addend1 = e1;
    addend2 = e2;
  }

  public Expression rawderiv()
  {
    return new SumExpression(addend1.rawderiv(), addend2.rawderiv());
  }

  public Expression simplify()
  {
    return simplifySum(addend1.simplify(), addend2.simplify());
  }

  public String toString()
  {
    return "(" + addend1.toString() + "+" + addend2.toString() + ")";
  }

  private static Expression simplifySum(Expression e1, Expression e2)
  {
    // 0 + x = x and constant arithmetic

    if(e1 instanceof ConstantExpression) {
      ConstantExpression ce = (ConstantExpression) e1;

      // 0 + x = x

      if(ce.getvalue() == 0.0) {
        return e2;
      }

      // Constant arithmetic 

     if(e2 instanceof ConstantExpression) {
      ConstantExpression ce2 = (ConstantExpression) e2;
      return new ConstantExpression(ce.getvalue() + ce2.getvalue());
      }
    }

    // x + 0 = x

    if(e2 instanceof ConstantExpression) {
      ConstantExpression ce = (ConstantExpression) e2;
      if(ce.getvalue() == 0.0) {
          return e1;
      }
    }

    // Default: do not simplify.

    return new SumExpression(e1, e2);
    }
  }

//==============================================================================
// Class SubExpression
//==============================================================================
// A SubExpression is an expression that is the substraction of one expression
// from another expression.
//==============================================================================

class SubExpression extends Expression {
  private Expression eA, eB;

  public SubExpression(Expression a, Expression b) { eA = a; eB = b; }

  public Expression rawderiv() { return new SubExpression(eA.rawderiv(), eB.rawderiv()); }
  public String toString() { return "("+eA.toString()+"-"+eB.toString()+")"; }
  public Expression simplify() { return sub(eA.simplify(), eB.simplify()); }
  private static Expression sub(Expression a, Expression b) {
    boolean aIsCon = (a instanceof ConstantExpression),
      bIsCon = (b instanceof ConstantExpression);
    
    if (aIsCon && bIsCon) {
      // Constant arithmetic
      return new ConstantExpression(
        ((ConstantExpression) a).getvalue() - ((ConstantExpression) b).getvalue()
      );
    }
    
    if (bIsCon) {
      double val = ((ConstantExpression) b).getvalue();

      // x - 0 = x
      if (val == 0.0) { return a; }
    }

    // Don't simplify
    return new SubExpression(a, b);
  }
}

//==============================================================================
// Class ProExpression
//==============================================================================
// ProExpression represents an expression that is two expressions multiplied
// together.
//==============================================================================

class ProExpression extends Expression {
  private Expression eA, eB;

  public ProExpression(Expression a, Expression b) { eA = a; eB = b; }

  public Expression rawderiv() {
    return new SumExpression(
      new ProExpression(eA, eB.rawderiv()), new ProExpression(eA.rawderiv(), eB)
    );
  }
  public String toString() { return "("+eA.toString()+"*"+eB.toString()+")"; }
  public Expression simplify() { return mlt(eA.simplify(), eB.simplify()); }
  private static Expression mlt(Expression a, Expression b) {
    boolean aIsCon = (a instanceof ConstantExpression),
      bIsCon = (b instanceof ConstantExpression);
    
    if (aIsCon && bIsCon) {
      // Constant arithmetic
      return new ConstantExpression(
        ((ConstantExpression) a).getvalue() * ((ConstantExpression) b).getvalue()
      );
    }
    
    if (aIsCon) {
      double val = ((ConstantExpression) a).getvalue();

      // 0 * x = 0
      // 1 * x = x
      if (val == 0.0) { return new ConstantExpression(0); }
      else if (val == 1.0) { return b; }
    }

    if (bIsCon) {
      double val = ((ConstantExpression) b).getvalue();

      // x * 0 = 0
      // x * 1 = x
      if (val == 0.0) { return new ConstantExpression(0); }
      else if (val == 1.0) { return a; }
    }

    // Else don't simplify
    return new ProExpression(a, b);
  }
}

//==============================================================================
// Class ExpExpression
//==============================================================================
// ExpExpression represents an expression that is raised to the power of
// another expression.
//==============================================================================

class ExpExpression extends Expression {
  private Expression eA, eB;
  private boolean notSimple;

  public ExpExpression(Expression a, Expression b) { eA = a; eB = b; notSimple = false; }

  public Expression rawderiv() {
    boolean retest = false;
    Expression simp = eB;

    // If the exponent isn't constant, attempt to simplify it first before not
    // furthering derivative calculation
    while(!notSimple || !retest) {
      if (simp instanceof ConstantExpression) {
        return new ProExpression(
          new ProExpression(
            simp, new ExpExpression(eA, new ConstantExpression(((ConstantExpression) simp).getvalue()-1))),
          eA.deriv()
        );
      }
      else if (!notSimple) { notSimple = true; simp = simp.simplify(); }
      else { retest = true; }
    }

    // Else, don't simplify
    return this;
  }

  public String toString() { return "("+eA.toString()+"^"+eB.toString()+")"; }
  public Expression simplify() { return pro(eA.simplify(), eB.simplify()); }
  private static Expression pro(Expression a, Expression b) {
    boolean aIsCon = (a instanceof ConstantExpression),
      bIsCon = (b instanceof ConstantExpression);

    if (aIsCon && bIsCon) {
      // Constant arithmetic
      return new ConstantExpression(
        Math.pow(((ConstantExpression) a).getvalue(), ((ConstantExpression) b).getvalue())
      );
    }

    if (aIsCon) {
      double val = ((ConstantExpression) a).getvalue();

      // 1 ^ x = 1
      if (val == 1.0) { return new ConstantExpression(1.0); }
    }

    if (bIsCon) {
      double val = ((ConstantExpression) b).getvalue();

      // x ^ 1 = x
      if (val == 1.0) { return a; }
    }

    // Don't simplify
    return new ExpExpression(a, b);
  }
}