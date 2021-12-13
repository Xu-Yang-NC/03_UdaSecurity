package com.udacity.catpoint.security;


import com.udacity.catpoint.image.service.ImageService;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import com.udacity.catpoint.security.service.SecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    private SecurityService securityService;

    @Mock
    ImageService imageService;

    @Mock
    SecurityRepository securityRepository;

    @Mock
    Sensor sensor;

    @Mock
    StatusListener statusListener;



    @BeforeEach
    void init(){
        securityService = new SecurityService(securityRepository,imageService);
        sensor =  new Sensor(UUID.randomUUID().toString(), SensorType.DOOR);
    }

    // 1. Alarm armed and sensor activated, return alarm status pending.
    @Test
    void alarmArmedAndSensorActivated_systemReturnPending(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.changeSensorActivationStatus(sensor,true);
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    // 2. Alarm armed, sensor activated, and system pending, return alarm status
    // to alarm.
    @Test
    void systemPendingAlarmArmedSensorActivated_returnAlarmToAlarm(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.changeSensorActivationStatus(sensor,true);
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    // 3. Pending alarm and all sensors are inactive, return to no alarm state.
    @Test
    void pendingAlarmAndSensorsInactivated_returnNoAlarm(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor,false);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // 4. If alarm is active, change in sensor state should not affect the alarm
    // state.
    @ParameterizedTest
    @ValueSource(booleans = {true,false})
    void changingSensorState_returnNoChangingInAlarm(boolean flag){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor,flag);
        verify(securityRepository,never()).setAlarmStatus(AlarmStatus.ALARM);

    }

    // 5. If a sensor is activated while already active and the system is in
    // pending state, change it to alarm state.
    @Test
    void activeSensorAlreadyActivatedAndAlarmPending_returnAlarmStatusAlarm(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor,true);
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    // 6. If a sensor is deactivated while already inactive, make no changes to
    // the alarm state.
    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    void deactivatedSensorAlreadyInactive_returnNoChangeToAlarmState(AlarmStatus state){
        when(securityRepository.getAlarmStatus()).thenReturn(state);
        securityService.changeSensorActivationStatus(sensor,false);
        verify(securityRepository,never()).setAlarmStatus(state);
    }

    // 7. If the image service identifies an image containing a cat while the
    // system is armed-home, put the system into alarm status.
    @Test
    void systemArmedHomeCatImageDetected_returnAlarmStatusAlarm(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    // 8. If the image service identifies an images that does not contain a cat,
    // change the status to no alarm as long as the sensors are not active.
    @Test
    void noCatDetectedAndSensorInactive_returnAlarmStatusNoAlarm(){
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // 9. If the system is disarmed, set the status to no alarm.
    @Test
    void systemDisarmed_returnAlarmStatusNoAlarm(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // 10. If the system is armed, reset all sensors to inactive.
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class,names = {"ARMED_HOME", "ARMED_AWAY"})
    void systemArmed_resetAllSensorsToInactive(ArmingStatus status){
        securityService.setArmingStatus(status);
        Assertions.assertTrue(securityService.getSensors().stream().allMatch(sensor -> Boolean.FALSE.equals(sensor.getActive())));
    }

    // 11. If the system is armed-home while the camera shows a cat, set the alarm
    // status to alarm.
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class,names = {"ARMED_HOME", "ARMED_AWAY"})
    void systemArmedHomeAndCatDetected_returnAlarmStatusAlarm(ArmingStatus armingStatus){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(true);
        securityService.processImage(mock(BufferedImage.class));
        securityService.setArmingStatus(armingStatus);
        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void addAndRemoveStatusListener(){
        securityService.addStatusListener(statusListener);
        Assertions.assertTrue(securityService.getStatusListeners().contains(statusListener));
        securityService.removeStatusListener(statusListener);
        Assertions.assertFalse(securityService.getStatusListeners().contains(statusListener));
    }

    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    void callGetAlarmStatus_returnAlarmStatus(AlarmStatus status){
        when(securityRepository.getAlarmStatus()).thenReturn(status);
        Assertions.assertEquals(status,securityService.getAlarmStatus());
    }

    @Test
    void addAndRemoveSensor(){
        securityService.addSensor(sensor);
        verify(securityRepository,times(1)).addSensor(sensor);
        securityService.removeSensor(sensor);
        verify(securityRepository,times(1)).removeSensor(sensor);

    }

    @Test
    void resetSensors_sensorsReturnInactive(){
        securityService.resetAllSensorInactive();
        Assertions.assertTrue(securityService.getSensors().stream().allMatch(sensor -> Boolean.FALSE.equals(sensor.getActive())));
    }


}

