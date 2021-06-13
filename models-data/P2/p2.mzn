int: n;
int: w1;
int: w2;
int: w3;
int: w4;
int: w5;
int: a1;
int: a2;
int: a3;
int: a4;
int: a5;
int: b1;
int: b2;
int: b3;
int: b4;
int: b5;
int: p;
int: q;

var int: x;
var int: y;

constraint x >= 0;
constraint y >= 0;
constraint x <= n;
constraint y <= n;
constraint a1*x + b1*y <= w1;
constraint a2*x + b2*y <= w2;
constraint a3*x + b3*y <= w3;
constraint a4*x + b4*y <= w4;
constraint a5*x + b5*y <= w5;

solve maximize p*x + q*y;