#include<stdio.h>

int n;
int a[100000];
int c;
int _result;
int _best__result;

void _input() {
	scanf("%d", &n);
	for (int _tmp0 = 1; _tmp0 <= n; _tmp0++)
		scanf("%d", &a[_tmp0 - 1]);
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
	_best__result = 0;
	for (c = 1; c <= 100000; c++) {
		_result = c;
		bool _tmp0 = true;
		for (int i_1 = 1; i_1 <= 100000; i_1++) {
			int _tmp1 = i_1;
			if (_tmp1 > n)
				continue;
			if (!(a[_tmp1 - 1] % c == 0)) {
				_tmp0 = false;
				break;
			}
		}
		if (!_tmp0)
			continue;
		_update();
	}
}

int main() {
	_input();
	_solve();
	_output();
	return 0;
}
