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
    int c = rnd(2, 50000);
    int k = n / c;
    for (int i = 0; i < n; i++)
        cout << rnd(1, k) * c << ' ';
    cout << endl;
    return 0;
}