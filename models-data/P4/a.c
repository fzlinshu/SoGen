#include<stdio.h>
#include<stdlib.h>

int n;
int g[100][100];
int c[100];

void _input() {
	scanf("%d", &n);
	for (int _tmp0 = 1; _tmp0 <= n; _tmp0++)
		for (int _tmp1 = 1; _tmp1 <= n; _tmp1++)
			scanf("%d", &g[_tmp0 - 1][_tmp1 - 1]);
}

void _output() {
	for (int _tmp0 = 1; _tmp0 <= n; _tmp0++)
		printf("%d ", c[_tmp0 - 1]);
	printf("\n");
	exit(0);
}

void _find_c(int _step) {
	if (_step == n - 1 + 1) {
		_output();
		return;
	}
	for (c[_step] = 1; c[_step] <= 4; c[_step]++) {
        bool _tmp0 = true;
        for (int i_1 = 1; i_1 <= _step; i_1++)
            if (!((g[i_1 - 1][_step] == 0) || (c[i_1 - 1] != c[_step]))) {
                _tmp0 = false;
                break;
            }
        if (!_tmp0)
			return;
		_find_c(_step + 1);
    }
}

void _solve() {
	_find_c(0);
}

int main() {
	_input();
	_solve();
	printf("No Solution!\n");
	return 0;
}
