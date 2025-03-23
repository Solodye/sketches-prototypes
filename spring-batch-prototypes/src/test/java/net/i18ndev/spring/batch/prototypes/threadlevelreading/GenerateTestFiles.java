package net.i18ndev.spring.batch.prototypes.threadlevelreading;

public class GenerateTestFiles {

    public static void main(String[] args) {
        int i = 1;
        for(; i < 10000; i ++) {
            System.out.println("customer" + i + "," + i);
        }
    }
}
