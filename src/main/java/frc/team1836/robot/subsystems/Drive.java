package frc.team1836.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SPI;
import frc.team1836.robot.Constants.DRIVE;
import frc.team1836.robot.util.drivers.MkDrive;
import frc.team1836.robot.util.drivers.MkGyro;
import frc.team1836.robot.util.logging.ReflectingCSVWriter;
import frc.team1836.robot.util.loops.Loop;
import frc.team1836.robot.util.loops.Looper;
import frc.team1836.robot.util.state.DriveSignal;
import frc.team1836.robot.util.state.TrajectoryStatus;
import frc.team254.lib.trajectory.Path;
import frc.team254.lib.trajectory.PathFollower;

public class Drive extends Subsystem {

	private static Drive mInstance = new Drive();
	private final ReflectingCSVWriter<DriveDebugOutput> mCSVWriter;
	private final MkDrive leftDrive, rightDrive;
	private final MkGyro navX;
	private DriveControlState mDriveControlState;
	private PathFollower pathFollower = null;
	private DriveDebugOutput mDebug = new DriveDebugOutput();
	private TrajectoryStatus leftStatus;
	private TrajectoryStatus rightStatus;
	private DriveSignal currentSetpoint;

	private Drive() {
		leftDrive = new MkDrive(DRIVE.LEFT_MASTER_ID, DRIVE.LEFT_SLAVE_ID);
		rightDrive = new MkDrive(DRIVE.RIGHT_MASTER_ID, DRIVE.RIGHT_SLAVE_ID);
		navX = new MkGyro(new AHRS(SPI.Port.kMXP));

		mDriveControlState = DriveControlState.OPEN_LOOP;
		mCSVWriter = new ReflectingCSVWriter<>("/home/lvuser/DRIVE-LOGS.csv",
				DriveDebugOutput.class);
		leftStatus = TrajectoryStatus.NEUTRAL;
		rightStatus = TrajectoryStatus.NEUTRAL;
		currentSetpoint = DriveSignal.NEUTRAL;
	}

	public static Drive getInstance() {
		return mInstance;
	}

	private synchronized void setOpenLoop(DriveSignal signal) {
		mDriveControlState = DriveControlState.OPEN_LOOP;
		leftDrive.set(ControlMode.PercentOutput, signal.getLeft());
		rightDrive.set(ControlMode.PercentOutput, signal.getRight());
	}

	private synchronized void setVelocitySetpoint(DriveSignal signal) {
		mDriveControlState = DriveControlState.VELOCITY_SETPOINT;
		leftDrive.set(ControlMode.Velocity, signal.getLeftNativeVel());
		rightDrive.set(ControlMode.Velocity, signal.getLeftNativeVel());
	}

	public synchronized void setWantDrivePath(Path path, double dist_tol, double ang_tol) {
		mDriveControlState = DriveControlState.PATH_FOLLOWING;
		pathFollower = new PathFollower(path, dist_tol, ang_tol);
	}

	@Override
	public void writeToLog() {
		mCSVWriter.write();
	}

	@Override
	public void outputToSmartDashboard() {

	}

	@Override
	public void stop() {
		setOpenLoop(DriveSignal.NEUTRAL);
	}

	@Override
	public void zeroSensors() {
		leftDrive.resetEncoder();
		rightDrive.resetEncoder();
	}

	@Override
	public void checkSystem() {
		leftDrive.testDrive();
		rightDrive.testDrive();
		if (!navX.isConnected()) {
			System.out.println("FAILED - NAVX DISCONNECTED");
		}

	}

