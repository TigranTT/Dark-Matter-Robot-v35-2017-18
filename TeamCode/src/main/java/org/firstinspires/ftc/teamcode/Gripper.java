package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoControllerEx;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

/**
 *    Everything related to the gripper
 */
public class Gripper {

    // Hardware
    public Servo purpleGrip = null;
    public Servo blackGrip = null;
    public Servo rotateServo = null;
    public Servo extendGrip = null;

    // Servo constants
    public final static double B_GRIP_OPEN = 0.71;
    public final static double B_GRIP_PARTIAL_OPEN = 0.71; // Changed to same as full close
    public final static double B_GRIP_CLOSED = 0.234;
    public final static double P_GRIP_OPEN = 0.71;
    public final static double P_GRIP_PARTIAL_OPEN = 0.71;
    public final static double P_GRIP_CLOSED = 0.234;

    public final static double GRIP_ROTATE_NORMAL = 0.8947;
    public final static double GRIP_ROTATE_FLIPPED = 0.0;
    public final static double GRIP_EXTEND_HOME = 0.48;   // Savox 1256tg 0.93
    public final static double GRIP_EXTEND_OUT = 0.87;  // Savox 1256tg 0.55
    public final static double GRIP_EXTEND_INIT = 0.603;  // Savox 1256tg 0.7
    public final static double FLIP_TIME = 600;        // 600 ms for servo to flip gripper
    public final static double GRIP_TIME_GRAB = 225;        // 225 ms timer for grip to complete grab
    public final static double GRIP_TIME_RELEASE = 700;    // 700 ms timer for grip to complete release
    public final static double EXTEND_TIME = 250;

    /* Gripper state variables */
    Servo topGrip = purpleGrip;        // Should start w/ purple gripper on top
    Servo btmGrip = blackGrip;         // and black on bottom
    boolean isGripFlipped = false;

    /* Flip flipTimer */
    ElapsedTime flipTimer = new ElapsedTime();
    ElapsedTime purpleTimer = new ElapsedTime();
    ElapsedTime blackTimer = new ElapsedTime();
    ElapsedTime extendTimer = new ElapsedTime();
    ElapsedTime topTimer = null;
    ElapsedTime btmTimer = null;

    /**
     * Constructor
     */
    public void Gripper() {
        // Do nothing
    }

    /**
     * Initialize the gripper
     *
     * @param hw  Hardwaremap for our robot
     * @param pg  Name of purple gripper servo
     * @param bg  Name of black gripper servo
     * @param rot Name of gripper rotation servo
     */
    public void init(HardwareMap hw, String pg, String bg, String rot, String ext) {
        // Define and Initialize gripper servos
        purpleGrip = hw.servo.get(pg);
        blackGrip = hw.servo.get(bg);
        rotateServo = hw.servo.get(rot);
        extendGrip = hw.servo.get(ext);

        // Set the rotation servo for extended PWM range
        if (rotateServo.getController() instanceof ServoControllerEx) {
            // Confirm its an extended range servo controller before we try to set to avoid crash
            ServoControllerEx theControl = (ServoControllerEx) rotateServo.getController();
            int thePort = rotateServo.getPortNumber();
            PwmControl.PwmRange theRange = new PwmControl.PwmRange(553, 2500);
            theControl.setServoPwmRange(thePort, theRange);
        }

        // Set the black grip servo for extended PWM range
        if (blackGrip.getController() instanceof ServoControllerEx) {
            // Confirm its an extended range servo controller before we try to set to avoid crash
            ServoControllerEx theControl = (ServoControllerEx) blackGrip.getController();
            int thePort = blackGrip.getPortNumber();
            PwmControl.PwmRange theRange = new PwmControl.PwmRange(553, 2500);
            theControl.setServoPwmRange(thePort, theRange);
        }

        // Set the purple grip servo for extended PWM range
        if (purpleGrip.getController() instanceof ServoControllerEx) {
            // Confirm its an extended range servo controller before we try to set to avoid crash
            ServoControllerEx theControl = (ServoControllerEx) purpleGrip.getController();
            int thePort = rotateServo.getPortNumber();
            PwmControl.PwmRange theRange = new PwmControl.PwmRange(553, 2500);
            theControl.setServoPwmRange(thePort, theRange);
        }

        // Start with purple on top
        topGrip = purpleGrip;
        btmGrip = blackGrip;
        topTimer = purpleTimer;
        btmTimer = blackTimer;
        isGripFlipped = false;

        setFlipped(false);
        setExtendInitPosition();
        //setExtendIn();
        setBothClosed();
    }

    /**
     * Open the purple gripper
     */
    public void setPurpleOpen() {

        purpleGrip.setPosition(P_GRIP_OPEN);
        purpleTimer.reset();
    }

    /**
     * Open the black gripper
     */
    public void setBlackOpen() {

        blackGrip.setPosition(B_GRIP_OPEN);
        blackTimer.reset();
    }

