// Author: Matthew Morgan
// Date:   20 August 2018
// Tabs:   2

//==============================================================
// These functions work on arrays that represent binary numbers.
// Each value in the arrays is either 0 or 1, and the low order
// bit is at index 0.  For example, if array A contains binary
// number 14 (1110 in standard binary notation), then it might be
// that
//    A[0] = 0
//    A[1] = 1
//    A[2] = 1
//    A[3] = 1
//
// Say that a binary number is *normalized* if its highest order
// bit is 1.  (As a special case, 0 is normalized if it is
// represented by an array of length 0.)
//
// All results of these functions are normalized.  But the
// parameters are not required to be normalized.  For example,
// if array A holds
//    A[0] = 0
//    A[1] = 1
//    A[2] = 1
//    A[3] = 1
//    A[4] = 0
//    A[5] = 0
// then A is a non-normalized representation of 14.
//==============================================================

public class Arithmetic
{
  //=================================================================
  // inc(A) returns an array of bits representing A+1.
  //=================================================================

  public static byte[] inc(byte[] A)
  {
    // carry, if 1, says we haven't yet incremented the binary number
    // result is the binary number after incrementation
    byte carry = 1;
    byte[] result = new byte[A.length];

    // Increment through all bits in the binary number. If carry is 1 and a
    // bit is reached that is 0, then the bits from the original number are
    // simply copied over.
    for(int i=0; i<result.length; i++)
    {
      if (carry == 1)
      {
        if (A[i] == 1)
        {
          result[i] = 0;
        }
        else
        {
          result[i] = 1;
          carry = 0;
        }
      }
      else
      {
        result[i] = A[i];
      }
    }

    // If carry is still 1 after all bits have been iterated over, then the
    // provided binary number has nothing but bits of 1. Extend the number by
    // 1 bit, and make all bits but the MSB 0. (The MSB is 1.)
    if (carry == 1)
    {
      result = new byte[result.length+1];
      for(int k=0; k<result.length; k++) { result[k] = 0; }
      result[result.length-1] = 1;
    }

    // Return the normalized binary number.
    return normalize(result);
  }

  //=================================================================
  // sum(A,B) returns an array of bits representing A+B.
  //=================================================================

  public static byte[] sum(byte[] A, byte[] B)
  {
    // arrShort is the shorter array of A and B
    // arrLong is the longer array of A and B
    // result is the resulting binary number after addition
    // carry toggles whether a bit is being carried or not
    byte[] arrShort, arrLong, result;
    boolean carry = false;

    // Determine which of the byte arrays is longer and shorter. This
    // will be used to, in turn, create the result array's length.
    if (A.length > B.length)
    {
      arrShort = B;
      arrLong = A;
    }
    else
    {
      arrShort = A;
      arrLong = B;
    }

    result = new byte[arrLong.length+1];

    // Iterate through all the bits, adding them together.
    for(int i=0; i<arrLong.length; i++)
    {
      // cur is the number of 1's being added for this bit
      byte cur = arrLong[i];
      if (carry) { cur++; }
      if (i < arrShort.length) { cur += arrShort[i]; }

      // If the total number of 1's being added is even, the resulting bit is 0
      // Else, the bit is 1
      result[i] = (byte)(cur % 2);

      // If the total number of 1's being added is >= 2, a bit must be carried
      // Else, the bit must not be carried
      carry = (cur >= 2);
    }

    // If a bit is still being carried, the resulting bit array has an extra 1
    // since the summed number cannot bit into a binary number with A/B bits.
    if (carry) { result[result.length-1] = 1; }

    // Return the normalized copy of the resulting binary number
    return normalize(result);
  }

  //=================================================================
  // product(A,B) returns an array of bits representing A*B.
  //=================================================================

  public static byte[] product(byte[] A, byte[] B)
  {
    // arrL stores a reference to the longer of the arrays
    // arrS stores a reference to the shorter of the arrays
    // result is the resulting number
    byte[] arrL, arrS, result = null;

    // Determine which of the arrays is longer to optimize looping.
    if (A.length > B.length)
    {
      arrL = A;
      arrS = B;
    }
    else
    {
      arrL = B;
      arrS = A;
    }

    // If one or both of the arrays is zero, then the product is always zero.
    if (isZero(arrS) || isZero(arrL)) { return new byte[0]; }

    // Iterate through all of the bits of the shorter operand and sum together
    // intermediate products. This is only done is the current bit is 1.
    for(int i=0; i<arrS.length; i++)
    {
      if (arrS[i] == 1)
      {
        // Create a new byte array padded for the current bit, copying over the
        // bits from the longer operand and inserting the 0-bit padding.
        byte[] tmp = new byte[i+arrL.length];
        for(int k=0; k<i; k++) { tmp[k] = 0; }
        for(int k=i; k<tmp.length; k++) { tmp[k] = arrL[k-i]; }

        // Sum together the newly-generated number
        if (result == null)
        {
          result = tmp;
        }
        else
        {
          result = sum(result, tmp);
        }
      }
    }

    return normalize(result);
  }

  /* print(A,norm) prints an array of bits, starting with the high-order bit.
   * This function does not print a linebreak before or after the string of
   * bits. norm specifies whether the binary number represented by A should be
   * normalized prior to printing. */

  public static void print(byte[] A, boolean norm)
  {
    // If the array of bits is a representation of 0, then we simply print the
    // number '0' to the screen.
    if (isZero(A)) { System.out.print("0"); }
    else
    {
      byte[] num = A;
      if (norm) { num = normalize(A); }
      for(int i=0; i<num.length; i++) { System.out.print(num[num.length-1-i]); }
    }
  }

  /* print(A) is a short-form for print(A,norm) that assumes the binary number
   * represented by A should be printed after normalization. */

  public static void print(byte[] A) { print(A, true); }

  /* normalize(A) will return an array of bits, A, after normalization. (This
   * is, the removal of any 0's prior to the most-significant bit.) If the
   * provided number is 0, then an empty array of bits is returned.
   *
   * For example, normalize([1,0,0]) --> [1] and normalize([1,1]) --> [1,1]. */

  private static byte[] normalize(byte[] A)
  {
    // Special case: 0 representation
    if (isZero(A)) { return new byte[0]; }

    // rmBits is the number of bits to be removed from the number (leading 0's)
    // norm is the binary number after normalization
    byte rmBits = 0;
    byte[] norm;

    // Determine the number of bits that need to be removed. Then create an
    // array for the normalized number of bits, copying them over.
    while(A[A.length-1-rmBits] != 1) { rmBits++; }
    norm = new byte[A.length-rmBits];
    for(int i=0; i<norm.length; i++) { norm[i] = A[i]; }
    
    return norm;
  }

  /* isZero(A) returns whether or not A is a representation of 0. */

  private static boolean isZero(byte[] A)
  {
    // An array of bits is assumed to be 0 until such a time occurs that a bit
    // of 1 is found in the array. This means that an array of size 0 is always
    // a representation of 0, and any array of bits with only 0's will be found
    // to be 0 as well.
    boolean zero = true;
    for(int i=0; i<A.length && zero; i++)
    {
      if (A[i] == 1) { zero = false; }
    }
    return zero;
  }
}

