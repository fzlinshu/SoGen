#include<stdio.h>

int v;
int n;
int h;
int l;
int _result;
int w;
int _best__result;

void _input() {
	scanf("%d", &v);
	scanf("%d", &n);
}

void _output() {
	printf("%d\n", _best__result);
}

void _update() {
	if (_best__result != 0 && _result >= _best__result)
		return;
	_best__result = _result;
}

void _solve() {
	_best__result = 0;
	for (h = 1; h <= n; h++) {
        if (v % h != 0)
			continue;
		for (l = 1; l <= n; l++) {
			if (v / h % l != 0)
				continue;
			w = v / h / l;
			_result = l * w + (w * h + h * l);
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