    /**
     * Open whichever gripper is currently on top
     */
    public void setTopOpen() {

        RobotLog.i("DM10337 -- Gripper TOP set to OPEN");
        topGrip.setPosition(isGripFlipped ? B_GRIP_OPEN: P_GRIP_OPEN);
        topTimer.reset();
    }

    /**
     * Open whichever gripper is currently on bottom
     */
    public void setBtmOpen() {

        RobotLog.i("DM10337 -- Gripper BOTTOM set to OPEN");
        btmGrip.setPosition(isGripFlipped ? P_GRIP_OPEN: B_GRIP_OPEN);
        btmTimer.reset();
    }

    /**
     * Open both grippers
     */
    public void setBothOpen() {
        setTopOpen();
        setBtmOpen();
    }

    /**
     * Open both grippers partially to score without interferring with other stacked glyphs
     */
    public void setBothPartialOpen() {
        setBtmPartialOpen();
        setTopPartialOpen();
    }

    public void setBtmPartialOpen() {
        RobotLog.i("DM10337 -- Gripper BOTTOM set to PARTIAL OPEN");
        btmGrip.setPosition(isGripFlipped ? P_GRIP_PARTIAL_OPEN: B_GRIP_PARTIAL_OPEN);
        btmTimer.reset();
    }

    public void setTopPartialOpen() {
        RobotLog.i("DM10337 -- Gripper TOP set to PARTIAL OPEN");
        topGrip.setPosition(isGripFlipped ? B_GRIP_PARTIAL_OPEN: P_GRIP_PARTIAL_OPEN);
        topTimer.reset();
    }

    /**
     * Extend gripper
     */
    public void setExtendOut() {

        RobotLog.i("DM10337 -- Gripper set to OUT position");
        extendGrip.setPosition(GRIP_EXTEND_OUT);
        extendTimer.reset();
    }

    public void moveInOut(double speed) {
        speed = Range.clip(speed, -1, 1);
        double current = extendGrip.getPosition();
        double target =  current + ((Math.abs(GRIP_EXTEND_HOME - GRIP_EXTEND_OUT) * speed) / 3.5);   // At full stick will take 20 cycles
        target = Range.clip(target, GRIP_EXTEND_HOME, GRIP_EXTEND_OUT); // min max reversed if using Savox 1256tg
        extendGrip.setPosition(target);
        extendTimer.reset();
    }

    /**
     * Retract gripper
     */
    public void setExtendIn() {
        RobotLog.i("DM10337 -- Pusher set to HOME position");
        extendGrip.setPosition(GRIP_EXTEND_HOME);
        extendTimer.reset();
    }

    public void setExtendInitPosition() {
        RobotLog.i("DM10337 -- Pusher set to INIT position");
        extendGrip.setPosition(GRIP_EXTEND_INIT);
    }
    /**
     * Close the purple gripper
     */
    public void setPurpleClosed() {

        purpleGrip.setPosition(P_GRIP_CLOSED);
        purpleTimer.reset();
    }

    /**
     * Close the black gripper
     */
    public void setBlackClosed() {

        blackGrip.setPosition(B_GRIP_CLOSED);
        blackTimer.reset();
    }

    /**
     * Close whichever gripper is currently on top
     */
    public void setTopClosed() {

        RobotLog.i("DM10337 -- Gripper TOP set to CLOSED");
        topGrip.setPosition(isGripFlipped ? B_GRIP_CLOSED: P_GRIP_CLOSED);
        topTimer.reset();
    }

    /**
     * Close whichever gripper is currently on bottom
     */
    public void setBtmClosed() {
        RobotLog.i("DM10337 -- Gripper BOTTOM set to CLOSED");
        btmGrip.setPosition(isGripFlipped ? P_GRIP_CLOSED: B_GRIP_CLOSED);
        btmTimer.reset();
    }

    /**
     * Close both grippers
     */
    public void setBothClosed() {
        setTopClosed();
        setBtmClosed();
    }


    public boolean isTopClosed() {
        return (almostEqual(topGrip.getPosition(), (isGripFlipped ? B_GRIP_CLOSED: P_GRIP_CLOSED)));
    }

    public boolean isBtmClosed() {
        return (almostEqual(btmGrip.getPosition(), isGripFlipped ? P_GRIP_CLOSED: B_GRIP_CLOSED));
    }

    public boolean isPurpleClosed() {
        return (almostEqual(purpleGrip.getPosition(), B_GRIP_CLOSED));
    }

    public boolean isBlackClosed() {
        return (almostEqual(blackGrip.getPosition(), B_GRIP_CLOSED));
    }

    public boolean isBtmOpen() { return (almostEqual(btmGrip.getPosition(), isGripFlipped ? P_GRIP_OPEN: B_GRIP_OPEN)); }

