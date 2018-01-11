/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;


/**
 * This file illustrates the concept of driving up to a line and then stopping.
 * It uses the common Pushbot hardware class to define the drive on the robot.
 * The code is structured as a LinearOpMode
 *
 * The code shows using two different light sensors:
 *   The Primary sensor shown in this code is a legacy NXT Light sensor (called "sensor_light")
 *   Alternative "commented out" code uses a MR Optical Distance Sensor (called "sensor_ods")
 *   instead of the LEGO sensor.  Chose to use one sensor or the other.
 *
 *   Setting the correct WHITE_THRESHOLD value is key to stopping correctly.
 *   This should be set half way between the light and dark values.
 *   These values can be read on the screen once the OpMode has been INIT, but before it is STARTED.
 *   Move the senso on asnd off the white line and not the min and max readings.
 *   Edit this code to make WHITE_THRESHOLD half way between the min and max.
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@Autonomous(name="Auto Blue Master - Do Not Run", group="DM18")
@Disabled
public class Auto_Blue_R_2G_Old extends LinearOpMode {

    HardwareDM18         robot   = new HardwareDM18 ();   // Use a Pushbot's hardware
    AutoHelper auto = new AutoHelper();

    RelicRecoveryVuMark vuMark;

    int left1Pos;
    int left2Pos;
    int right1Pos;
    int right2Pos;


    /**
     * The main routine of the OpMode.
     *
     * @throws InterruptedException
     */
    @Override
    public void runOpMode() throws InterruptedException {

        // Init the robot hardware -- including gyro and range finder
        robot.init(hardwareMap, true, true);

        // Start up all the common Auto stuff

        auto.init(robot, hardwareMap, this, iAmBlue());

        robot.intake.setStop();
        // Set a flipTimer of how often to update gyro status telemetry
        ElapsedTime updateGyroStartTimer = new ElapsedTime();
        updateGyroStartTimer.reset();

        while (!isStarted()) {
            if (updateGyroStartTimer.milliseconds() >= 500) {
                // Update telemetry every 0.5 seconds
                telemetry.addData("IMU calibrated: ", robot.adaGyro.isSystemCalibrated());
                telemetry.addData("IMU Gyro calibrated:  ", robot.adaGyro.isGyroCalibrated());

                // Do a gyro read to keep it "fresh"
                telemetry.addData("Gyro heading: ", auto.readGyro());
                telemetry.update();

                // And reset the flipTimer
                updateGyroStartTimer.reset();
            }
            idle();
        }

        telemetry.clearAll();

        // Startup vuforia and other start of auto tasks
        auto.processStartButton();

        RobotLog.i("DM10337- Auto Pressed Start");
        // Step through each leg of the path,

        // Make sure the gyro is zeroed
        auto.zeroGyro();

        RobotLog.i("DM10337 - Gyro bias set to " + auto.headingBias);

        robot.lift.resetFloorPos();


        /*
        JEWEL CODE
        */
        auto.processJewel();

        readVuMark();

        driveToBox();

        driveToPile();

        loadExtraGlyphs();

        returnToBox();

        placeExtraGlyphs();

        park();

        RobotLog.i("DM10337- Finished last move of auto");


        telemetry.addData("Path", "Complete");
        telemetry.update();
    }


    public void readVuMark() {
        vuMark = RelicRecoveryVuMark.from(auto.relicTemplate);

        telemetry.addData("VuMark", "%s visible", vuMark);

    }

    public void driveToBox() throws InterruptedException {

        if (vuMark == RelicRecoveryVuMark.CENTER || vuMark == RelicRecoveryVuMark.UNKNOWN) {
            // Drive forward to lineup with center cryptoglyph
            if (iAmBlue()) {
                auto.encoderDrive(0.5, 33.0, 5.0, true, 0.0);
            } else {
                auto.encoderDrive(0.5, -36.0, 5.0, true, 0.0);
            }

                    }
        if (vuMark == RelicRecoveryVuMark.RIGHT) {
            // Drive forward to lineup with center cryptoglyph
            if (iAmBlue()) {
                auto.encoderDrive(0.5, 33.0 + 7.5, 5.0, true, 0.0);
            } else {
                auto.encoderDrive(0.5, -36.0 + 7.5, 5.0, true, 0.0);
            }

        }
        if (vuMark == RelicRecoveryVuMark.LEFT) {
            // Drive forward to lineup with center cryptoglyph
            if (iAmBlue()) {
                auto.encoderDrive(0.5, 33.0 - 7.5, 5.0, true, 0.0);
            } else {
                auto.encoderDrive(0.5, -36.0 - 7.5, 5.0, true, 0.0);
            }

        }

        // Turn toward center cryptoglyph
        auto.gyroTurn(0.8, 90, auto.P_TURN_COEFF);
        // Outake glyph
        robot.gripper.setBothOpen();
        sleep(250);
        // Drive closer to center cryptoglyph
        auto.encoderDrive(0.5, 7.0, 3.0, true, 90);

        robot.intake.setOut();
        sleep(500);
    }

    public void park() throws InterruptedException {
        // Drive back, but stay in safe zone
        auto.encoderDrive(0.6, -8.0, 3.0, true, 90);

        robot.intake.setStop();
    }

    public void driveToPile() {
        auto.gyroTurn(0.8,-90, auto.P_TURN_COEFF);

        // Record drive motor encoder positions. Use these values to return to this position after collecting glyphs
        left1Pos = robot.leftDrive1.getCurrentPosition();
        left2Pos = robot.leftDrive2.getCurrentPosition();
        right1Pos = robot.rightDrive1.getCurrentPosition();
        right2Pos = robot.rightDrive2.getCurrentPosition();

        robot.lift.setLiftMid();

        // Drive forward to collect glyph
        auto.collectGlyph(0.3, 18,3, true, -90);

    }

    public void loadExtraGlyphs() throws InterruptedException {
        // Check if glyph is in intake
        if (robot.intake.distLeft() < 12.0 || robot.intake.distRight() < 12.0) {

            // square glyph
            auto.squareGlyph(1.0, -0.15, 500);

            // intake on to hold glyph while driving back
            robot.intake.intakeLeftMotor.setPower(0.30);
            robot.intake.intakeRightMotor.setPower(0.30);
            // drive backwards to avoid interference from other glyphs during load
            auto.encoderDrive(0.3, -4.0, 2.0, true, -90);

            robot.intake.setStop();

            // Auto load glyph
            auto.autoLoadFirstGlyph();
            auto.glyphsCollected = 1;

        } else {
            // never detected glyph in intake. Back off and set intake out to clear any potential jams.
            robot.intake.setOut();
            auto.encoderDrive(0.3, -7.0, 3.0, true, -90);
            robot.intake.setStop();
        }
    }

    public void returnToBox() throws InterruptedException {
        int inches = auto.determineDistance(left1Pos, left2Pos, right1Pos, right2Pos);

        auto.encoderDrive(0.8, -inches, 5, true, -90);

        // Turn toward cryptobox
        auto.gyroTurn(0.8, 90, auto.P_TURN_COEFF);

        auto.encoderDrive(0.8, 16, 5, true, 90);

    }

    public void placeExtraGlyphs(){
        sleep (250);

        if (auto.glyphsCollected > 0){

            // Extend gripper out
            robot.gripper.setExtendOut();

            while(robot.gripper.isExtending()) idle();

            // Drop glyphs
            robot.gripper.setBothOpen();
        }

        while (robot.gripper.isMoving()) idle();



    }

    public boolean iAmBlue() {
        return true;
    }


}
