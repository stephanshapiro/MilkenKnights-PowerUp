package frc.team1836.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import frc.team1836.robot.RobotState.DriveControlState;
import frc.team1836.robot.RobotState.MatchState;
import frc.team1836.robot.subsystems.Drive;
import frc.team1836.robot.subsystems.Input;
import frc.team1836.robot.subsystems.Superstructure;
import frc.team1836.robot.util.logging.CrashTracker;
import frc.team1836.robot.util.loops.Looper;
import frc.team1836.robot.util.other.SubsystemManager;
import frc.team1836.robot.util.state.DriveSignal;
import java.util.Arrays;

public class Robot extends IterativeRobot {

	private final SubsystemManager mSubsystemManager = new SubsystemManager(
			Arrays.asList(Drive.getInstance(), Superstructure.getInstance(), Input.getInstance()));
	private Looper mEnabledLooper = new Looper();

	public Robot() {
		CrashTracker.logRobotConstruction();
	}

	@Override
	public void robotInit() {
		try {
			CrashTracker.logRobotInit();
			mSubsystemManager.registerEnabledLoops(mEnabledLooper);
			mSubsystemManager.zeroSensors();
			AutoChooser.loadChooser();
		} catch (Throwable t) {
			CrashTracker.logThrowableCrash(t);
			throw t;
		}

	}

	@Override
	public void disabledInit() {
		try {
			CrashTracker.logDisabledInit();
			AutoChooser.disableAuto();
			mEnabledLooper.stop();
			mSubsystemManager.stop();
			RobotState.mMatchState = MatchState.DISABLED;
			Drive.getInstance().setOpenLoop(DriveSignal.NEUTRAL);
		} catch (Throwable t) {
			CrashTracker.logThrowableCrash(t);
			throw t;
		}
	}

	@Override
	public void autonomousInit() {
		try {
			CrashTracker.logAutoInit();
			System.out.println("Auto start timestamp: " + Timer.getFPGATimestamp());
			mSubsystemManager.zeroSensors();
			RobotState.mMatchState = MatchState.AUTO;
			mEnabledLooper.start();
			AutoChooser.startAuto();
		} catch (Throwable t) {
			CrashTracker.logThrowableCrash(t);
			throw t;
		}
	}

	@Override
	public void teleopInit() {
		try {
			CrashTracker.logTeleopInit();
			mSubsystemManager.zeroSensors();
			RobotState.mMatchState = MatchState.TELEOP;
			RobotState.mDriveControlState = DriveControlState.VELOCITY_SETPOINT;
			mEnabledLooper.start();
		} catch (Throwable t) {
			CrashTracker.logThrowableCrash(t);
			throw t;
		}
	}

	@Override
	public void testInit() {
	}


	@Override
	public void disabledPeriodic() {
		allPeriodic();
	}

	@Override
	public void autonomousPeriodic() {
		allPeriodic();
	}

	@Override
	public void teleopPeriodic() {
		allPeriodic();
	}

	@Override
	public void testPeriodic() {
	}


	public void allPeriodic() {
		try {
			mSubsystemManager.outputToSmartDashboard();
			mSubsystemManager.writeToLog();
			mEnabledLooper.outputToSmartDashboard();
		} catch (Throwable t) {
			CrashTracker.logThrowableCrash(t);
			throw t;
		}
	}
}
