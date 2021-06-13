#include<stdio.h>
#include<set>
#include<stdlib.h>

int n;
int q[100];

void _input() {
	scanf("%d", &n);
}

void _output() {
	for (int _tmp0 = 1; _tmp0 <= n; _tmp0++)
		printf("%d ", q[_tmp0 - 1]);
	printf("\n");
	exit(0);
}

std::set<int> _set0;
std::set<int> _set1;
std::set<int> _set2;

void _find_q(int _step) {
	if (_step == n - 1 + 1) {
		_output();
		return;
	}
	for (q[_step] = 1; q[_step] <= n; q[_step]++) {
		int _tmp0 = q[_step];
		if (_set0.find(_tmp0) != _set0.end())
			continue;
		int _tmp1 = q[_step + 1 - 1] + _step + 1;
		if (_set1.find(_tmp1) != _set1.end())
			continue;
		int _tmp2 = q[_step + 1 - 1] - _step + 1;
		if (_set2.find(_tmp2) != _set2.end())
			continue;
		_set0.insert(_tmp0);
		_set1.insert(_tmp1);
		_set2.insert(_tmp2);
		_find_q(_step + 1);
		_set0.erase(_tmp0);
		_set1.erase(_tmp1);
		_set2.erase(_tmp2);
	}
}

void _solve() {
	_find_q(0);
}

int main() {
	_input();
	_solve();
	printf("No Solution!\n");
	return 0;
}
