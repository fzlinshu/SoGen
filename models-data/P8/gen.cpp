#include<iostream>
#include<ctime>
using namespace std;

int rnd(int l, int u) {
    return rand() % (u - l + 1) + l;
}

int main() {
    srand((unsigned)time(NULL));
    int n;
    cin >> n;
    cout << n << endl;
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++)
            cout << rnd(1, 100) << ' ';
        cout << endl;
    }
    return 0;
}