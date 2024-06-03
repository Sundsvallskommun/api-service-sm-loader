package se.sundsvall.smloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtil {
	//Not using resourceloader because it's not compatible with ISO-8859-1.
	public static byte[] readOpenEFile(String fileName) {
		Path path = Paths.get("src/test/resources/open-e/" + fileName);
		try {
			return Files.readAllBytes(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
