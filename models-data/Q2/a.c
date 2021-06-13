#include<stdio.h>
#include<set>

int n;
int r[10];
int _result;
int _best__result;

void _input() {
	scanf("%d", &n);
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

void _find_r(int _step) {
	if (_step == n - 1 + 1) {
		_result = r[n - 1];
		_update();
		return;
	}
	for (r[_step] = r[_step - 1] + 1; r[_step] <= n * n; r[_step]++) {
        bool _tmp0 = true;
        for (int j_2 = 1; j_2 <= _step; j_2++) {
            int _tmp3 = r[_step + 1 - 1] - r[j_2 - 1];
            if (_set0.find(_tmp3) != _set0.end()) {
                _tmp0 = false;
                break;
            }
        }
        if (!_tmp0)
			continue;
        for (int j_2 = 1; j_2 <= _step; j_2++) {
            int _tmp4 = r[_step + 1 - 1] - r[j_2 - 1];
		    _set0.insert(_tmp4);
        }
		_find_r(_step + 1);
		for (int j_2 = 1; j_2 <= _step; j_2++) {
            int _tmp5 = r[_step + 1 - 1] - r[j_2 - 1];
		    _set0.erase(_tmp5);
        }
	}
}

void _solve() {
	_best__result = 101;
    r[0] = 0;
	_find_r(1);
}

int main() {
	_input();
	_solve();
	_output();
	return 0;
}
