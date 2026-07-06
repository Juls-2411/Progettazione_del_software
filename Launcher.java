package com.example.gestioneafam;

/**
 * Quando un'applicazione JavaFX viene impacchettata in un fat-jar e lanciata
 * con "java -jar", la JVM verifica che la classe indicata come Main-Class
 * estenda javafx.application.Application PRIMA che il classpath sia del
 * tutto pronto, causando spesso l'errore
 * "Error: JavaFX runtime components are missing".
 * La soluzione standard e' usare come Main-Class una classe "neutra" (che
 * non estende Application) il cui unico compito e' richiamare il main
 * dell'Application reale. Qui e' referenziata dal maven-shade-plugin
 * (proprieta' app.launcherClass nel pom.xml).
 */
public class Launcher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
