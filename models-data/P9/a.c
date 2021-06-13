#include<stdio.h>

int n;
int g[20][20];
int p[20];
int _result;
int _best__result;

void _input() {
	scanf("%d", &n);
	for (int _tmp0 = 1; _tmp0 <= n; _tmp0++)
		for (int _tmp1 = 1; _tmp1 <= n; _tmp1++)
			scanf("%d", &g[_tmp0 - 1][_tmp1 - 1]);
}

void _output() {
	printf("%d\n", _best__result);
}

void _update() {
	if (_result >= _best__result)
		return;
	_best__result = _result;
}

int _DP_p[20][400];

int _find_p(int _step, int _sum1) {
	if (_step == n - 1 + 1) {
		_result = _sum1;
		if (!(p[n - 1] == n))
			return 1901;
		_update();
		return _sum1;
	}
	if (_step >= 1 && _DP_p[_step][p[_step - 1] - 1] != 1901) {
		_sum1 += _DP_p[_step][p[_step - 1] - 1];
		_result = _sum1;
		_update();
		return _sum1;
	}
	int __sum1 = _sum1;
	for (p[_step] = 1; p[_step] <= n; p[_step]++) {
		_sum1 = __sum1;
        int _tmp0 = p[_step - 1];
		int _tmp1 = p[_step + 1 - 1];
		_sum1 += g[_tmp0 - 1][_tmp1 - 1];
		int _tmp2 = _find_p(_step + 1, _sum1) - __sum1;
		if (_tmp2 < _DP_p[_step][p[_step - 1] - 1])
			_DP_p[_step][p[_step - 1] - 1] = _tmp2;
	}
	return __sum1 + _DP_p[_step][p[_step - 1] - 1];
}

void _solve() {
	_best__result = 1901;
	for (int _tmp0 = 0; _tmp0 < n; _tmp0++)
		for (int _tmp1 = 0; _tmp1 < 400; _tmp1++)
			_DP_p[_tmp0][_tmp1] = 1901;
    p[0] = 1;
	_find_p(1, 0);
}

int main() {
	_input();
	_solve();
	_output();
	return 0;
}
