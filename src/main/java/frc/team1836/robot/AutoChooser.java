package frc.team1836.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.team1836.robot.auto.modes.DriveStraightMode;
import frc.team1836.robot.auto.modes.StandStillMode;
import frc.team1836.robot.auto.modes.center.CenterScaleMode;
import frc.team1836.robot.auto.modes.center.CenterSwitchMode;
import frc.team1836.robot.auto.modes.left.LeftScaleMode;
import frc.team1836.robot.auto.modes.left.LeftSwitchMode;
import frc.team1836.robot.auto.modes.right.RightScaleMode;
import frc.team1836.robot.auto.modes.right.RightSwitchMode;
import frc.team1836.robot.util.auto.AutoModeBase;
import frc.team1836.robot.util.auto.AutoModeExecuter;

public class AutoChooser {


	private static SendableChooser<AutoPosition> positionChooser = new SendableChooser<>();
	private static SendableChooser<AutoAction> actionChooser = new SendableChooser<>();
	private static AutoModeExecuter mAutoModeExecuter = null;
	private static String gameData;

	public static void loadChooser() {
		positionChooser.addDefault("Center", AutoPosition.CENTER);
		positionChooser.addObject("Left", AutoPosition.LEFT);
		positionChooser.addObject("Right", AutoPosition.RIGHT);
		SmartDashboard.putData("Auto Position Chooser", positionChooser);

		actionChooser.addDefault("Standstill", AutoAction.STANDSTILL);
		actionChooser.addObject("Drive Straight", AutoAction.DRIVE_STRAIGHT);
		actionChooser.addObject("Switch", AutoAction.SWITCH);
		actionChooser.addObject("Scale", AutoAction.SCALE);
		SmartDashboard.putData("Auto Action Chooser", actionChooser);
		gameData = DriverStation.getInstance().getGameSpecificMessage();
	}

	public static AutoModeBase getAutoMode() {
		switch (actionChooser.getSelected()) {
			case STANDSTILL:
				return new StandStillMode();
			case DRIVE_STRAIGHT:
				if (positionChooser.getSelected() == AutoPosition.CENTER) {
					return new DriveStraightMode();
				}
				return new DriveStraightMode();
			case SCALE:
				if (positionChooser.getSelected() == AutoPosition.LEFT) {
					return new LeftScaleMode(getScalePosition());
				}
				if (positionChooser.getSelected() == AutoPosition.RIGHT) {
					return new RightScaleMode(getScalePosition());
				}
				return new CenterScaleMode(getScalePosition());
			case SWITCH:
				if (positionChooser.getSelected() == AutoPosition.LEFT) {
					return new LeftSwitchMode(getScalePosition());
				}
				if (positionChooser.getSelected() == AutoPosition.RIGHT) {
					return new RightSwitchMode(getScalePosition());
				}
				return new CenterSwitchMode(getScalePosition());
			default:
				System.out
						.println("Unexpected Auto Mode: " + actionChooser.getSelected().toString() + " + "
								+ positionChooser.getSelected().toString());
				break;
		}
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

	public static GameObjectPosition getScalePosition() {
		return gameData.charAt(1) == 'L' ? GameObjectPosition.LEFT : GameObjectPosition.RIGHT;
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
		SCALE

	}

}