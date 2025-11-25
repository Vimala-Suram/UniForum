module Uniforum_Project {
    requires javafx.controls;
    requires javafx.fxml;
	requires java.desktop;
	requires java.sql;
	requires jbcrypt;
	requires google.api.client;
	requires google.api.client.gson;
	requires com.google.api.services.calendar;
	requires com.google.api.client;
	requires com.google.api.client.json.gson;
	requires com.google.api.client.auth;
	requires com.google.api.client.extensions.java6.auth;
	requires com.google.api.client.extensions.jetty.auth;
	requires transitive com.google.gson;
	requires com.google.common;
	requires jdk.httpserver;


    // Let FXML use reflection on these packages:
    opens application to javafx.graphics, javafx.fxml;
    opens edu.northeastern.uniforum.forum.controller to javafx.fxml, google.api.client, com.google.api.services.calendar, com.google.api.client, com.google.api.client.json.gson, com.google.api.client.auth, com.google.api.client.extensions.java6.auth, com.google.api.client.extensions.jetty.auth, com.google.gson;
    opens edu.northeastern.uniforum.forum.view to javafx.fxml;

    // Optional: export if other modules ever use them
    exports application;
    exports edu.northeastern.uniforum.forum.controller;
}
