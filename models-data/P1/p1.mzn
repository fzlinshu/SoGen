int: n;
array [1..n] of int: a;

var int: gcd;
array [1..n] of var int: k;

constraint gcd >= 1;
constraint forall (i in 1..n) (a[i] = k[i] * gcd);

solve maximize gcd;