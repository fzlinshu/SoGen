#include<stdio.h>

int n;
int c;
int w[100];
int v[100];
int s[100];
int _result;
int _best__result;

void _input() {
	scanf("%d", &n);
	scanf("%d", &n);
	for (int _tmp0 = 1; _tmp0 <= n; _tmp0++)
		scanf("%d", &w[_tmp0 - 1]);
	for (int _tmp0 = 1; _tmp0 <= n; _tmp0++)
		scanf("%d", &v[_tmp0 - 1]);
}

void _output() {
	printf("%d\n", _best__result);
}

void _update() {
	if (_result <= _best__result)
		return;
	_best__result = _result;
}

int _DP_s[100][10001];

int _find_s(int _step, int _sum1, int _sum2) {
	if (_step == n - 1 + 1) {
		_result = _sum2;
		if (!(_sum1 <= c))
			return -1;
		_update();
		return _sum2;
	}
	if (_DP_s[_step][_sum1] != -1) {
		_sum2 += _DP_s[_step][_sum1];
		_result = _sum2;
		_update();
		return _sum2;
	}
	int __sum1 = _sum1;
	int __sum2 = _sum2;
	for (s[_step] = 0; s[_step] <= 1; s[_step]++) {
		_sum1 = __sum1;
		_sum2 = __sum2;
		if (s[_step + 1 - 1])
			_sum1 += w[_step + 1 - 1];
		if (s[_step + 1 - 1])
			_sum2 += v[_step + 1 - 1];
		if (!(_sum1 <= c))
			continue;
		int _tmp0 = _find_s(_step + 1, _sum1, _sum2) - __sum2;
		if (_tmp0 > _DP_s[_step][__sum1])
			_DP_s[_step][__sum1] = _tmp0;
	}
	return __sum2 + _DP_s[_step][__sum1];
}

void _solve() {
	_best__result = -1;
	for (int _tmp0 = 0; _tmp0 < n; _tmp0++)
		for (int _tmp1 = 0; _tmp1 < c + 1; _tmp1++)
			_DP_s[_tmp0][_tmp1] = -1;
	_find_s(0, 0, 0);
}

int main() {
	_input();
	_solve();
	_output();
	return 0;
}
