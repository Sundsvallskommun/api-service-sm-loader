package se.sundsvall.smloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtil {
	//Not using resourceloader because it's not compatible with ISO-8859-1.
	public static byte[] readOpenEFile(String fileName) throws Exception {
		Path path = Path.of(ClassLoader.getSystemResource("open-e/" + fileName).toURI());
		try {
			return Files.readAllBytes(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
