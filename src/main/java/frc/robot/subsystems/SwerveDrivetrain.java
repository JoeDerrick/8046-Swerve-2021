// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.sensors.CANCoder;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.ctre.phoenix.motorcontrol.*;
import edu.wpi.first.wpilibj.Joystick;


import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.SwerveDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.util.Units;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class SwerveDrivetrain extends SubsystemBase {

  public static final double kMaxSpeed = Units.feetToMeters(13.6); // 13.6 feet per second
  public static final double kMaxAngularSpeed = Math.PI; // 1/2 rotation per second

  //-------Pigeon implementation----------------//move to line 71?
  PigeonIMU _pidgey;
  TalonSRX _pigeonTalon = new TalonSRX(10); // added to support the pigeon, although maybe we should wire it into CAN
  
  int _axisSelection = 0; //!< [0,2] => [Yaw,Pitch,Roll]
  int _signalSelection = 0;  //!< [0,7] => What signal to print, see Instrum implem
  boolean _printEnable = true; //!< True => print signal values periodically

  /* timeouts for certain blocking actions */
  final int kTimeoutMs = 50;

  //--------End Pigeon------------------//

  //---------Motor Tuning Implementation-------------//
  Joystick _joy = new Joystick(0);
    
  /* String for output */
  StringBuilder _sb = new StringBuilder();
  
  /* Loop tracker for prints */
int _loops = 0;

  //-----------End Motor Tuning Implementation---------//

  /**
   * TODO: These are example values and will need to be adjusted for your robot!
   * Modules are in the order of -
   * Front Left
   * Front Right
   * Back Left
   * Back Right
   * 
   * Positive x values represent moving toward the front of the robot whereas
   * positive y values represent moving toward the left of the robot
   * https://docs.wpilib.org/en/stable/docs/software/kinematics-and-odometry/swerve-drive-kinematics.html#constructing-the-kinematics-object
   */
  private SwerveDriveKinematics kinematics = new SwerveDriveKinematics(

  //------ Not sure why "10" is this an arbitrary value for width?----- jd - 6/28/21----//
    new Translation2d(
      Units.inchesToMeters(10),
      Units.inchesToMeters(10)
    ),
    new Translation2d(
      Units.inchesToMeters(10),
      Units.inchesToMeters(-10)
    ),
    new Translation2d(
      Units.inchesToMeters(-10),
      Units.inchesToMeters(10)
    ),
    new Translation2d(
      Units.inchesToMeters(-10),
      Units.inchesToMeters(-10)
    )
  );

  private final AnalogGyro gyro = new AnalogGyro(0);
  
  
  // DONE: Update these CAN device IDs to match your TalonFX + CANCoder device IDs
  // TODO: Update module offsets to match your CANCoder offsets// what are these?
  private SwerveModuleMK3[] modules = new SwerveModuleMK3[] {
    new SwerveModuleMK3(new TalonFX(1), new TalonFX(2), new CANCoder(11), Rotation2d.fromDegrees(0)), // Front Left
    new SwerveModuleMK3(new TalonFX(3), new TalonFX(4), new CANCoder(13), Rotation2d.fromDegrees(0)), // Front Right
    new SwerveModuleMK3(new TalonFX(5), new TalonFX(6), new CANCoder(15), Rotation2d.fromDegrees(0)), // Back Left
    new SwerveModuleMK3(new TalonFX(7), new TalonFX(8), new CANCoder(17), Rotation2d.fromDegrees(0))  // Back Right
  };

  public SwerveDrivetrain() {
    gyro.reset();
    // attempt to setup pigeon for gyro heading JD-6/28/21----//

    _pidgey = new PigeonIMU(_pigeonTalon);
    _pidgey.configFactoryDefault();
    _pidgey.setYaw(0,kTimeoutMs);
    _pidgey.setAccumZAngle(0,kTimeoutMs);

    
  }

  /**
   * Method to drive the robot using joystick info.
   *
   * @param xSpeed Speed of the robot in the x direction (forward).
   * @param ySpeed Speed of the robot in the y direction (sideways).
   * @param rot Angular rate of the robot.
   * @param fieldRelative Whether the provided x and y speeds are relative to the field.
   */
  public void drive(double xSpeed, double ySpeed, double rot, boolean fieldRelative) {
    SwerveModuleState[] states =
      kinematics.toSwerveModuleStates(
        fieldRelative
          ? ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, rot,gyro.getRotation2d())//gyro.getRotation2d()
          : new ChassisSpeeds(xSpeed, ySpeed, rot));
    SwerveDriveKinematics.normalizeWheelSpeeds(states, kMaxSpeed);
    for (int i = 0; i < states.length; i++) {
      SwerveModuleMK3 module = modules[i];
      SwerveModuleState state = states[i];
      module.setDesiredState(state);
    }
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    // woud like to output angle for diagnoistics
      
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }
}
