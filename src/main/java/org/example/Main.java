package org.example;

public class Main {
    public static void main(String[] args) {
        var client = new ClientImpl();
        var handler = new HandlerImpl(client);
        var response = handler.performOperation("op123");
        System.out.println(response);
    }
}
