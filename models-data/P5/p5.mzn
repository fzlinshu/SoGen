int: n;
int: c;
array [1..n] of int: w;
array [1..n] of int: v;

var set of 1..n: s;

constraint sum (i in s) (w[i]) <= c;

solve maximize sum (i in s) (v[i]);