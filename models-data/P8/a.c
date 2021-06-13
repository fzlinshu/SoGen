#include<stdio.h>
#include<set>

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

std::set<int> _set0;

void _update() {
	if (_result >= _best__result)
		return;
	_best__result = _result;
}

void _find_p(int _step, int _sum1) {
	if (_step == n - 1 + 1) {
		_result = _sum1 + g[p[n - 1] - 1][0];
		_update();
		return;
	}
	int __sum1 = _sum1;
	for (p[_step] = 1; p[_step] <= n; p[_step]++) {
		int _tmp0 = p[_step];
		if (_set0.find(_tmp0) != _set0.end())
			continue;
		_sum1 = __sum1;
        int _tmp1 = p[_step - 1];
        int _tmp2 = p[_step + 1 - 1];
        _sum1 += g[_tmp1 - 1][_tmp2 - 1];
		if (!(_sum1 < _best__result))
			continue;
		_set0.insert(_tmp0);
		_find_p(_step + 1, _sum1);
		_set0.erase(_tmp0);
	}
}

void _solve() {
	_best__result = 2001;
    p[0] = 1;
	_find_p(1, 0);
}

int main() {
	_input();
	_solve();
	_output();
	return 0;
}
