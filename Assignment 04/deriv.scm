; Use the R5RS Scheme language under legacy languages in Racket.

; File: deriv.scm
; Date: 16 October 2018
; Name: Matthew Morgan
; Tabs: 2

; These definitions assume that expressions are defined as the following:
; + Numeric constants, the symbol x, or subexpressions will be each operand
; + The only operations that will take place are +, -, *, and ^
;
; These are sample expressions:
; (^ 2 (* x 3))          (- (+ 10 10) (* 3 5))
; These are not expressions:
; (* x y)                (- 10 (/ 5 z))

; -------------------------------------------------------------------------------
; nil is a special symbol that will be used to flag expressions
; dFlag is a special symbol that flags an expression as unresolvable by deriv

(define nil 'null)
(define dFlag 'deriv)

; -------------------------------------------------------------------------------
; (deriv E) returns the derivative of E, as obtained directly from the derivative
; rules without simplification. Power expressions that cannot be handled will be
; specified by '(deriv E)', a list whose head is the symbol deriv
;
; (derivative E) returns the derivative of E after simplification

(define (deriv E)
  (cond
    ; c' = 0                    x' = 1
    ((not (list? E))
      (cond
        ((number? E) 0)
        ((symbol? E) 1)
      ))
    ; (a+b)' = a' + b'          (a-b)' = a' - b'
    ((or (opTest '+ E) (opTest '- E))
      (list (getOp E) (deriv (getTermL E)) (deriv (getTermR E))))
    ; (ab)' = a(b') + (a')b
    ((opTest '* E)
      (list '+
        (list '* (getTermL E) (deriv (getTermR E)))
        (list '* (deriv (getTermL E)) (getTermR E))
      ))
    ; (a^c)' = c(a^(c-1))a'     (a^b) = (`deriv a)
    ((opTest '^ E)
      (if (number? (getTermR E))
        ; If 'c' is a number, perform derivative rule
        (list '* (getTermR E)
          (list '* (deriv (getTermL E))
            (list '^ (getTermL E) (- (getTermR E) 1))))
        (if (not (isTouched (getTermR E)))
          ; If 'c' is an expression, attempt simplification before flagging it as
          ; unsolvable (for the cases of, say, '(3+1)' as exponent)
          (deriv (list (getOp E) (getTermL E) (simplifier (getTermR E))))
          (list dFlag (detouch E))
      )))
  )
)

(define (derivative E) (simplify (deriv E)))

; -------------------------------------------------------------------------------
; (simplify E) applies the simplifier to expression E and removes traces of eval
; (simplifier E) applies simplification rules to the expression E
; (simplify-arithmetic E) simplifies subexpressions with two number operands
; (simplify-left E) simplifies subexpressions with numbers as left operand
; (simplify-right E) simplifies subexpressions with numbers as right operand

(define (simplify E) (detouch (simplifier E)))

(define (simplifier E)
  (cond
    ; Catch expressions with the `deriv header
    ((derivTest E) (append E (list nil)))
    ; Recursively call the simplification function to simplify the entire expression
    ((isExpression (getTermL E) #t) (simplifier (list (getOp E) (simplifier (getTermL E)) (getTermR E))))
    ((isExpression (getTermR E) #t) (simplifier (list (getOp E) (getTermL E) (simplifier (getTermR E)))))
    ; Simplify basic expressions based on whether the left, right, or both
    ; operands are numerical
    (else
      (cond
        ((and (number? (getTermL E)) (number? (getTermR E))) (simplify-arithmetic E))
        ((number? (getTermL E)) (simplify-left E))
        ((number? (getTermR E)) (simplify-right E))
        (else (touch E))
      )
    )
  )
)

(define (simplify-arithmetic E)
  (cond
    ((opTest '+ E) (+ (getTermL E) (getTermR E)))
    ((opTest '- E) (- (getTermL E) (getTermR E)))
    ((opTest '* E) (* (getTermL E) (getTermR E)))
    ((opTest '^ E) (expt (getTermL E) (getTermR E)))
    ; Mark the expression as simplified due to unsupported operator
    (else (touch E))
  )
)

(define (simplify-left E)
  (cond
    ; 0 + y = y          0 * y = 0
    ((= (getTermL E) 0)
      (cond
        ((opTest '+ E) (getTermR E))
        ((opTest '* E) 0)
        (else (touch E))
      )
    )
    ; 1 * y = y          1 ^ y = 1
    ((= (getTermL E) 1)
      (cond
        ((opTest '* E) (getTermR E))
        ((opTest '^ E) 1)
        (else (touch E))
      )
    )
    ; Mark the expression to prevent resimplification
    (else (touch E))
  )
)

(define (simplify-right E)
  (cond
    ; y + 0 = y          y - 0 = y
    ; y * 0 = 0
    ((= (getTermR E) 0)
      (cond
        ((opTest '+ E) (getTermL E))
        ((opTest '- E) (getTermL E))
        ((opTest '* E) 0)
        (else (touch E))
      )
    )
    ; y * 1 = y          y ^ 1 = y
    ((= (getTermR E) 1)
      (cond
        ((opTest '* E) (getTermL E))
        ((opTest '^ E) (getTermL E))
        (else (touch E))
      )
    )
    ; Mark the expression to prevent resimplification
    (else (touch E))
  )
)

; -------------------------------------------------------------------------------
; (isExpression E notouch?) returns whether E is an expression and, if notouch?
; is true, if it was touched during prior evaluation. That is:
; + E should be a list if it is an expression
; + E should not have the symbol `deriv as the heading element
; + E should not have been touched prior in evaluation, if notouch? is true
;
; (isTouched E) returns whether there is a flag in the expression E for it having
; been touched during prior evaluation
;
; (derivTest E) tests whether the expression is flagged as unevaluable by derivative
; (opTest op E) tests whether the operator for expression 'E' is equal to 'op'
;
; (getOp E) gets the operator of the expression E
; (getTermL E) and (getTermR E) get the left and right operands of expression E
;
; (touch E) applies a flag to E that marks it as touched during prior evaluation
; (detouch E) recurses down the expression E and removes all flags from
; the expression and it's subexpressions regarding being touched during eval

(define (isExpression E notouch?) (and (list? E) (not (derivTest E)) (if notouch? (not (isTouched E)) #t)))
(define (isTouched E) (member nil E))
(define (derivTest E) (equal? (car E) dFlag))
(define (opTest op E) (equal? op (getOp E)))
(define (getOp E)     (list-ref E 0))
(define (getTermL E)  (list-ref E 1))
(define (getTermR E)  (list-ref E 2))
(define (detouch E)
  (if (not (list? E)) E
    (if (derivTest E)
      (list (list-ref E 0) (list-ref E 1))
      (list (getOp E) (detouch (getTermL E)) (detouch (getTermR E)))
    ))
)
(define (touch E) (append E (list nil)))

; ===============================================================================
; MAIN PROGRAM EXECUTION

(display "----------  SIMPLIFY RULES  ----------\n")
(display "y + 0 = y: ")    (simplify `(+ x 0))
(display "0 + y = y: ")    (simplify `(+ 0 x))
(display "y - 0 = y: ")    (simplify `(- x 0))
(display "0 * y = 0: ")    (simplify `(* 0 x))
(display "y * 0 = 0: ")    (simplify `(* x 0))
(display "1 * y = y: ")    (simplify `(* 1 x))
(display "y * 1 = y: ")    (simplify `(* x 1))
(display "y ^ 1 = y: ")    (simplify `(^ x 1))
(display "1 ^ y = 1: ")    (simplify `(^ 1 x))

(display "\n---------- SIMPLIFICATION ----------\n")
(display "((4*5)-(3+2))^3 = 3375: ")
(simplify `(^ (- (* 4 5) (+ 3 2)) 3))

(display "(4*5)/3 = '(/ 20 3)':   ")
(simplify `(/ (* 4 5) 3))

(display "(4*(3+7))*(14-(((x^2)^2)*0)) = 560: ")
(simplify `(* (* 4 (+ 3 7)) (- 14 (* (^ (^ x 2) 2) 0))))

(display "(x^2)+(x^3)\n  = (+ (^ x 2) (^ x 3))\n  : ")
(simplify `(+ (^ x 2) (^ x 3)))

(display "(4*(3/3))+((2+2)-(2*3))\n  = (+ (* 4 (/ 3 3)) -2)\n  : ")
(simplify `(+ (* 4 (/ 3 3)) (- (+ 2 2) (* 2 3))))

(display "(((10*20)+40)-(x+3))+((30*2)-(x^2))\n  = (+ (- 240 (+ x 3)) (- 60 (^ x 2)))\n  : ")
(simplify `(+ (- (+ (* 10 20) 40) (+ x 3)) (- (* 30 2) (^ x 2))))

(display "\n----------   DERIVATIVE   ----------\n")
(display "(^ x 2) = '(* 2 x)'\n  : ")
(deriv `(^ x 2))
(display "  = ")
(derivative `(^ x 2))

(display "(+ 2 (* 3 x)) = '3'\n  : ")
(deriv `(+ 2 (* 3 x)))
(display "  = ")
(derivative `(+ 2 (* 3 x)))

(display "(x^2)^(3+x) = ...\n  : ")
(deriv `(^ (^ x 2) (+ 3 x)))
(display "  = ")
(derivative `(^ (^ x 2) (+ 3 x)))

(display "(x^2)^(3+1) = ...\n  : ")
(deriv `(^ (^ x 2) (+ 3 1)))
(display "  = ")
(derivative `(^ (^ x 2) (+ 3 1)))

(display "(((10*20)+40)-(x+3))+((30*2)-(x^2))\n  = ...\n  : ")
(deriv `(+ (- (+ (* 10 20) 40) (+ x 3)) (- (* 30 2) (^ x 2))))
(display "  = ")
(derivative `(+ (- (+ (* 10 20) 40) (+ x 3)) (- (* 30 2) (^ x 2))))

(display "(x^3), or (x*x*x)\n  = (* 3 (^ x 2), or\n  = (+ (* x (+ x x)) (* x x))\n  : ")
(derivative `(* x (* x x)))
(display "  : ")
(derivative `(^ x 3))

(display "((5*(x^2))+((x^3)-(7*(x^4))))\n  = (+ (* 10 x) (- (* 3 (^ x 2)) (* 28 (^ x 3))))\n  : ")
(deriv `(+ (* 5 (^ x 2)) (- (^ x 3) (* 7 (^ x 4)))))
(display "  : ")
(derivative `(+ (* 5 (^ x 2)) (- (^ x 3) (* 7 (^ x 4)))))