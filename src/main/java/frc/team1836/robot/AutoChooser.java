package frc.team1836.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.team1836.robot.auto.modes.*;
import frc.team1836.robot.auto.trajectory.Path;
import frc.team1836.robot.subsystems.Drive;
import frc.team1836.robot.util.auto.AutoModeBase;
import frc.team1836.robot.util.auto.AutoModeExecuter;
import frc.team1836.robot.util.auto.DeserializePath;
import frc.team1836.robot.util.logging.CrashTracker;

import java.util.HashMap;
import java.util.Map;

public class AutoChooser {

    public static final Map<String, Path> autoPaths = new HashMap<>();
    private static SendableChooser<AutoPosition> positionChooser = new SendableChooser<>();
    private static SendableChooser<AutoAction> actionChooser = new SendableChooser<>();
    private static AutoModeExecuter mAutoModeExecuter = null;
    private static String gameData;

    public static void loadAutos() {
        positionChooser.addDefault("Center", AutoPosition.CENTER);
        positionChooser.addObject("Left", AutoPosition.LEFT);
        positionChooser.addObject("Right", AutoPosition.RIGHT);
        SmartDashboard.putData("Auto Position Chooser", positionChooser);
        actionChooser.addDefault("Standstill", AutoAction.STANDSTILL);
        actionChooser.addObject("Drive Straight", AutoAction.DRIVE_STRAIGHT);
        actionChooser.addObject("Switch", AutoAction.SWITCH);
        SmartDashboard.putData("Auto Action Chooser", actionChooser);
        for (String pathName : Constants.AUTO.autoNames) {
            autoPaths.put(pathName, DeserializePath.getPathFromFile(pathName));
        }
    }

    public static AutoModeBase getAutoMode() {
        gameData = DriverStation.getInstance().getGameSpecificMessage();
        switch (actionChooser.getSelected()) {
            case STANDSTILL:
                return new StandStillMode();
            case DRIVE_STRAIGHT:
                return getStraightMode();
            case SWITCH:
                return getSwitchMode();
            default:
                System.out
                        .println("Unexpected Auto Mode: " + actionChooser.getSelected().toString() + " + "
                                + positionChooser.getSelected().toString());
                break;
        }
        return null;
    }


    private static AutoModeBase getStraightMode() {
        if (Drive.getInstance().isEncodersConnected()) {
            return new DriveStraightMode();
        } else {
            return new DriveStraightOpenLoopMode();
        }
    }

    private static AutoModeBase getSwitchMode() {
        if (Drive.getInstance().isEncodersConnected()) {
            if (positionChooser.getSelected() == AutoPosition.LEFT) {
                return new LeftSwitchMode(getSwitchPosition());
            }
            if (positionChooser.getSelected() == AutoPosition.RIGHT) {
                return new RightSwitchMode(getSwitchPosition());
            }
            if (positionChooser.getSelected() == AutoPosition.CENTER) {
                return new CenterSwitchMode(getSwitchPosition());
            }
        } else {
            if (positionChooser.getSelected() == AutoPosition.LEFT
                    && getSwitchPosition() == GameObjectPosition.LEFT) {
                return new SwitchOpenLoop();
            }
            if (positionChooser.getSelected() == AutoPosition.RIGHT
                    && getSwitchPosition() == GameObjectPosition.RIGHT) {
                return new SwitchOpenLoop();
            }
            if (positionChooser.getSelected() == AutoPosition.CENTER) {
                return new CenterSwitchOpenLoopGyro();
            }
        }
        CrashTracker.logMarker("Couldn't Get Switch Mode");
        return null;
    }


    public static void startAuto() {
        if (mAutoModeExecuter != null) {
            mAutoModeExecuter.stop();
        }
        mAutoModeExecuter = null;
        mAutoModeExecuter = new AutoModeExecuter();
        mAutoModeExecuter.setAutoMode(getAutoMode());
        mAutoModeExecuter.start();
    }

    public static void disableAuto() {
        if (mAutoModeExecuter != null) {
            mAutoModeExecuter.stop();
        }
        mAutoModeExecuter = null;
    }

    public static GameObjectPosition getSwitchPosition() {
        return gameData.charAt(0) == 'L' ? GameObjectPosition.LEFT : GameObjectPosition.RIGHT;
    }

    public enum AutoPosition {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum GameObjectPosition {
        LEFT,
        RIGHT,
    }

    public enum AutoAction {
        STANDSTILL,
        DRIVE_STRAIGHT,
        SWITCH,
    }

}
