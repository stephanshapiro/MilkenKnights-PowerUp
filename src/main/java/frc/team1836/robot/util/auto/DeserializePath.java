package frc.team1836.robot.util.auto;

import frc.team1836.robot.util.logging.CrashTracker;
import frc.team254.lib.trajectory.Path;
import frc.team254.lib.trajectory.io.TextFileDeserializer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DeserializePath {

	public static Path getPathFromFile(String dir) throws IOException {
		TextFileDeserializer textFileDeserializer = new TextFileDeserializer();
		try {
			String contents = new String(Files.readAllBytes(Paths.get(dir)));
			return textFileDeserializer.deserialize(contents);
		} catch (Throwable t) {
			CrashTracker.logThrowableCrash(t);
			throw t;
		}
	}


}
