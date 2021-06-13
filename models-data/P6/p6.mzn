int: n;
array [1..n] of int: c;
array [1..n] of int: w;

var set of 1..n: s;

constraint sum (i in s) (c[i]) >0;

solve maximize sum (i in s) (w[i]);