	@Override
	public void registerEnabledLoops(Looper enabledLooper) {
		Loop mLoop = new Loop() {

			@Override
			public void onStart(double timestamp) {
				synchronized (Drive.this) {

				}
			}

			@Override
			public void onLoop(double timestamp) {
				synchronized (Drive.this) {
					updateDebugOutput(timestamp);
					switch (mDriveControlState) {
						case OPEN_LOOP:
							zeroTrajectoryStatus();
							return;
						case VELOCITY_SETPOINT:
							zeroTrajectoryStatus();
							return;
						case PATH_FOLLOWING:
							updatePathFollower();
							updateTrajectoryStatus();
							return;
						default:
							System.out.println("Unexpected drive control state: " + mDriveControlState);
							break;
					}
					mCSVWriter.add(mDebug);
				}
			}

			@Override
			public void onStop(double timestamp) {
				stop();
			}
		};
		enabledLooper.register(mLoop);
	}

	private void updateDebugOutput(double timestamp) {
		mDebug.timestamp = timestamp;
		mDebug.controlMode = mDriveControlState.toString();
		mDebug.leftOutput = leftDrive.getPercentOutput();
		mDebug.rightOutput = rightDrive.getPercentOutput();
		mDebug.rightPosition = leftDrive.getPosition();
		mDebug.leftPosition = rightDrive.getPosition();
		mDebug.leftVelocity = leftDrive.getSpeed();
		mDebug.rightVelocity = rightDrive.getSpeed();
		mDebug.heading = navX.getFullYaw();
	}

	private void updatePathFollower() {
		TrajectoryStatus leftUpdate = pathFollower
				.getLeftVelocity(leftDrive.getPosition(), leftDrive.getSpeed(),
						Math.toRadians(navX.getFullYaw()));
		TrajectoryStatus rightUpdate = pathFollower
				.getRightVelocity(rightDrive.getPosition(), rightDrive.getSpeed(),
						Math.toRadians(navX.getFullYaw()));
		setVelocitySetpoint(new DriveSignal(leftUpdate.getOutput(), rightUpdate.getOutput()));
	}

	private void zeroTrajectoryStatus() {
		mDebug.leftDesiredPos = 0;
		mDebug.leftDesiredVel = 0;
		mDebug.rightDesiredPos = 0;
		mDebug.rightDesiredVel = 0;
		mDebug.desiredHeading = 0;
		mDebug.headingError = 0;
		mDebug.leftVelError = 0;
		mDebug.leftPosError = 0;
		mDebug.rightVelError = 0;
		mDebug.rightPosError = 0;
		mDebug.desiredX = 0;
		mDebug.desiredY = 0;
	}

	private void updateTrajectoryStatus() {
		mDebug.leftDesiredPos = leftStatus.getSeg().pos;
		mDebug.leftDesiredVel = leftStatus.getSeg().vel;
		mDebug.rightDesiredPos = rightStatus.getSeg().pos;
		mDebug.rightDesiredVel = rightStatus.getSeg().vel;
		mDebug.desiredHeading = leftStatus.getSeg().heading;
		mDebug.headingError = leftStatus.getAngError();
		mDebug.leftVelError = leftStatus.getVelError();
		mDebug.leftPosError = leftStatus.getPosError();
		mDebug.rightVelError = rightStatus.getVelError();
		mDebug.rightPosError = rightStatus.getPosError();
		mDebug.desiredX = (leftStatus.getSeg().x + rightStatus.getSeg().x) / 2;
		mDebug.desiredY = (leftStatus.getSeg().y + rightStatus.getSeg().y) / 2;
	}

	public enum DriveControlState {
		OPEN_LOOP, // open loop voltage control
		VELOCITY_SETPOINT, // velocity PID control
		PATH_FOLLOWING, // used for autonomous driving
	}

	private static class DriveDebugOutput {

		public double timestamp;
		public String controlMode;
		public double leftOutput;
		public double rightOutput;
		public double leftSetpoint;
		public double rightSetpoint;
		public double leftPosition;
		public double rightPosition;
		public double leftVelocity;
		public double rightVelocity;
		public double heading;
		public double desiredHeading;
		public double headingError;
		public double leftDesiredVel;
		public double leftDesiredPos;
		public double leftPosError;
		public double leftVelError;
		public double rightDesiredVel;
		public double rightDesiredPos;
		public double rightPosError;
		public double rightVelError;
		public double desiredX;
		public double desiredY;
	}

}
