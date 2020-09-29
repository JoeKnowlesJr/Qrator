package danasoft;

import org.jetbrains.annotations.Contract;

class RotInt {
    private int data;
    private int max;

    @Contract(pure = true)
    RotInt(int i) { data = i; }

    @Contract(pure = true)
    RotInt(int i, int m) {
        data = i;
        max = m;
    }

    int getValue() { return data; }
    void setValue(int i) { data = i; }
    void increaseBy(int i) {
        data += i;
        if (data > max)
            data = 0;
    }
    void decreaseBy(int i) {
        data -= i;
        if (data < (0 - max))
            data = 0;
    }

    void reset() { data = 0; }
}
