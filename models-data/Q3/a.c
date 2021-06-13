#include<stdio.h>
#include<stdlib.h>

int n;
int c;
int g[10][10];

void _input() {
	scanf("%d", &n);
	scanf("%d", &c);
}

void _output() {
	for (int _tmp0 = 1; _tmp0 <= n; _tmp0++) {
		for (int _tmp1 = 1; _tmp1 <= n; _tmp1++)
			printf("%d ", g[_tmp0 - 1][_tmp1 - 1]);
        printf("\n");
    }
	exit(0);
}

void _find_g(int _step) {
	if (_step == n * n - 1 + 1) {
		_output();
		return;
	}
	for (g[_step / n][_step % n] = 1; g[_step / n][_step % n] <= c; g[_step / n][_step % n]++) {
        bool _tmp0 = true;
        int j_1 = _step / n + 1;
        int l_1 = _step % n + 1;
		for (int i_1 = 1; i_1 < j_1; i_1++)
            for (int k_1 = 1; k_1 < l_1; k_1++)
                if (!(!((g[i_1 - 1][k_1 - 1] == g[i_1 - 1][l_1 - 1]) && (g[i_1 - 1][l_1 - 1] == g[j_1 - 1][l_1 - 1]) && (g[j_1 - 1][l_1 - 1] == g[j_1 - 1][k_1 - 1])))) {
                    _tmp0 = false;
                    break;
                }
		if (!_tmp0)
			return;
		_find_g(_step + 1);
    }
}

void _solve() {
	_find_g(0);
}

int main() {
	_input();
	_solve();
	printf("No Solution!\n");
	return 0;
}
