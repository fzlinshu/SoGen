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
    cout << n * 20 << endl;
    for (int i = 0; i < n; i++)
        cout << rnd(1, 100) << ' ';
    cout << endl;
    for (int i = 0; i < n; i++)
        cout << rnd(1, 100) << ' ';
    cout << endl;
    return 0;
}