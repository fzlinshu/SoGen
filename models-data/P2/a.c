#include<stdio.h>

int n;
int w1;
int w2;
int w3;
int w4;
int w5;
int a1;
int a2;
int a3;
int a4;
int a5;
int b1;
int b2;
int b3;
int b4;
int b5;
int p;
int q;
int x;
int y;
int _result;
int _best__result;

void _input() {
	scanf("%d", &n);
	scanf("%d", &w1);
	scanf("%d", &w2);
	scanf("%d", &w3);
	scanf("%d", &w4);
	scanf("%d", &w5);
	scanf("%d", &a1);
	scanf("%d", &a2);
	scanf("%d", &a3);
	scanf("%d", &a4);
	scanf("%d", &a5);
	scanf("%d", &b1);
	scanf("%d", &b2);
	scanf("%d", &b3);
	scanf("%d", &b4);
	scanf("%d", &b5);
	scanf("%d", &p);
	scanf("%d", &q);
}

void _output() {
	printf("%d\n", _best__result);
}

void _update() {
	if (_result <= _best__result)
		return;
	_best__result = _result;
}

void _solve() {
	_best__result = -1;
	for (x = 0; x <= n; x++) {
		for (y = 0; y <= n; y++) {
			_result = p * x + q * y;
			if (!(a1 * x + b1 * y <= w1))
				continue;
			if (!(a2 * x + b2 * y <= w2))
				continue;
			if (!(a3 * x + b3 * y <= w3))
				continue;
			if (!(a4 * x + b4 * y <= w4))
				continue;
			if (!(a5 * x + b5 * y <= w5))
				continue;
			_update();
		}
	}
}

int main() {
	_input();
	_solve();
	_output();
	return 0;
}
