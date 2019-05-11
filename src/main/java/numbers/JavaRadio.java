package numbers;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavaRadio {

    static Message cljToJavaMessage(Map message) {
        IFn keyword = Clojure.var("clojure.core", "keyword");

        Long time = (Long) message.get(keyword.invoke("time"));
        String type = (String) message.get(keyword.invoke("type"));
        String name = (String) message.get(keyword.invoke("name"));
        Integer longitude = (Integer) message.get(keyword.invoke("long"));
        Integer latitude = (Integer) message.get(keyword.invoke("lat"));
        List<String> content = (List<String>) message.get(keyword.invoke("content"));

        if (content != null) {
            return new Message(time, type, name, longitude, latitude, content.toArray(new String[content.size()]));
        } else {
            return new Message(time, type, name, longitude, latitude);
        }
    }

    static List<Message> cljToJavaMessages(List<Map> messages) {
        List<Message> javaMessages = new ArrayList<>();
        for (Map message : messages) {
            javaMessages.add(cljToJavaMessage(message));
        }
        return javaMessages;
    }

    static List<Message> listen() {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("numbers.radio"));
        IFn listen = Clojure.var("numbers.radio", "listen");
        return cljToJavaMessages((List<Map>) listen.invoke());
    }

    static List<Message> sample() {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("numbers.radio"));
        IFn sample = Clojure.var("numbers.radio", "sample");
        List<Message> messages = cljToJavaMessages((List<Map>) sample.invoke());
        System.out.println();
        for (Message message: messages) {
            System.out.println(message);
        }
        return messages;
    }

    public static void main( String[] args )
    {
      sample();
    }

}
