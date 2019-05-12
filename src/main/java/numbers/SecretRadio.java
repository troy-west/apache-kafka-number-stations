package numbers;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

import java.util.List;

public class SecretRadio {

    static List<Message> listen() {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("numbers.radio"));
        IFn listen = Clojure.var("numbers.radio", "java-listen");
        return (List<Message>) listen.invoke();
    }

    static List<Message> sample() {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("numbers.radio"));
        IFn sample = Clojure.var("numbers.radio", "java-sample");
        List<Message> messages = (List<Message>) sample.invoke();
        System.out.println();
        for (Message message : messages) {
            System.out.println(message);
        }
        return messages;
    }

    public static void main(String[] args) {
        sample();
    }

}
