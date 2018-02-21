package frc.team1836.robot.subsystems;

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.team1836.robot.Constants;
import frc.team1836.robot.Constants.LOGGING;
import frc.team1836.robot.RobotState;
import frc.team1836.robot.util.drivers.MkLED;
import frc.team1836.robot.util.logging.ReflectingCSVWriter;
import frc.team1836.robot.util.loops.Loop;
import frc.team1836.robot.util.loops.Looper;
import frc.team1836.robot.util.other.Subsystem;

public class Superstructure extends Subsystem {

	private final ReflectingCSVWriter<SupertructureDebugOutput> mCSVWriter;
	PowerDistributionPanel pdp = new PowerDistributionPanel();
	private SupertructureDebugOutput mDebug = new SupertructureDebugOutput();
	private float _hue;
	private MkLED mkLED;

	public Superstructure() {
		mCSVWriter = new ReflectingCSVWriter<>(LOGGING.SUPERSTRUCTURE_LOG_PATH,
				SupertructureDebugOutput.class);
		mkLED = new MkLED(Constants.CANIFIER_ID);
	}

	public static Superstructure getInstance() {
		return InstanceHolder.mInstance;
	}

	@Override
	public void writeToLog() {
		mCSVWriter.write();
	}

	@Override
	public void outputToSmartDashboard() {
		SmartDashboard.putString("System State", RobotState.mSystemState.toString());
		updateLEDStrip();
	}

	@Override
	public void stop() {

	}

	@Override
	public void zeroSensors() {

	}

	@Override
	public void checkSystem() {
		if (pdp.getVoltage() < 10) {
			System.out.println("FAILED - PDP VOLTAGE LOW");
		}
	}

	@Override
	public void registerEnabledLoops(Looper enabledLooper) {
		Loop mLoop = new Loop() {

			@Override
			public void onStart(double timestamp) {
				synchronized (Superstructure.this) {
					pdp.clearStickyFaults();
				}
			}

			@Override
			public void onLoop(double timestamp) {
				synchronized (Superstructure.this) {
					updateLEDStrip();
					switch (RobotState.mSystemState) {
						case IDLE:
							break;
						default:
							break;
					}
					mDebug.SuperstructureState = RobotState.mSystemState.toString();
					mDebug.PDPVoltage = 0;
					mDebug.PDPTotalPower = 0;
					mDebug.PDPTemp = 0;
					mDebug.timestamp = timestamp;
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

	private void updateLEDStrip() {
		switch (RobotState.mMatchState) {
			case AUTO:
				mkLED.set_rgb(0, 66, 255);
			case TELEOP:
				float[] pulse1 = {4,255,0};
				float[] pulse2 = {0,251,255};
				mkLED.setPulse(pulse1, pulse2);
			case DISABLED:
				mkLED.set_rgb(255, 0, 0);
		}

	}

	public void setLED(float[] color) {
		mkLED.set_rgb(color[0], color[1], color[2]);
	}


	public static class SupertructureDebugOutput {

		public String SuperstructureState;
		public double PDPVoltage;
		public double PDPTotalPower;
		public double PDPTemp;
		public double timestamp;
	}

	private static class InstanceHolder {

		private static final Superstructure mInstance = new Superstructure();

	}
}
