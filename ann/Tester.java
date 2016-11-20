import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class Tester {
    private static Instances getInstances(Scanner s) throws Exception {
        System.out.print("File ARFF name : ");
        Instances i = new ConverterUtils.DataSource(s.next()).getDataSet();
        System.out.println();
        for (int j = 0; j < i.numAttributes(); j++ ) {
            System.out.println((j + 1) + ". " + i.attribute(j).name());
        }
        System.out.print("Select class index : ");
        i.setClassIndex(s.nextInt() - 1);
        return i;
    }

    private static Classifier getClassifier(Scanner s, Instances i) throws Exception {
        @SuppressWarnings("UnusedAssignment")
        Classifier c = null;
        System.out.println("\n1. Build model");
        System.out.println("2. Load model");
        System.out.print("Choose : ");
        switch(s.nextInt()) {
            case 1 :
                System.out.print("\nLearning rate, validation threshold, number of hidden neuron : ");
                c = new ANN(s.nextDouble(), s.nextDouble(), s.nextInt());
                break;
            case 2 :
                System.out.printf("\nFile MODEL name : ");
                c = (Classifier) weka.core.SerializationHelper.read(s.next());
                break;
            default :
                System.exit(0);
        }
        return c;
    }

    private static Evaluation getEvaluation(Scanner s, Instances i, Classifier c) throws Exception {
        System.out.println("\n1. Full training");
        System.out.println("2. 10-cross-fold validation");
        System.out.print("Choose : ");
        Evaluation e = new Evaluation(i);
        switch(s.nextInt()) {
            case 1 :
                c.buildClassifier(i);
                e.evaluateModel(c, i);
                break;
            case 2 :
                e.crossValidateModel(c, i, 10, new Random(1));
                break;
            default :
                System.exit(0);
        }
        return e;
    }

    private static void saveModel(Scanner s, Classifier c) throws Exception {
        System.out.println("1. Save model");
        System.out.println("2. Exit");
        System.out.print("Choose : ");
        switch(s.nextInt()) {
            case 1 :
                System.out.printf("\nFile MODEL name : ");
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(s.next()))) {
                    oos.writeObject(c);
                    oos.flush();
                }
                break;
            default :
                System.exit(0);
        }
    }

    private static void experiment() throws Exception {
        double learnRate; //current variable
        double valThres;
        int nHidden;
        int trueAnswer;
        Classifier c;
        Evaluation e;
        double max = 102; //variabel saat nilai maksimum
        double maxLearnRate = -1;
        double maxValThres = -1;
        int maxNHidden = -1;
        int maxTrueAnswer = -1;
        Instances i = new ConverterUtils.DataSource("Team.arff").getDataSet(); //load data
        i.setClassIndex(i.numAttributes() - 1);
        System.out.println("Learning rate, validation threshold, number of hidden neuron = correct answer");
        System.out.println("Start " + new Date().toString());
        learnRate = 0.05;
        while (learnRate <= 0.30) {
            valThres = 0.05;
            while (valThres <= 0.30) {
                nHidden = 1;
                while (nHidden <= 30) {
                    c = new ANN(learnRate, valThres, nHidden); //build classifier
                    //c.buildClassifier(i);
                    e = new Evaluation(i);
                    //e.evaluateModel(c, i); //full training
                    e.crossValidateModel(c, i, 10, new Random(1)); //10 cross fold validation
                    trueAnswer = (int) e.correct();
                    System.out.printf("%.2f %.2f %2d = %3d\n", learnRate, valThres, nHidden, trueAnswer);
                    if (trueAnswer > max) {
                        max = trueAnswer;
                        maxLearnRate = learnRate;
                        maxValThres = valThres;
                        maxNHidden = nHidden;
                        maxTrueAnswer = trueAnswer;
                    }
                    nHidden += 1;
                }
                valThres += 1;
            }
            learnRate += 0.05;
        }
        System.out.println("Finish " + new Date().toString());
        System.out.printf("Maksimum : %.2f %.2f %2d = %3d dari %3d\n",
            maxLearnRate, maxValThres, maxNHidden, maxTrueAnswer, i.numInstances());
    }

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY); //biar cepet
        //experiment(); System.exit(0); //komentari baris ini jika mau test manual
        Scanner s = new Scanner(System.in);
        Instances i = getInstances(s);
        Classifier c = getClassifier(s, i);
        Evaluation e = getEvaluation(s, i, c);
        System.out.println(e.toSummaryString(true));
        System.out.println(e.toMatrixString());
        saveModel(s, c);
    }
}