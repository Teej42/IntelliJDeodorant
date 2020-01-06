package ru.hse.godclass;

public class TestEnclosingAccess {
    private int outerField = 0;

    public class InnerClass {
        private int a;
        private int b;
        private int c;
        private int d;
        private int e;

        public void fun1() {
            a += 1;
            b += 1;
            c += 1;
            outerField += 1;
        }

        public void fun2() {
            d += 1;
            e += 1;
            outerField += 1;
        }
    }
}