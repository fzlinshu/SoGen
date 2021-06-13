int: n;
array [1..n,1..n] of int: g;

array [1..n] of var 1..n: p;

constraint p[1] = 1;
constraint p[n] = n;

solve minimize (sum (i in 1..n-1) (g[p[i],p[i+1]]));