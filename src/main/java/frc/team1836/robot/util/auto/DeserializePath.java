package frc.team1836.robot.util.auto;

import frc.team1836.robot.Constants;
import frc.team1836.robot.auto.trajectory.Path;
import frc.team1836.robot.util.logging.CrashTracker;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.modifiers.TankModifier;

import java.io.File;

public class DeserializePath {

    public static Path getPathFromFile(String name) {
        try {
            String filePath = Constants.AUTO.pathPath + name + ".csv";
            Trajectory traj = Pathfinder.readFromCSV(new File(filePath));
            TankModifier modifier = new TankModifier(traj).modify(Constants.DRIVE.PATH_WHEELBASE);
            Trajectory left = modifier.getLeftTrajectory();
            Trajectory right = modifier.getRightTrajectory();
            return new Path(name, new Path.Pair(left, right));
        } catch (Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }
}