    public boolean isTopOpen() { return (almostEqual(topGrip.getPosition(), isGripFlipped ? B_GRIP_OPEN: P_GRIP_OPEN)); }

    public boolean isBtmPartialOpen() { return (almostEqual(btmGrip.getPosition(), isGripFlipped ? P_GRIP_PARTIAL_OPEN: B_GRIP_PARTIAL_OPEN)); }

    public boolean isTopPartialOpen() { return (almostEqual(topGrip.getPosition(), isGripFlipped ? B_GRIP_PARTIAL_OPEN: P_GRIP_PARTIAL_OPEN)); }

    public boolean isPusherOut() { return (Math.abs(extendGrip.getPosition() - GRIP_EXTEND_HOME) > 0.02); }

    public void flip() {
        RobotLog.i("DM10337 -- Flipping Gripper");
        if (isGripFlipped) {
            // Was flipped so turn it back upright
            setFlipped(false);
        } else {
            // Was not flipped so turn it upside down
            setFlipped(true);
        }

    }

    public void setFlipped(boolean flipped) {
        if (flipped) {
            rotateServo.setPosition(GRIP_ROTATE_FLIPPED);
            isGripFlipped = true;
            topGrip = blackGrip;
            btmGrip = purpleGrip;
            RobotLog.i("DM10337 -- Gripper FLIPPED. Black Up!");
        }
        else {
            rotateServo.setPosition(GRIP_ROTATE_NORMAL);
            isGripFlipped = false;
            topGrip = purpleGrip;
            btmGrip = blackGrip;
            RobotLog.i("DM10337 -- Gripper FLIPPED. Purple Up!");
        }
        flipTimer.reset();          // Start a flipTimer so we can check later if it might be moving
    }

    public boolean isFlipping() { return (flipTimer.milliseconds() < FLIP_TIME); }

    public boolean topIsMoving() {
        return (topTimer.milliseconds() < GRIP_TIME_GRAB);
    }

    public boolean btmIsMoving() { return (btmTimer.milliseconds() < GRIP_TIME_GRAB);
    }

    public boolean purpleIsMoving() {
        return (purpleTimer.milliseconds() < GRIP_TIME_GRAB);
    }

    public boolean blackIsMoving() {
        return (blackTimer.milliseconds() < GRIP_TIME_GRAB);
    }

    public boolean isMoving() {
        return (purpleIsMoving() || blackIsMoving() || btmIsMoving() || topIsMoving() || isFlipping());
    }


    public boolean topIsReleasing() {
        return (topTimer.milliseconds() < GRIP_TIME_RELEASE);
    }

    public boolean btmIsReleasing() { return (btmTimer.milliseconds() < GRIP_TIME_RELEASE);
    }

    public boolean purpleIsReleasing() {
        return (purpleTimer.milliseconds() < GRIP_TIME_RELEASE);
    }

    public boolean blackIsReleasing() {
        return (blackTimer.milliseconds() < GRIP_TIME_RELEASE);
    }

    public boolean isReleasing() {
        return (purpleIsReleasing() || blackIsReleasing() || btmIsReleasing() || topIsReleasing() || isFlipping());
    }


    public boolean isExtending() { return (extendTimer.milliseconds() < EXTEND_TIME);}

    /**
     * This is designed to be used for manual tuning of servo Min/Max constants not for routine use
     *
     * @return current top grip servo position
     */
    public double getTopServoPos() {
        return topGrip.getPosition();
    }

    /**
     * This is designed to be used for manual tuning of servo Min/Max constants not for routine use
     *
     * @return current btm grip servo position
     */

    public double getBtmServoPos() {
        return btmGrip.getPosition();
    }

    /**
     * This is designed to be used for manual tuning of servo Min/Max constants not for routine use
     *
     * @param pos desired top grip servo position
     */
    public void setTopServoPos(double pos) {
        pos = Range.clip(pos, 0.0, 1.0);
        topGrip.setPosition(pos);
    }

    /**
     * This is designed to be used for manual tuning of servo Min/Max constants not for routine use
     *
     * @param pos desired bottom grip servo position
     */
    public void setBtmServoPos(double pos) {
        pos = Range.clip(pos, 0.0, 1.0);
        btmGrip.setPosition(pos);
    }


    /**
     * This is designed to be used for manual tuning of servo Min/Max constants not for routine use
     *
     * @return current rotation servo position
     */
    public double getRotServoPos() {
        return rotateServo.getPosition();
    }

    /**
     * This is designed to be used for manual tuning of servo Min/Max constants not for routine use
     *
     * @param pos desired rotate servo position
     */
    public void setRotServoPos(double pos) {
        pos = Range.clip(pos, 0.0, 1.0);
        rotateServo.setPosition(pos);
    }


    public boolean almostEqual(double val1, double val2) {
        return (Math.abs(val1 - val2) < 0.02);
    }
}

