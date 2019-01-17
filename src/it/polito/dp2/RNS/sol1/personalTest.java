package it.polito.dp2.RNS.sol1;

/**
 * Copyright by Jacopx on 27/11/2018.
 */
public class personalTest {
    public static void main(String[] args) {
        test1();
        test2();
        test3();
        test4();
        System.out.println("*** Personal Test END ***");
    }

    public static void test1() {
        String testPath = "xsd/personalTest.xml";
        String testPath2 = "xsd/personalTest2.xml";
        System.setProperty("it.polito.dp2.RNS.RnsReaderFactory", "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl");
        System.setProperty("it.polito.dp2.RNS.sol1.RnsInfo.file", testPath);

        RnsInfoSerializer rnsInfoSerializer = new RnsInfoSerializer();
        RnsInfoSerializer.main(new String[]{testPath});
        System.setProperty("it.polito.dp2.RNS.RnsReaderFactory", "it.polito.dp2.RNS.sol1.RnsReaderFactory");
        RnsReaderFactory rnsReaderFactory = new RnsReaderFactory();
        RnsInfoSerializer rnsInfoSerializer2 = new RnsInfoSerializer();
        RnsInfoSerializer.main(new String[]{testPath2});

        System.out.println("*** Test1 END ***");
    }

    public static void test2() {
        String testPath = "xsd/randomOut.xml";
        System.setProperty("it.polito.dp2.RNS.RnsReaderFactory", "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl");
        System.setProperty("it.polito.dp2.RNS.Random.testcase", "2");
        System.setProperty("it.polito.dp2.RNS.Random.seed", "99999999");
        RnsInfoSerializer rnsInfoSerializer = new RnsInfoSerializer();
        RnsInfoSerializer.main(new String[]{testPath});

        System.out.println("*** Test2 END ***");
    }

    public static void test3() {
        String testPath = "xsd/randomOut.xml";
        System.setProperty("it.polito.dp2.RNS.sol1.RnsInfo.file", testPath);
        System.setProperty("it.polito.dp2.RNS.RnsReaderFactory", "it.polito.dp2.RNS.sol1.RnsReaderFactory");
        RnsReaderFactory rnsReaderFactory = new RnsReaderFactory();
        rnsReaderFactory.newRnsReader();

        System.out.println("*** Test3 END ***");
    }

    public static void test4() {
        String testPath = "xsd/randomOut.xml";
        String testPath2 = "xsd/randomOut2.xml";
        System.setProperty("it.polito.dp2.RNS.RnsReaderFactory", "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl");
        System.setProperty("it.polito.dp2.RNS.Random.testcase", "2");
        System.setProperty("it.polito.dp2.RNS.Random.seed", "99999999");
        System.setProperty("it.polito.dp2.RNS.sol1.RnsInfo.file", testPath);

        RnsInfoSerializer rnsInfoSerializer = new RnsInfoSerializer();
        RnsInfoSerializer.main(new String[]{testPath});
        System.setProperty("it.polito.dp2.RNS.RnsReaderFactory", "it.polito.dp2.RNS.sol1.RnsReaderFactory");
        RnsReaderFactory rnsReaderFactory = new RnsReaderFactory();
        RnsInfoSerializer rnsInfoSerializer2 = new RnsInfoSerializer();
        RnsInfoSerializer.main(new String[]{testPath2});

        System.out.println("*** Test4 END ***");
    }
}
