package edu.northeastern.uniforum.forum.util;
import java.io.IOException;

import application.Main;
import edu.northeastern.uniforum.forum.controller.DashboardController;
import edu.northeastern.uniforum.forum.controller.ForumController;
import edu.northeastern.uniforum.forum.controller.SettingsController;
import edu.northeastern.uniforum.forum.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
	
	/**
	 * Loads and displays a scene from an FXML file
	 */
	public static void loadScene(String fxmlPath, Stage stage, double width, double height) {
		try {
			FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
			Parent root = loader.load();
			Scene scene = new Scene(root, width, height);
			stage.setScene(scene);
			stage.setResizable(false);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Switches to the Login view
	 */
	public static void switchToLogin() {
		loadScene("/edu/northeastern/uniforum/forum/view/LoginView.fxml", Main.getPrimaryStage(), 1000, 600);
	}
	
	/**
	 * Switches to the Registration view
	 */
	public static void switchToRegistration() {
		loadScene("/edu/northeastern/uniforum/forum/view/RegistrationView.fxml", Main.getPrimaryStage(), 1000, 600);
	}
	
	/**
	 * Switches to the Dashboard view with user data
	 */
	public static void switchToDashboard(User user) {
		try {
			FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/edu/northeastern/uniforum/forum/view/DashboardView.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root, 1400, 800);
			Main.getPrimaryStage().setScene(scene);
			Main.getPrimaryStage().setResizable(true);
			Main.getPrimaryStage().show();
			
			// Initialize controller with user data
			DashboardController controller = loader.getController();
			if (controller != null) {
				controller.initData(user);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Switches to the Forum view with user data
	 */
	public static void switchToForum(User user) {
		try {
			FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/edu/northeastern/uniforum/forum/view/forum.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root, 1400, 800);
			Main.getPrimaryStage().setScene(scene);
			Main.getPrimaryStage().setResizable(true);
			Main.getPrimaryStage().show();
			
			// Initialize controller with user data
			ForumController controller = loader.getController();
			if (controller != null) {
				controller.initData(user);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Switches to the Settings view with user data
	 */
	public static void switchToSettings(User user) {
		try {
			FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/edu/northeastern/uniforum/forum/view/SettingsView.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root, 1400, 800);
			Main.getPrimaryStage().setScene(scene);
			Main.getPrimaryStage().setResizable(true);
			Main.getPrimaryStage().show();
			
			// Initialize controller with user data
			SettingsController controller = loader.getController();
			if (controller != null) {
				controller.initData(user);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Switches to the Course Selection view with user data
	 */
	public static void switchToCourseSelection(User user) {
		try {
			FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/edu/northeastern/uniforum/forum/view/CourseSelection.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root, 1000, 600);
			Main.getPrimaryStage().setScene(scene);
			Main.getPrimaryStage().setResizable(false);
			Main.getPrimaryStage().show();
			
			// Initialize controller with user data
			edu.northeastern.uniforum.forum.controller.CourseSelectionController controller = loader.getController();
			if (controller != null) {
				controller.setCurrentUser(user);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
