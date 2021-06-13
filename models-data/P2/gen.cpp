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
    for (int i = 0; i < 5; i++)
        cout << rnd(1, 100000) << endl;
    for (int i = 0; i < 10; i++)
        cout << rnd(0, 100) << endl;
    for (int i = 0; i < 2; i++)
        cout << rnd(1, 100) << endl;
    return 0;
}