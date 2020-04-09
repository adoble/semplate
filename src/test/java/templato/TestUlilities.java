package templato;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUlilities {
	
	public static void copyFromResource(String resourceFileName, Path outputFile)  {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		try (InputStream in  = classLoader.getResourceAsStream(resourceFileName)) {
			Files.copy(in, outputFile);
		} catch (IOException e) {
			fail(e.getMessage());
			
		}
		assertTrue(Files.exists(outputFile));
	}

}
