package com.yang.serialport.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.yang.serialport.ui.CarMainFrame;

/**
 *
 * @author Sayming-Hong
 * @date 2023-12-13
 */
public class WorkTimerUtils {

    private ScheduledExecutorService scheduler;
    private long secondsPassed = 0;
    private CarMainFrame carMainFrame;

    public WorkTimerUtils(CarMainFrame carMainFrame) {
        this.carMainFrame = carMainFrame;
        
        this.scheduler = Executors.newScheduledThreadPool(1,  new ThreadFactory()
        {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true); // 设置为守护线程
                return t;
            }
        });
    }
    
    /**
     * 获取当前时间
     * @return
     */
    public long getSecond() {
        return secondsPassed;
    }

    public void startTimer(int cancellationDelaySeconds) {
        Runnable timerTask = new Runnable() {
            @Override
            public void run() {
                secondsPassed++;
                String str = convertToChineseTime(secondsPassed);
                carMainFrame.setWorkTimeLabel(str);
            }
        };

        scheduler.scheduleAtFixedRate(timerTask, 0, 1, TimeUnit.SECONDS);
    }

    private void stopTimer() {
        scheduler.shutdown();
        System.out.println("Timer stopped.");
    }
    
    public static String convertToChineseTime(long seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds must be non-negative.");
        }

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        StringBuilder result = new StringBuilder();

        if (hours > 0) {
            result.append(hours).append("小时");
        }

        if (minutes > 0) {
            result.append(minutes).append("分钟");
        }

        if (result.length() == 0) {
            result.append("0分钟"); // 如果时间为0，则显示0分钟
        }

        return result.toString();
    }
}
