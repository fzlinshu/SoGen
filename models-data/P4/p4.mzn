int: n;
array [1..n,1..n] of 0..1: g;

array [1..n] of var 1..4: c;

constraint forall(i in 1..n, j in 1..n) (g[i,j]=0 \/ c[i] != c[j]);

solve satisfy;