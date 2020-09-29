package danasoft;

import org.jetbrains.annotations.Contract;

class BoolWrap {
    private boolean data;

    @Contract(pure = true)
    BoolWrap(boolean b) { data = b; }

    boolean getValue() { return data; }
    void setValue(boolean b) { data = b; }
}
