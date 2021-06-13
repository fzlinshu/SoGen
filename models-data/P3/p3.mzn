int: v;
int: n;

var int: l;
var int: w;
var int: h;

constraint l <= n /\ w <= n /\ h <= n;
constraint l > 0 /\ w > 0 /\ h > 0 /\ l * w * h = v;

solve minimize (l * w + w * h + h * l);