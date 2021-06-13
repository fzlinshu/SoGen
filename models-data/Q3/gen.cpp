#include<iostream>
#include<ctime>
using namespace std;

int rnd(int l, int u) {
    return rand() % (u - l + 1) + l;
}

int main() {
    srand((unsigned)time(NULL));
    int n;
    int c;
    cin >> n >> c;
    cout << n << ' ' << c << endl;
    return 0;
}